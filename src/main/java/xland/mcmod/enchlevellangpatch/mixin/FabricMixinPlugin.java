package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public class FabricMixinPlugin extends AbstractMixinPlugin {
    /** Very ancient versions that may be supported by Ornithes while unsupported by Legacy Fabric.
     * Legacy Fabric only declares support since 1.3. */
    private static final Supplier<VersionPredicate> V100_BELOW = parseVersionPredicate("<=1.0.0");
    /** Before 1.13.2, the mapping was under <a href="https://github.com/Legacy-Fabric/Legacy-Intermediaries">
     * Legacy Intermediaries</a>. */
    private static final Supplier<VersionPredicate> V1132_BELOW = parseVersionPredicate("<=1.13.2");
    /** The latest version supported by <a href='https://ornithemc.net/'>Ornithe</a>, an old Fabric
     * implementation. */
    private static final Supplier<VersionPredicate> V1144_BELOW = parseVersionPredicate("<=1.14.4");
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
    private static final String MAP_SIGNATURE = "Ljava/util/Map;";

    @NotNullByDefault
    private enum MappingEnvironment {
        OFFICIAL(
                "net.minecraft.client.resources.language.ClientLanguage", "storage", "getOrDefault",
                null, null, null,
                null
        ),
        INTERMEDIARY(
                "net.minecraft.class_1078", "field_5330", "method_4679",
                "net.minecraft.class_2477", "field_11487", "method_10518",
                "ellp.refmap-intermediary.json"
        ),
        LEGACY_FABRIC(
                "net.minecraft.class_1667", "field_6654", "method_5951",
                "net.minecraft.class_244", "field_6692", "method_6006",
                "ellp.refmap-legacy-fabric.json"
        ),
        ORNITHES_V1(
                "net.minecraft.unmapped.C_8639317", "f_5267515", "m_7114739",
                "net.minecraft.unmapped.C_0759248", "f_6169381", "m_2574583",
                "ellp.refmap-ornithes-gen1.json"
        ),
        @ApiStatus.Experimental
        ORNITHES_V2(
                "net.minecraft.unmapped.C_88121482", "f_70417737", "m_88101502",
                "net.minecraft.unmapped.C_16154270", "f_10100345", "m_41857107",
                "ellp.refmap-ornithes-gen2.json"
        ),
        ;

        private final String storageClass;
        private final String storageField;
        private final String targetMethod;
        private final @Nullable String externalClass;
        private final @Nullable String externalStorageField;
        private final @Nullable String externalTargetMethod;

        private final @Nullable String refMapName;

        MappingEnvironment(String storageClass, String storageField, String targetMethod,
                           @Nullable String externalClass, @Nullable String externalStorageField, @Nullable String externalTargetMethod,
                           @Nullable String refMapName) {
            this.storageClass = storageClass;
            this.storageField = storageField;
            this.targetMethod = targetMethod;
            this.externalClass = externalClass;
            this.externalStorageField = externalStorageField;
            this.externalTargetMethod = externalTargetMethod;
            this.refMapName = refMapName;
        }

        boolean appliesUnmodifiableWrap(Version mcVersion) {
            return !isMojMapped() && UNDER_V116.get().test(mcVersion);
        }

        boolean isMojMapped() {
            return this == OFFICIAL;
        }

        static MappingEnvironment detect(Version mcVersion) {
            if (FabricMixinPlugin.isMojMapped(mcVersion)) return OFFICIAL;

            // 1.15-snap or above, definitely modern fabric
            if (!V1144_BELOW.get().test(mcVersion)) return INTERMEDIARY;

            // Ornithes intermediary mappings are like "net.minecraft.unmapped.**".
            // ProGuard obfuscated jars will definitely contain a class named "a", so there must be
            // a mapped name if the jar is obfuscated.
            // However, very ancient versions, which Ornithes support, are not obfuscated.
            // Fortunately, Legacy Fabric only declares support since 1.3, so we can add a version check,
            // e.g. "<1.0.0", to distinguish ancient Ornithes in advance.
            boolean legacyFabricExcluded = V100_BELOW.get().test(mcVersion);
            // now: [1.0.0,1.14.4]; can be ORNITHES, LEGACY_FABRIC or INTERMEDIARY
            MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
            String firstClassEntry = mappingResolver.mapClassName("official", "a");
            if (!"intermediary".equals(mappingResolver.getCurrentRuntimeNamespace())) {
                firstClassEntry = mappingResolver.unmapClassName("intermediary", firstClassEntry);
            }
            if (firstClassEntry.startsWith("net.minecraft.unmapped.")) {
                final int gen2Length = "net.minecraft.unmapped.C_xxxxxxxx".length();
                return firstClassEntry.length() == gen2Length ? ORNITHES_V2 : ORNITHES_V1;
            }
            // LEGACY_FABRIC ... 1.13.2 | 1.14-snap INTERMEDIARY;
            // OR illegal <1.0.0 environment (no calamus intermediary present)
            if (legacyFabricExcluded) return ORNITHES_V1;   // arbitrarily
            return V1132_BELOW.get().test(mcVersion) ? LEGACY_FABRIC : INTERMEDIARY;
        }

        public String getStorageField(MappingResolver resolver) {
            return isMojMapped() ? storageField : resolver.mapFieldName(
                    "intermediary", storageClass, storageField, MAP_SIGNATURE
            );
        }

        public String getTargetMethod(MappingResolver resolver, String descriptor) {
            return isMojMapped() ? targetMethod : resolver.mapMethodName(
                    "intermediary", storageClass, targetMethod, descriptor
            );
        }

        public String getExternalStorageField(MappingResolver resolver) {
            if (externalStorageField == null) throw new UnsupportedOperationException();
            return resolver.mapFieldName("intermediary", externalClass, externalStorageField, MAP_SIGNATURE);
        }

        public String getExternalTargetMethod(MappingResolver resolver, String descriptor) {
            if (externalTargetMethod == null) throw new UnsupportedOperationException();
            return resolver.mapMethodName("intermediary", externalClass, externalTargetMethod, descriptor);
        }

        public @Nullable String getRefMapName() {
            return refMapName;
        }
    }

    private MappingEnvironment mappingEnv;
    private transient Version mcVersion;

    private Version initVersion() {
        ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new NoSuchElementException("minecraft"));
        Version minecraftVersion = minecraft.getMetadata().getVersion();
        appliesFallback = V1194_ABOVE.get().test(minecraftVersion);
        return minecraftVersion;
    }

    @Override
    public void onLoad(String mixinPackage) {
        mappingEnv = MappingEnvironment.detect(mcVersion = initVersion());
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        storageFieldName = mappingEnv.getStorageField(resolver);
        targetMethodName = mappingEnv.getTargetMethod(resolver, targetMethodDesc());
        appliesUnmodifiableWrap = mappingEnv.appliesUnmodifiableWrap(mcVersion);

        printVersion();
        setSystemProperties(mappingEnv);
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

    private static void setSystemProperties(MappingEnvironment mappingEnv) {
        switch (mappingEnv) {
            case ORNITHES_V1:
            case ORNITHES_V2:
                System.setProperty("langpatch.fabricEnv", "ornithes");
                break;
            case LEGACY_FABRIC:
                System.setProperty("langpatch.fabricEnv", "legacy-fabric");
                break;
        }
    }

    @Override
    protected boolean shouldApplyExternalLanguageMap() {
        return UNDER_V116.get().test(mcVersion);
    }

    @Override
    public String getRefMapperConfig() {
        return mappingEnv.getRefMapName();
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
    protected void applyExternal(String targetClassName, ClassNode targetClass) {
        final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        final String mappedMethodName = mappingEnv.getExternalTargetMethod(resolver, targetMethodDesc());
        final String mappedFieldName = mappingEnv.getExternalStorageField(resolver);

        MethodNode method = findMethod(targetClass, mappedMethodName, targetMethodDesc())
                .findAny()
                .orElseThrow(() -> new NoSuchElementException(mappedMethodName + " is not found in " + targetClassName));
        AsmTranslationStorage asm = new AsmTranslationStorage(
                targetClassName, mappedFieldName,
                /*fallback=*/false, /*unmodifiableWrap=*/true, /*guardPutField=*/false
        );
        asm.accept(method);
    }

    @Override
    public String toString() {
        return super.toString() + " [Minecraft version: " + mcVersion + ']';
    }
}
