package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@SuppressWarnings("unused")
public class ForgeMixinPlugin extends AbstractMixinPlugin {
	// If in Neo environment: -1
	// Otherwise: Forge major version
    private volatile Integer forgeVersion;
    private static final int V117 = 36, V1194 = 45, V1206 = 50;
    private static final int V1161 = 32;
    private static final String V1122 = "14.UNKNOWN";

    @Override
    public void onLoad(String mixinPackage) {
        if (forgeVersion == null) {
            synchronized (this) {
                if (forgeVersion == null) {
                    String forgeVersion = getForgeVersion();

                    this.forgeVersion = Integer.parseInt(forgeVersion.split("\\.", 2)[0]);
                }
            }
        }
    }

    private static @NotNull String getForgeVersion() {
		try {
			// Check Neo environment
			Class<?> clazz = Class.forName("net.neoforged.api.distmarker.Dist");
			return "-1";
		} catch (ClassNotFoundException ignored) {
		}
    
        try {
            // 1.13.1+
            Class<?> c = Class.forName("net.minecraftforge.versions.forge.ForgeVersion");
            MethodHandle mh = MethodHandles.lookup().findStatic(c, "getSpec", MethodType.methodType(String.class));
            return (String) mh.invokeExact();
        } catch (ClassNotFoundException ignored) {
            // probably 1.12.2
        } catch (Throwable e) {
            throw new IllegalStateException("Not in Forge environment", e);
        }

        try {
            // 1.12.2
            Class.forName("net.minecraftforge.fml.relauncher.Side");
            return V1122;
        } catch (ClassNotFoundException ignored) {
        }

        throw new IllegalStateException("Not in Forge environment");
    }

    @Override
    public String getRefMapperConfig() {
    	if (forgeVersion < 0 || forgeVersion >= V1206)
    		// Neo & MCF 1.20.6+ uses pure MojMaps
    		return null;
        if (forgeVersion < V1161)
            return "ellp.refmap-113.json";
        return forgeVersion >= V117 ? "ellp.refmap-117.json" : "ellp.refmap-116.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        final boolean b = mixinClassName.endsWith("1194");
        return (forgeVersion < 0 || forgeVersion >= V1194) == b;
    }
}
