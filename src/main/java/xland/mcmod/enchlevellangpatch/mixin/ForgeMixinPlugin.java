package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ForgeMixinPlugin implements IMixinConfigPlugin {
	// If in Neo environment: -1
	// Otherwise: Forge major version
    private volatile Integer forgeVersion;
    private static final int V117 = 36, V1194 = 45, V1206 = 50;

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
            Class<?> clazz = Class.forName("net.minecraftforge.fml.loading.StringSubstitutor");
            MethodHandle mh = MethodHandles.lookup().findStatic(clazz, "replace", MethodType.fromMethodDescriptorString("(Ljava/lang/String;Lnet/minecraftforge/fml/loading/moddiscovery/ModFile;)Ljava/lang/String;", clazz.getClassLoader()));
            return (String) mh.invoke("${global.forgeVersion}", (Void)null);
        } catch (Throwable e) {
            throw new IllegalStateException("Not in Forge environment", e);
        }
    }

    @Override
    public String getRefMapperConfig() {
    	if (forgeVersion < 0 || forgeVersion >= V1206)
    		// Neo & MCF 1.20.6+ uses pure MojMaps
    		return null;
        return forgeVersion >= V117 ? "ellp.refmap-117.json" : "ellp.refmap-116.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        final boolean b = mixinClassName.endsWith("1194");
        return (forgeVersion < 0 || forgeVersion >= V1194) == b;
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
