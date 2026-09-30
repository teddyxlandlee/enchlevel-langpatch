package xland.mcmod.enchlevellangpatch.mixin.legacy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.jspecify.annotations.Nullable;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

import java.io.*;
import java.net.URL;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
public class LegacyModContainer extends DummyModContainer {
    public LegacyModContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "enchlevellangpatch";
        meta.version = LegacyModContainer.class.getPackage().getImplementationVersion();
        try {
            loadMetaFromFMJ(meta);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or corrupted metadata found from LangPatch", e);
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")   // Guava's EventBus
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(new Object() {
            @Subscribe
            @SuppressWarnings("unused")
            public void initializeMod(FMLPostInitializationEvent event) {
                LangPatchImpl.init();
            }
        });
        return true;
    }

    private static void loadMetaFromFMJ(ModMetadata meta) throws JsonParseException, IOException {
        JsonObject json = readFabricModJson();

        meta.name = requireNonNull(json.get("name"), "name").getAsString();
        meta.description = requireNonNull(json.get("description"), "description").getAsString();
        meta.authorList = Lists.newArrayList(Iterables.transform(
                requireNonNull(json.getAsJsonArray("authors"), "authors"), JsonElement::getAsString
        ));
        meta.url = requireNonNull(
                requireNonNull(json.getAsJsonObject("contact"), "contact").get("homepage"),
                "contact.homepage"
        ).getAsString();

        if (isVersionInvalid(meta.version)) {
            String version = requireNonNull(json.get("version"), "version").getAsString();
            if (isVersionInvalid(version)) {
                throw new IllegalStateException("Corrupted version field at both" +
                        "MANIFEST.MF (Implementation-Version = " + meta.version +
                        ") and fabric.mod.json (version =" + version + ')'
                );
            }
            meta.version = version;
        }
    }

    private static boolean isVersionInvalid(@Nullable String s) {
        return s == null || (s = s.trim()).isEmpty() || "${version}".equals(s);
    }

    private static JsonObject readFabricModJson() throws IOException {
        java.util.Enumeration<URL> enums = LegacyModContainer.class.getClassLoader().getResources("fabric.mod.json");
        Gson gson = new Gson();

        FileNotFoundException e = new FileNotFoundException("fabric.mod.json");

        while (enums.hasMoreElements()) {
            try (Reader reader = new InputStreamReader(enums.nextElement().openStream())) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                JsonElement element = obj.get("id");
                if (element != null && element.isJsonPrimitive() && "enchlevel-langpatch".equals(element.getAsString())) {
                    // id matched, correct json
                    return obj;
                }
            } catch (Exception ex) {
                e.addSuppressed(ex);
            }
        }

        throw e;
    }
}
