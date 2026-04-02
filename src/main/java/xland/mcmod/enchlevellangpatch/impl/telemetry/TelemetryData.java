package xland.mcmod.enchlevellangpatch.impl.telemetry;

import com.google.gson.JsonObject;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

public class TelemetryData {
    static final int TELEMETRY_SCHEMA = 1;

    public static JsonObject getMandatory() {
        JsonObject obj = new JsonObject();
        obj.addProperty("schema", TELEMETRY_SCHEMA);
        obj.addProperty("mod_version", LangPatchImpl.class.getPackage().getImplementationVersion());
        obj.addProperty("mod_platform", Platform.CURRENT.getName());
        obj.addProperty("mc_version", Platform.CURRENT.getMinecraftVersion());
        return obj;
    }

    public static JsonObject getFull() {
        return getMandatory();
    }

}
