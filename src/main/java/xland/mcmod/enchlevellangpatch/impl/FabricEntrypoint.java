package xland.mcmod.enchlevellangpatch.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricEntrypoint {
    private FabricEntrypoint() {}

    public static void init() {
        FabricLoader.getInstance().getEntrypoints("enchlevel-langpatch.init", ClientModInitializer.class)
                .forEach(ClientModInitializer::onInitializeClient);
        LangPatchImpl.init();
    }
}
