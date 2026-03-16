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
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    @SuppressWarnings("UnstableApiUsage")
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
        JsonObject json;
        try (InputStream in = LegacyModContainer.class.getResourceAsStream("/fabric.mod.json")) {
            if (in == null) throw new FileNotFoundException("fabric.mod.json");
            json = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
        }

        meta.name = requireNonNull(json.get("name")).getAsString();
        meta.description = requireNonNull(json.get("description")).getAsString();
        meta.authorList = Lists.newArrayList(Iterables.transform(
                requireNonNull(json.getAsJsonArray("authors")), JsonElement::getAsString
        ));
        meta.url = requireNonNull(
                requireNonNull(json.getAsJsonObject("contact")).get("homepage")
        ).getAsString();
    }
}
