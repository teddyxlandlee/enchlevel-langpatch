package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xland.mcmod.enchlevellangpatch.impl.AsmHook;

import java.util.Map;

@Mixin(targets = "net.minecraft.client.resources.language.ClientLanguage")
public abstract class MixinTranslationStorage1194 {
    //@Shadow @Final private Map<String, String> storage;
    @Accessor("storage")
    abstract Map<String, String> ellp$getStorage();

    @Inject(at = @At("RETURN"), cancellable = true, method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")
    private void langPatchHooks(String key, String fallback, CallbackInfoReturnable<String> cir) {
        @Nullable String s = AsmHook.langPatchHookWithFallback(key, ellp$getStorage(), fallback);
        if (s != null) cir.setReturnValue(s);
    }
}
