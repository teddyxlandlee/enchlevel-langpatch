package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ForgeMixinPlugin extends AbstractMixinPlugin {
	// If in Neo environment: -1
	// Otherwise: Forge major version
    private int forgeVersion;
    private @Nullable String refMapName;

    @Override
    public void onLoad(String mixinPackage) {
        this.forgeVersion = ForgeVersion.getForgeVersionAsInt();
        this.targetMethodDesc = targetMethodDesc(is1194OrLater = forgeVersion < 0 || forgeVersion >= ForgeVersion.V1194);
        initNames();
    }

    private void initNames() {
        if (forgeVersion < 0 || forgeVersion >= ForgeVersion.V1206) {
            // Neo & MCF 1.20.6+ uses pure MojMaps
            storageFieldName = "storage";
            targetMethodName = "getOrDefault";
            refMapName = null;
        } else if (forgeVersion < ForgeVersion.V1161) {
            storageFieldName = "field_135032_a";
            targetMethodName = "func_135026_c";
            refMapName = "ellp.refmap-113.json";
        } else if (forgeVersion < ForgeVersion.V117) {
            storageFieldName = "field_239495_c_";
            targetMethodName = "func_230503_a_";
            refMapName = "ellp.refmap-116.json";
        } else {
            refMapName = null;  // classnames are MojMapped
            storageFieldName = "f_118910_";
            targetMethodName = is1194OrLater ? "m_118919_" : "m_6834_";
        }
    }

    @Override
    public String getRefMapperConfig() {
        return refMapName;
    }
}
