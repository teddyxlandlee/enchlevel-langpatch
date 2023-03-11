package xland.mcmod.enchlevellangpatch.impl;

import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

@API(status = API.Status.EXPERIMENTAL, since = "2.0.0", consumers = "xland.mcmod.enchlevellangpatch.ext.conf4")
public interface ConfigProvider {
    @Nullable
    String getEnchantmentConfig();

    @Nullable
    String getPotionConfig();
}
