package xland.mctestmod.enchlevellangpatch.chinese;

import com.google.common.collect.Maps;
import xland.mcmod.enchlevellangpatch.impl.AsmHook;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

import java.util.Map;

public class LangPatchTest {
    private static final Map<String, String> map = Maps.newLinkedHashMap();
    //private static final Logger LOGGER = LogManager.getLogger();

    private static void change(String s) {
        map.put("langpatch.conf.enchantment.default.type", s);
    }

    private static void print(String key) {
        String s = AsmHook.langPatchHook(key, map);
        if (s == null)
            s = map.getOrDefault(key, key);
        System.out.printf("`%s`: %s\n", key, s);
    }

    public static void main(String[] args) {
        LangPatchImpl.init();

        map.put("enchantment.level.x", "%s");
        map.put("enchantment.level.1", "I");

        change("arabic");
        print("enchantment.level.1");
        print("enchantment.level.14");
        print("enchantment.level.514");
        print("missingno");

        change("roman");
        print("enchantment.level.1");
        print("enchantment.level.14");
        print("enchantment.level.514");
        print("missingno");

        change("chinese");
        print("enchantment.level.1");
        print("enchantment.level.14");
        print("enchantment.level.514");
        print("missingno");

        change("traditional");
        print("enchantment.level.1");
        print("enchantment.level.14");
        print("enchantment.level.514");
        print("missingno");

        print("potion.potency.14");
    }
}
