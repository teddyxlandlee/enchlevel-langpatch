package xland.mcmod.enchlevellangpatch.impl.telemetry;

import com.google.gson.JsonObject;

public class TelemetryData {
    static final int TELEMETRY_SCHEMA = 1;

    public static JsonObject getNecessary() {
        JsonObject obj = new JsonObject();
        obj.addProperty("telemetry_level", LEVEL_MANDATORY);
        obj.addProperty("schema", TELEMETRY_SCHEMA);
        obj.addProperty("client_time", System.currentTimeMillis());
        return obj;
    }

    public static JsonObject getFunctional() {
        JsonObject obj =  getNecessary();
        obj.addProperty("telemetry_level", LEVEL_FUNCTIONAL);
        obj.addProperty("mod_version", Platform.CURRENT.getModVersion());
        obj.addProperty("mod_platform", Platform.CURRENT.getName());
        obj.addProperty("mc_version", Platform.CURRENT.getMinecraftVersion());
        return obj;
    }

    public static JsonObject getFull() {
        JsonObject obj =  getFunctional();
        obj.addProperty("telemetry_level", LEVEL_OPTIONAL);
        return obj;
    }

    static final int LEVEL_MANDATORY = 0;
    static final int LEVEL_FUNCTIONAL = 1;
    static final int LEVEL_OPTIONAL = 2;
}
