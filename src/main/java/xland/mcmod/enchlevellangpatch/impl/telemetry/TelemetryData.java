package xland.mcmod.enchlevellangpatch.impl.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatchConfig;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

import java.util.Map;

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

        obj.add("current_hooks", getCurrentHooksInfo());
        return obj;
    }

    private static JsonObject getCurrentHooksInfo() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enchantment", LangPatchImpl.ENCHANTMENT_HOOK.getId(EnchantmentLevelLangPatchConfig.getCurrentEnchantmentHooks()).toString());
        obj.addProperty("potion", LangPatchImpl.POTION_HOOK.getId(EnchantmentLevelLangPatchConfig.getCurrentPotionHooks()).toString());
        return obj;
    }

    public static JsonObject getFull() {
        JsonObject obj =  getFunctional();
        obj.addProperty("telemetry_level", LEVEL_OPTIONAL);
        obj.add("all_hooks", getAllHooksInfo());
        return obj;
    }

    private static JsonObject getAllHooksInfo() {
        JsonObject obj = new JsonObject();
        obj.add("enchantment", keySetToJsonArray(EnchantmentLevelLangPatchConfig.getEnchantmentHooksContext()));
        obj.add("potion", keySetToJsonArray(EnchantmentLevelLangPatchConfig.getPotionHooksContext()));
        return obj;
    }

    private static JsonArray keySetToJsonArray(Map<? extends String, ?> map) {
        JsonArray arr = new JsonArray();
        map.keySet().forEach(arr::add);
        return arr;
    }

    static final int LEVEL_MANDATORY = 0;
    static final int LEVEL_FUNCTIONAL = 1;
    static final int LEVEL_OPTIONAL = 2;
}
