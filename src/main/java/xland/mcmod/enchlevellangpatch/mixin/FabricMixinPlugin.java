package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class FabricMixinPlugin extends AbstractMixinPlugin {
    private static final Supplier<VersionPredicate> V1194_ABOVE = Suppliers.memoize(() -> {
        try {
            return VersionPredicate.parse(">=1.19.4-");
        } catch (VersionParsingException e) {
            throw new RuntimeException("Should not happen: invalid version predicate", e);
        }
    });

    private boolean is1194OrLater;

    private void initVersion() {
        final ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new NoSuchElementException("minecraft"));
        is1194OrLater = V1194_ABOVE.get().test(minecraft.getMetadata().getVersion());
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
}
