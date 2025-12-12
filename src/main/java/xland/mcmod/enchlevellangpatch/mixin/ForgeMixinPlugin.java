package xland.mcmod.enchlevellangpatch.mixin;

@SuppressWarnings("unused")
public class ForgeMixinPlugin extends AbstractMixinPlugin {
	// If in Neo environment: -1
	// Otherwise: Forge major version
    private volatile Integer forgeVersion;

    @Override
    public void onLoad(String mixinPackage) {
        if (forgeVersion == null) {
            synchronized (this) {
                if (forgeVersion == null) {
                    this.forgeVersion = ForgeVersion.FORGE_VERSION;
                }
            }
        }
    }

    @Override
    public String getRefMapperConfig() {
    	if (forgeVersion < 0 || forgeVersion >= ForgeVersion.V1206)
    		// Neo & MCF 1.20.6+ uses pure MojMaps
    		return null;
        if (forgeVersion < ForgeVersion.V1161)
            return "ellp.refmap-113.json";
        return forgeVersion >= ForgeVersion.V117 ? "ellp.refmap-117.json" : "ellp.refmap-116.json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        final boolean b = mixinClassName.endsWith("1194");
        return (forgeVersion < 0 || forgeVersion >= ForgeVersion.V1194) == b;
    }
}
