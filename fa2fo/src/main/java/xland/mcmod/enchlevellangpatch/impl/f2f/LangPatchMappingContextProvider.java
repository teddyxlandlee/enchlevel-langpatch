package xland.mcmod.enchlevellangpatch.impl.f2f;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xland.mcmodbridge.fa2fomapper.api.MappingContextProvider;
import xland.mcmodbridge.fa2fomapper.api.SimpleMappingContextProvider;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.stream.Collectors;

public class LangPatchMappingContextProvider extends SimpleMappingContextProvider
        implements MappingContextProvider {
    private static final Logger LOGGER = LogManager.getLogger("LangPatchMappingContextProvider");

    @Override
    protected @NotNull BufferedReader mappingReader() {
        return new BufferedReader(new StringReader(getMappingString()));
    }

    static String getMappingString() {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Class<?> clazz = Class.forName(LangPatchMappingContextProvider.class.getPackage().getName() +
                    ".$Mapping");
            return (String) lookup.findStaticGetter(clazz, "instance", String.class).invoke();
        } catch (Throwable t) {
            LOGGER.fatal("Can't find the mapping provider class");
            return "v1\tbase";
        }
    }

    public static final Collection<String> REMAPPED_CLASSES = ImmutableSet.of(
            "api.EnchantmentLevelLangPatch", "api.EnchantmentLevelLangPatchConfig",
            "impl.AsmHook", "impl.IndependentLangPatchRegistry",
            "impl.LangPatchImpl", "impl.NumberFormatUtil",
            "impl.ChineseExchange"
    );

    public LangPatchMappingContextProvider() {
        super(REMAPPED_CLASSES.stream()
                .map(c -> "xland.mcmod.enchlevellangpatch." + c)
                .map(c -> c.replace('.', '/'))
                .collect(Collectors.toSet())
        );
        LOGGER.info("Preparing remapping classes");
    }
}
