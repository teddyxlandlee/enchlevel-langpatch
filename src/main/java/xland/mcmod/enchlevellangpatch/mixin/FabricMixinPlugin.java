package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public class FabricMixinPlugin extends AbstractMixinPlugin {
    /** The {@code storage} field was <i>not</i> unmodifiable until this version. */
    private static final Supplier<VersionPredicate> UNDER_V116 = parseVersionPredicate("<1.16-alpha.20.22.a");

    /** This version (23w03a, first snapshot of 1.19.4) introduces Fallback Translation. */
    private static final Supplier<VersionPredicate> V1194_ABOVE = parseVersionPredicate(">=1.19.4-");

    /** Definitely not moj-named. */
    private static final Supplier<VersionPredicate> V25W44A_BELOW = parseVersionPredicate("<=1.21.11-alpha.25.44.a");
    /**
     * The version Mojang <b>declares</b> to completely remove obfuscation.
     * @see <a href='https://www.minecraft.net/zh-hans/article/removing-obfuscation-in-java-edition'>
     *     Removing obfuscation in Java Edition - minecraft.net</a>
     * @see <a href='https://www.minecraft.net/zh-hans/article/minecraft-new-version-numbering-system'>
     *     Minecraft's new version numbering system</a>
     */
    private static final Supplier<VersionPredicate> V26_ABOVE = parseVersionPredicate(">=26");

    /** SemVer support for <code>&lt;snapshot&gt;[ _][uU]nobfuscated</code> since Fabric Loader 0.18.0 */
    private static final String UNOBFUSCATED_BUILD = "unobfuscated";

    private boolean isMojMapped;
    private transient Version mcVersion;

    private Version initVersion() {
        ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new NoSuchElementException("minecraft"));
        Version minecraftVersion = minecraft.getMetadata().getVersion();
        targetMethodDesc = targetMethodDesc(appliesFallback = V1194_ABOVE.get().test(minecraftVersion));
        return minecraftVersion;
    }

    private void initNames() {
        if (isMojMapped) {
            storageFieldName = "storage";
            targetMethodName = "getOrDefault";
        } else {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
            storageFieldName = resolver.mapFieldName("intermediary", "net.minecraft.class_1078", "field_5330", "Ljava/util/Map;");
            targetMethodName = resolver.mapMethodName(
                    "intermediary",
                    "net.minecraft.class_1078", "method_4679",
                    targetMethodDesc
            );

            // small optimization: skip this check if already `isMojMapped`
            appliesUnmodifiableWrap = UNDER_V116.get().test(mcVersion);
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        isMojMapped = isMojMapped(mcVersion = initVersion());
        appliesPutFieldGuardCheck = isMojMapped;
        initNames();

        printVersion();
    }

    public static boolean isMojMapped(Version minecraftVersion) {
        Objects.requireNonNull(minecraftVersion);

        if (V25W44A_BELOW.get().test(minecraftVersion)) return false;  // definitely not moj-named, no obf removal at all

        boolean laterThanMountsOfMayhem = V26_ABOVE.get().test(minecraftVersion);
        if (laterThanMountsOfMayhem) return true;   // definitely moj-named, according to Mojang's declaration

        // (25w44a, 1.21.11], likely dual-versioned
        return minecraftVersion instanceof SemanticVersion &&
                ((SemanticVersion) minecraftVersion).getBuildKey()
                        .orElse("")
                        .toLowerCase(Locale.ROOT)
                        .contains(UNOBFUSCATED_BUILD);

        // An alternative option (still unclear of its availability)
        // return "official".equals(FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace());
    }

    @Override
    protected boolean shouldApplyExternalLanguageMap() {
        return UNDER_V116.get().test(mcVersion);
    }

    @Override
    public String getRefMapperConfig() {
        return isMojMapped ? null : "ellp.refmap-intermediary.json";
    }

    private static Supplier<VersionPredicate> parseVersionPredicate(String predicate) {
        return Suppliers.memoize(() -> {
            try {
                return VersionPredicate.parse(predicate);
            } catch (VersionParsingException e) {
                throw new IllegalArgumentException("Invalid version predicate: " + predicate, e);
            }
        });
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);

        if (mixinClassName.endsWith(".MixinExternalLanguageMap")) {
            MethodNode method = findMethod(targetClass, "method_10518", targetMethodDesc)
                    .findAny()
                    .orElseThrow(() -> new NoSuchElementException("method_10518 is not found in " + targetClassName));
            AsmTranslationStorage asm = new AsmTranslationStorage(
                    targetClassName, "field_11487",
                    /*fallback=*/false, /*unmodifiableWrap=*/true, /*guardPutField=*/false
            );
            asm.accept(method);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " [Minecraft version: " + mcVersion + ']';
    }
}
