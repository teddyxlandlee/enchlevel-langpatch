package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class FabricMixinPlugin implements IMixinConfigPlugin {
    private static final Supplier<Version> V23w03a = Suppliers.memoize(() -> {
        try {
            return Version.parse("1.19.4-alpha.23.a.3");
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    });

    private boolean is1194OrLater;

    private void initVersion() {
        final ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new NoSuchElementException("minecraft"));
        is1194OrLater = minecraft.getMetadata().getVersion().compareTo(V23w03a.get()) >= 0;
    }

    @Override
    public void onLoad(String mixinPackage) {
        initVersion();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        final boolean b = mixinClassName.endsWith("1194");
        return is1194OrLater == b;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
