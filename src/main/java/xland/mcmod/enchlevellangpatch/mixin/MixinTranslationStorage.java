package xland.mcmod.enchlevellangpatch.mixin;

import net.minecraft.client.resource.language.TranslationStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xland.mcmod.enchlevellangpatch.impl.AsmHook;

import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {
    @Shadow @Final private Map<String, String> translations;

    @Inject(at = @At("RETURN"), cancellable = true, method = "get(Ljava/lang/String;)Ljava/lang/String;")
    private void langPatchHooks(String key, CallbackInfoReturnable<String> cir) {
        @Nullable String s = AsmHook.langPatchHook(key, translations);
        if (s != null) cir.setReturnValue(s);
    }
}
