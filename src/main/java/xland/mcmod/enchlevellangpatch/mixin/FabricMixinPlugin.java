package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.apiguardian.api.API;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public class FabricMixinPlugin extends AbstractMixinPlugin {
    /** This version introduces Fallback Translation. */
    private static final Supplier<VersionPredicate> V1194_ABOVE = parseVersionPredicate(">=1.19.4-");
    /**
     * The version Mojang <b>declares</b> to completely remove obfuscation.
     * Hopefully there's no hotfix for Mounts of Mayhem or something.
     * @see <a href='https://www.minecraft.net/zh-hans/article/removing-obfuscation-in-java-edition'>
     *     Removing obfuscation in Java Edition - minecraft.net</a>
     * @see <a href='https://www.minecraft.net/zh-hans/article/minecraft-new-version-numbering-system'>
     *     Minecraft's new version numbering system</a>
     */
    @API(status = API.Status.EXPERIMENTAL)
    private static final Supplier<VersionPredicate> V12112_ABOVE = parseVersionPredicate(">=1.21.12-");
    /** Definitely not moj-named. */
    private static final Supplier<VersionPredicate> PRE_25W44A = parseVersionPredicate("<=1.21.11-alpha.25.44.a");
    /** SemVer support for <code>&lt;snapshot&gt;[ _][uU]nobfuscated</code> since Fabric Loader 0.18.0 */
    private static final String UNOBFUSCATED_BUILD = "unobfuscated";

    private Version minecraftVersion;
    private boolean isMojMapped;

    private void initVersion() {
        ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new NoSuchElementException("minecraft"));
        minecraftVersion = minecraft.getMetadata().getVersion();
        targetMethodDesc = targetMethodDesc(is1194OrLater = V1194_ABOVE.get().test(minecraftVersion));
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
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        initVersion();
        isMojMapped = isMojMapped();
        initNames();
    }

    public boolean isMojMapped() {
        Objects.requireNonNull(minecraftVersion);
        // TODO: so far

        if (PRE_25W44A.get().test(minecraftVersion)) return false;  // definitely not moj-named, no obf removal at all

        boolean laterThanMountsOfMayhem = V12112_ABOVE.get().test(minecraftVersion);
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
}
