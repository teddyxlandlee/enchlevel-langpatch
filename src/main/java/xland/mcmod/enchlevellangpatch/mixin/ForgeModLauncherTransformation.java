package xland.mcmod.enchlevellangpatch.mixin;

import cpw.mods.modlauncher.api.*;
import net.minecraftforge.coremod.api.ASMAPI;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ForgeModLauncherTransformation implements ITransformationService {
    private boolean skipMe;

    @Override
    public @NotNull String name() {
        return "langpatch_legacy";
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void beginScanning(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        skipMe = otherServices.contains("mixin");
    }

    @Override
    public @NotNull List<ITransformer<?>> transformers() {
        int forgeVersion = ForgeVersion.getForgeVersionAsInt();

        skipMe |= forgeVersion >= ForgeVersion.V1161 || forgeVersion < 0;

        if (skipMe) return Collections.emptyList();

        return Collections.singletonList(new ITransformer<MethodNode>() {
            // pre-1.16 srg names
            // post-1.16 forge environments definitely embed mixin, so this antique retires.
            final String className = "net.minecraft.client.resources.Locale";
            final String storageFieldName = ASMAPI.mapField("field_135032_a");
            final String getLocaleMethodName = ASMAPI.mapMethod("func_135026_c");

            @Override
            public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
                AsmTranslationStorage asm = new AsmTranslationStorage(false, className, storageFieldName);
                return asm.apply(input);
            }

            @Override
            public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
                return TransformerVoteResult.YES;
            }

            @Override
            public @NotNull Set<Target> targets() {
                return Collections.singleton(Target.targetMethod(
                        className, getLocaleMethodName,
                        // always pre-1.19.4
                        "(Ljava/lang/String;)Ljava/lang/String;"
                ));
            }
        });
    }
}
