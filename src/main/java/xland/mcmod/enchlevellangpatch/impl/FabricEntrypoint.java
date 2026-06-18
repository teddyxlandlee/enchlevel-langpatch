package xland.mcmod.enchlevellangpatch.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricEntrypoint {
    private FabricEntrypoint() {}

    public static void init() {
        FabricLoader.getInstance().invokeEntrypoints(
                "enchlevel-langpatch.init",
                ClientModInitializer.class,
                ClientModInitializer::onInitializeClient
        );
        LangPatchImpl.init();
    }
}
