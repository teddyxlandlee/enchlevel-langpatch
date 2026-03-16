package xland.mcmod.enchlevellangpatch.mixin;

import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
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
    protected abstract boolean shouldApplyExternalLanguageMap();

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    protected static final String MIXIN_TRANSLATION_STORAGE = ".MixinTranslationStorage";
    protected static final String MIXIN_EXTERNAL_LANGUAGE_MAP = ".MixinExternalLanguageMap";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(MIXIN_TRANSLATION_STORAGE)) {
            return true;
        } else if (mixinClassName.endsWith(MIXIN_EXTERNAL_LANGUAGE_MAP)) {
            return shouldApplyExternalLanguageMap();
        } else {    // should not happen
            org.apache.logging.log4j.LogManager.getLogger().error(
                    "Invalid mixin {} -> {} registered to LangPatch. Rejected.",
                    mixinClassName, targetClassName
            );
            return false;
        }
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
        if (!mixinClassName.endsWith(MIXIN_TRANSLATION_STORAGE)) return;

        boolean appliesPutFieldGuardCheck0 = this.appliesPutFieldGuardCheck;
        if (appliesPutFieldGuardCheck0) {
            targetClass.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL,
                    AsmTranslationStorage.unmodifiableViewFieldName, "Ljava/util/Map;", null, null
            );
            try {
                findMethod(targetClass, "<init>", null).forEach(m -> AsmTranslationStorage.applyPutFieldGuardCheck(m, targetClass.name));
            } catch (Exception ex) {
                org.apache.logging.log4j.LogManager.getLogger().error("Exception while applying putFieldGuardCheck. This will be disabled.", ex);
                appliesPutFieldGuardCheck0 = false;
            }
        }

        MethodNode method = findMethod(targetClass, targetMethodName, targetMethodDesc)
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Method %s.%s:%s not found", targetClassName, targetMethodName, targetMethodDesc)
                ));
        AsmTranslationStorage asm = new AsmTranslationStorage(targetClassName, storageFieldName, appliesFallback, appliesUnmodifiableWrap, appliesPutFieldGuardCheck0);
        asm.accept(method);
    }

    protected void printVersion() {
        LogManager.getLogger(this).info("Loading LangPatch Mixin plugin {}", this);
    }
}
