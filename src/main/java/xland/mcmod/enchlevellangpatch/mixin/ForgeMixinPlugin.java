package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public class ForgeMixinPlugin extends AbstractMixinPlugin {
	// If in Neo environment: negative
	// Otherwise: Forge major version
    private int forgeVersion;
    private @Nullable String refMapName;

    @Override
    public void onLoad(String mixinPackage) {
        this.forgeVersion = ForgeVersion.getForgeVersionAsInt();
        this.targetMethodDesc = targetMethodDesc(appliesFallback = forgeVersion < 0 || forgeVersion >= ForgeVersion.V1194);
        initNames();

        printVersion();
    }

    private void initNames() {
        if (forgeVersion < 0 || forgeVersion >= ForgeVersion.V1206) {
            // Neo & MCF 1.20.6+ uses pure MojMaps
            storageFieldName = "storage";
            targetMethodName = "getOrDefault";
            refMapName = null;

            if (forgeVersion <= ForgeVersion.NEO_11 || forgeVersion > ForgeVersion.V121Z) {
                appliesPutFieldGuardCheck = true;
            }
        } else if (forgeVersion < ForgeVersion.V1161) {
            storageFieldName = "field_135032_a";
            targetMethodName = "func_135026_c";
            refMapName = forgeVersion >= ForgeVersion.V115 ? "ellp.refmap-115.json" : "ellp.refmap-113.json";

            // This happens at 1.16-20w22a.

            // Coincidentally, `ImmutableMap` wrap happens the same time
            // `Locale` renames to `ClientLanguage`.
            // @see AsmHook#langPatchHook(String, Map, String, boolean)

            // At the same time, net.minecraft.locale.Language #2477 becomes abstract,
            // and the internal map reference `field_74816_c` [private final "storage" #11487] gets removed.

            // ne.mi.lo.Language in SRG name: ne.mi.util.text.translation.LanguageMap
            // (no `translation` subpackage in 1.15~1.15.2), with additional methods
            // func_74805_b [public synchronized "getElement" #10520] and
            // func_135064_c [private "getProperty" #10518].

            // These fields/methods above are confirmed to exist in 1.12.2.
            appliesUnmodifiableWrap = true;
        } else if (forgeVersion < ForgeVersion.V117) {
            storageFieldName = "field_239495_c_";
            targetMethodName = "func_230503_a_";
            refMapName = "ellp.refmap-116.json";
        } else {
            refMapName = null;  // classnames are MojMapped
            storageFieldName = "f_118910_";
            targetMethodName = appliesFallback ? "m_118919_" : "m_6834_";
        }
    }

    @Override
    protected boolean shouldApplyExternalLanguageMap() {
        return forgeVersion >= 0 /*non-NeoForge*/ && forgeVersion < ForgeVersion.V1161;
    }

    @Override
    public String getRefMapperConfig() {
        return refMapName;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Processes MixinTranslationStorage
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);

        if (mixinClassName.endsWith(".MixinExternalLanguageMap")) {
            MethodNode method = findMethod(targetClass, "func_135064_c", targetMethodDesc)
                    .findAny()
                    .orElseThrow(() -> new NoSuchElementException("func_135064_c is not found in " + targetClassName));
            AsmTranslationStorage asm = new AsmTranslationStorage(
                    targetClassName, "field_74816_c",
                    /*fallback=*/false, /*unmodifiableWrap=*/true, /*guardPutField=*/false
            );
            asm.accept(method);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " [Forge version: " + forgeVersion + ']';
    }
}
