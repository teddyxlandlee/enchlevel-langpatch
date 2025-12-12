package xland.mcmod.enchlevellangpatch.mixin;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class ForgeVersion {
    private ForgeVersion() {}

    private static @NotNull String getForgeVersion() {
        try {
            // Check Neo environment
            Class<?> clazz = Class.forName("net.neoforged.api.distmarker.Dist");
            return "-1";
        } catch (ClassNotFoundException ignored) {
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            // 1.13.1+
            Class<?> c = Class.forName("net.minecraftforge.versions.forge.ForgeVersion");
            MethodHandle mh = lookup.findStatic(c, "getVersion", MethodType.methodType(String.class));
            return (String) mh.invokeExact();
        } catch (ClassNotFoundException ignored) {
            // probably 1.12.2
        } catch (Throwable e) {
            throw new IllegalStateException("Corrupted environment", e);
        }

        try {
            // 1.12.2~
            Class<?> c = Class.forName("net.minecraftforge.fml.relauncher.FMLInjectionData");
            java.lang.reflect.Method m = c.getMethod("data");
            Object[] arr = (Object[]) lookup.unreflect(m).invoke();
            if (arr == null || arr.length == 0) throw new ArrayIndexOutOfBoundsException(0);
            return String.valueOf(arr[0]);
        } catch (ClassNotFoundException ignored) {
            // probably not Forge 1.12.2
        } catch (Throwable e) {
            throw new IllegalStateException("Corrupt environment", e);
        }

        throw new IllegalStateException("Not in Forge environment");
    }

    static final int V117 = 36, V1194 = 45, V1206 = 50;
    static final int V1161 = 32;

    static final int FORGE_VERSION = Integer.parseInt(getForgeVersion().split("\\.", 2)[0]);
}
