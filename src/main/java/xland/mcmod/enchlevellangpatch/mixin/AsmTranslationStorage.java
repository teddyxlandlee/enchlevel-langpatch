package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class AsmTranslationStorage implements Consumer<MethodNode>, UnaryOperator<MethodNode> {
    private final String thisClassName;
    private final transient String storageMapFieldName;
    private static final String HOOK_CLASS = "xland/mcmod/enchlevellangpatch/impl/AsmHook";
    private final transient String hookMethodName;
    private final transient String hookMethodDesc;
    private final boolean appliesFallback;
    private final boolean appliesUnmodifiableWrap;

    public AsmTranslationStorage(@NotNull String thisClassName, @NotNull String storageMapFieldName,
                                 boolean appliesFallback, boolean appliesUnmodifiableWrap) {
        this.thisClassName = thisClassName.replace('.', '/');
        this.storageMapFieldName = Objects.requireNonNull(storageMapFieldName);
        this.appliesFallback = appliesFallback;
        if (appliesFallback) {
            hookMethodName = "langPatchHookWithFallback";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;Ljava/lang/String;)Ljava/lang/String;";
        } else {
            hookMethodName = "langPatchHook";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;";
        }
        this.appliesUnmodifiableWrap = appliesUnmodifiableWrap;
    }

    @Override
    public void accept(MethodNode m) {
        InsnList insnList = new InsnList();
        LabelNode L_ifNull = new LabelNode();

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));        // key
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, thisClassName, storageMapFieldName, "Ljava/util/Map;"));
        if (appliesUnmodifiableWrap) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "unmodifiableMap", "(Ljava/util/Map;)Ljava/util/Map;", false));
        }
        if (appliesFallback) {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));    // fallback
        }
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_CLASS, hookMethodName, hookMethodDesc, false));

        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new JumpInsnNode(Opcodes.IFNULL, L_ifNull));
        // nonnull - redirected
        insnList.add(new InsnNode(Opcodes.ARETURN));
        // null - no replacement
        insnList.add(L_ifNull);
        insnList.add(new FrameNode(Opcodes.F_SAME1, -1, null, -1, new Object[]{"java/lang/String"}));
        insnList.add(new InsnNode(Opcodes.POP));

        // insert insnList to the head
        m.instructions.insert(insnList);
    }

    @Override
    public MethodNode apply(MethodNode methodNode) {
        this.accept(methodNode);
        return methodNode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("AsmTranslationStorage")
                .add("target", thisClassName)
                .add("appliesFallback", appliesFallback)
                .add("appliesUnmodifiableWrap", appliesUnmodifiableWrap)
                .toString();
    }

    static void applyPutFieldGuardCheck(MethodNode m) {
        final Handle bootstrapMethod = new Handle(
                Opcodes.H_INVOKESTATIC, HOOK_CLASS, "guardRefEqual",
                Type.getMethodDescriptor(
                        Type.getType(CallSite.class),
                        Type.getType(MethodHandles.Lookup.class), Type.getType(String.class), Type.getType(MethodType.class),
                        Type.getType(Object[].class)
                ), false
        );
        final Type typeMap = Type.getType(Map.class);
        final Type typeImmutableMap = Type.getType(ImmutableMap.class);
        final List<? /*extends ConstantDesc*/> unmodifiableFilters = Arrays.asList(
                // Map.copyOf(Map) : Map
                new Handle(Opcodes.H_INVOKESTATIC, typeMap.getInternalName(), "copyOf", Type.getMethodDescriptor(typeMap, typeMap), true),
                // instanceof ImmutableMap, ImmutableSortedMap
                typeImmutableMap,
                // instanceof Collections.Unmodifiable[*]Map
                new Handle(Opcodes.H_INVOKESTATIC, HOOK_CLASS, "isCollectionsUnmodifiable", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, typeMap), false),
                // is Collections.emptyMap()
                new Handle(Opcodes.H_PUTSTATIC, "java/util/Collections", "EMPTY_MAP", typeMap.getDescriptor(), false),
                // is Collections.empty[Sorted,Navigable]Map()
                new Handle(Opcodes.H_INVOKESTATIC, "java/util/Collections", "emptySortedMap", Type.getMethodDescriptor(Type.getType(SortedMap.class)), false)
                // Other circumstances will not be considered
        );

        final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        for (AbstractInsnNode node = m.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node.getOpcode() == Opcodes.PUTFIELD) {     // implicitly instanceof FieldInsnNode
                FieldInsnNode fieldNode = (FieldInsnNode) node;
                if ("storage".equals(fieldNode.name)) {
                    final String errorMessage = String.format(
                            "[LangPatch] field storage:%2$s (invoked in constructor%1$s) is not unmodifiable",
                            m.desc, fieldNode.desc
                    );

                    Type fieldType = Type.getType(fieldNode.desc);
                    InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode(
                            base64Encoder.encodeToString(errorMessage.getBytes(StandardCharsets.UTF_8)),
                            Type.getMethodDescriptor(fieldType, fieldType),
                            bootstrapMethod, unmodifiableFilters.toArray()
                    );
                    m.instructions.insertBefore(fieldNode, indy);
                }
            }
        }
    }
}
