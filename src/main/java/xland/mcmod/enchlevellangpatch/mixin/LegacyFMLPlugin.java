package xland.mcmod.enchlevellangpatch.mixin;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class LegacyFMLPlugin implements IFMLLoadingPlugin {
    private final boolean shouldLoad;

    public LegacyFMLPlugin() {
        shouldLoad = ForgeVersion.isInjected.compareAndSet(false, true);
    }

    @Override
    public String[] getASMTransformerClass() {
        if (!shouldLoad) return new String[0];
        return new String[] {"xland.mcmod.enchlevellangpatch.mixin.legacy.LegacyTransformer"};
    }

    @Override
    public String getModContainerClass() {
        if (!shouldLoad) return null;
        return "xland.mcmod.enchlevellangpatch.mixin.legacy.LegacyModContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
