package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

abstract class AbstractMixinPlugin implements IMixinConfigPlugin {
    // >=1.19.4-
    protected boolean appliesFallback;
    protected boolean appliesUnmodifiableWrap;
    // Injects an indy before putField.
    // Fields must be moj-named.
    protected boolean appliesPutFieldGuardCheck;
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

    protected static Stream<MethodNode> findMethod(@NotNull ClassNode classNode, @NotNull String name, @Nullable String desc) {
        Objects.requireNonNull(name, "name");
        return classNode.methods.stream()
                .filter(m -> name.equals(m.name) && (desc == null || desc.equals(m.desc)));
    }

    protected static String targetMethodDesc(boolean isAbove1194) {
        return isAbove1194 ? "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;" : "(Ljava/lang/String;)Ljava/lang/String;";
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (appliesPutFieldGuardCheck) {
            findMethod(targetClass, "<init>", null).forEach(AsmTranslationStorage::applyPutFieldGuardCheck);
        }

        MethodNode method = findMethod(targetClass, targetMethodName, targetMethodDesc).findAny().orElseThrow(NoSuchElementException::new);
        AsmTranslationStorage asm = new AsmTranslationStorage(targetClassName, storageFieldName, appliesFallback, appliesUnmodifiableWrap, appliesPutFieldGuardCheck);
        asm.accept(method);
    }
}
