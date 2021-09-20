package xland.mcmod.enchlevellangpatch.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;
import xland.mcmod.enchlevellangpatch.impl.LangPatchImpl;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {
    @Shadow @Final private Map<String, String> translations;

    @Inject(at = @At("RETURN"), cancellable = true, method = "get(Ljava/lang/String;)Ljava/lang/String;")
    private void langPatchHooks(String key, CallbackInfoReturnable<String> cir) {

        LangPatchImpl.forEach((Predicate<String> keyPredicate,
                               EnchantmentLevelLangPatch valueMapping) -> {
            if (keyPredicate.test(key)) {
                cir.setReturnValue(valueMapping.apply(ImmutableMap.copyOf(translations), key));
                return true;
            } return false;
        });
    }
}
