package xland.mcmod.enchlevellangpatch.impl.f2f;

import org.apache.logging.log4j.LogManager;
import xland.mcmodbridge.fa2fomapper.api.AbstractMapperTransformationService;
import xland.mcmodbridge.fa2fomapper.api.MappingContextProvider;

public class LangPatchTransformationService extends AbstractMapperTransformationService {
    @Override
    public String mapperName() {
        return "enchlevel-langpatch";
    }

    public LangPatchTransformationService() {
        LogManager.getLogger().info(name() + " loaded!");
    }

    @Override
    public MappingContextProvider mappingContext() {
        return new LangPatchMappingContextProvider();
    }
}

