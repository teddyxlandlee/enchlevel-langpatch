package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class AsmTranslationStorage implements Consumer<MethodNode>, UnaryOperator<MethodNode> {
    private final boolean isAbove1194;
    private final String thisClassName;
    private final String storageMapFieldName;
    private static final String HOOK_CLASS = "xland/mcmod/enchlevellangpatch/impl/AsmHook";
    private final String hookMethodName;
    private final String hookMethodDesc;

    public AsmTranslationStorage(boolean isAbove1194, @NotNull String thisClassName, @NotNull String storageMapFieldName) {
        this.isAbove1194 = isAbove1194;
        this.thisClassName = thisClassName.replace('.', '/');
        this.storageMapFieldName = Objects.requireNonNull(storageMapFieldName);
        if (isAbove1194) {
            hookMethodName = "langPatchHookWithFallback";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;Ljava/lang/String;)Ljava/lang/String;";
        } else {
            hookMethodName = "langPatchHook";
            hookMethodDesc = "(Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;";
        }
    }

    @Override
    public void accept(MethodNode m) {
        InsnList insnList = new InsnList();
        LabelNode L_ifNull = new LabelNode();

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));        // key
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, thisClassName, storageMapFieldName, "Ljava/util/Map;"));
        if (isAbove1194) {
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
        insnList.add(m.instructions);
        m.instructions = insnList;
    }

    @Override
    public MethodNode apply(MethodNode methodNode) {
        this.accept(methodNode);
        return methodNode;
    }

    @Override
    public String toString() {
        return "AsmTranslationStorage[" + (isAbove1194 ? "above" : "below") + " 1.19.4]";
    }
}
