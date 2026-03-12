package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class AsmTranslationStorage implements Consumer<MethodNode>, UnaryOperator<MethodNode> {
    private final String thisClassName;
    private final transient String storageMapFieldName;
    private static final String HOOK_CLASS = "xland/mcmod/enchlevellangpatch/impl/AsmHook";
    private final transient String hookMethodName;
    private final transient String hookMethodDesc;
    private final boolean appliesFallback;
    private final boolean appliesUnmodifiableWrap;
    private final boolean appliesPutFieldGuardCheck;

    public AsmTranslationStorage(@NotNull String thisClassName, @NotNull String storageMapFieldName,
                                 boolean appliesFallback, boolean appliesUnmodifiableWrap, boolean appliesPutFieldGuardCheck) {
        this.thisClassName = thisClassName.replace('.', '/');
        Objects.requireNonNull(storageMapFieldName);
        if (appliesPutFieldGuardCheck) {
            this.storageMapFieldName = unmodifiableViewFieldName;
        } else {
            this.storageMapFieldName = storageMapFieldName;
        }
        this.appliesFallback = appliesFallback;
        if (appliesFallback) {
            hookMethodName = "langPatchHookWithFallback";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;Ljava/lang/String;)Ljava/lang/String;";
        } else {
            hookMethodName = "langPatchHook";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;";
        }
        this.appliesUnmodifiableWrap = appliesUnmodifiableWrap;
        this.appliesPutFieldGuardCheck = appliesPutFieldGuardCheck;
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
                .add("appliesPutFieldGuardCheck", appliesPutFieldGuardCheck)
                .toString();
    }

    static final String unmodifiableViewFieldName = "019ce1c8-e8f3-7231-b2bc-062dc83b1c42";

    static void applyPutFieldGuardCheck(MethodNode m, String thisClassName) {
        final Handle bootstrapMethod = new Handle(
                Opcodes.H_INVOKESTATIC, HOOK_CLASS, "makeUnmodifiableView",
                Type.getMethodDescriptor(
                        Type.getType(CallSite.class),
                        Type.getType(MethodHandles.Lookup.class), Type.getType(String.class), Type.getType(MethodType.class),
                        Type.getType(MethodHandle.class), Type.getType(Object[].class)
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
                new Handle(Opcodes.H_GETSTATIC, "java/util/Collections", "EMPTY_MAP", typeMap.getDescriptor(), false),
                // is Collections.empty[Sorted,Navigable]Map()
                new Handle(Opcodes.H_INVOKESTATIC, "java/util/Collections", "emptySortedMap", Type.getMethodDescriptor(Type.getType(SortedMap.class)), false)
                // Other circumstances will not be considered
        );
        final Handle fallbackHandle = new Handle(Opcodes.H_INVOKESTATIC, "java/util/Collections", "unmodifiableMap", Type.getMethodDescriptor(typeMap, typeMap), false);

        final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        boolean injected = false;
        for (AbstractInsnNode node = m.instructions.getFirst(); node != null; node = node.getNext()) {
            if (node.getOpcode() == Opcodes.PUTFIELD) {     // implicitly instanceof FieldInsnNode
                FieldInsnNode fieldNode = (FieldInsnNode) node;
                if ("storage".equals(fieldNode.name)) {
                    if (!typeMap.getDescriptor().equals(fieldNode.desc)) {
                        throw new IllegalArgumentException("Unsupported storage field: " + fieldNode.desc);
                    }
                    final String owner = fieldNode.owner;
                    Preconditions.checkState(Objects.equals(owner, thisClassName), "storage::owner != this. Should not happen");

                    final String errorMessage = String.format(
                            "[LangPatch] field storage:Ljava/util/Map; (invoked in constructor%1$s) should be unmodifiable, got",
                            m.desc
                    );

                    InsnList list = new InsnList();
                    list.add(new InsnNode(Opcodes.DUP2));   // [this] | [storage]
                    list.add(new InvokeDynamicInsnNode(
                            base64Encoder.encodeToString(errorMessage.getBytes(StandardCharsets.UTF_8)),
                            Type.getMethodDescriptor(typeMap, typeMap),
                            bootstrapMethod, Stream.concat(Stream.of(fallbackHandle), unmodifiableFilters.stream()).toArray()
                    ));
                    list.add(new FieldInsnNode(Opcodes.PUTFIELD, owner, unmodifiableViewFieldName, typeMap.getDescriptor()));
                    m.instructions.insertBefore(fieldNode, list);
                    injected = true;
                }
            }
        }
        Preconditions.checkState(injected, "No putField found");
    }
}
