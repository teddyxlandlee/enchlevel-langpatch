package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

abstract class AbstractMixinPlugin implements IMixinConfigPlugin {
    protected boolean is1194OrLater;
    protected String storageFieldName;
    protected String targetMethodName;
    protected String targetMethodDesc;

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    protected static MethodNode findMethod(@NotNull ClassNode classNode, @NotNull String name, @NotNull String desc) throws NoSuchElementException {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(desc, "desc");
        return classNode.methods.stream()
                .filter(m -> name.equals(m.name) && desc.equals(m.desc))
                .findAny()
                .orElseThrow(NoSuchElementException::new);
    }

    protected static String targetMethodDesc(boolean isAbove1194) {
        return isAbove1194 ? "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;" : "(Ljava/lang/String;)Ljava/lang/String;";
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        MethodNode method = findMethod(targetClass, targetMethodName, targetMethodDesc);
        AsmTranslationStorage asm = new AsmTranslationStorage(is1194OrLater, targetClassName, storageFieldName);
        asm.accept(method);
    }
}
