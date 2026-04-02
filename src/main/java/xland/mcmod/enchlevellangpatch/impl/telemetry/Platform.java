package xland.mcmod.enchlevellangpatch.impl.telemetry;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

// Why building another wheel: separate "mixin" package
abstract class Platform {
    private static final Logger LOGGER = LogManager.getLogger();
    static final Platform CURRENT = probe();

    abstract String getName();

    abstract String getMinecraftVersion();

    private static Platform probe() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return new FabricPlatform();
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class.forName("net.minecraftforge.versions.forge.ForgeVersion");
            return new ForgePlatform();
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class.forName("net.minecraftforge.fml.relauncher.FMLInjectionData");
            return new ForgePlatform();
        } catch (ClassNotFoundException ignore) {
        }

        try {
            Class<?> c = Class.forName("net.neoforged.fml.loading.FMLLoader");
            return new NeoPlatform(c);
        } catch (ClassNotFoundException ignore) {
        }

        return new UnknownPlatform();
    }

    private static final class FabricPlatform extends Platform {
        private final boolean isQuilt;

        private FabricPlatform() {
            boolean isQuilt0 = false;
            try {
                Class.forName("org.quiltmc.loader.api.QuiltLoader");
                isQuilt0 = true;
            } catch (ClassNotFoundException ignore) {
            }
            isQuilt = isQuilt0;
        }

        @Override
        String getName() {
            return isQuilt ? "quilt" : "fabric";
        }

        @Override
        String getMinecraftVersion() {
            return FabricLoader.getInstance().getRawGameVersion();
        }
    }

    private static final class ForgePlatform extends Platform {
        @Override
        String getName() {
            return "forge";
        }

        @Override
        String getMinecraftVersion() {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle mh = null;
            try {
                try {
                    Class<?> c = Class.forName("net.minecraftforge.versions.mcp.MCPVersion");
                    mh = lookup.findStatic(c, "getMCVersion", MethodType.methodType(String.class));
                } catch (ClassNotFoundException ignore) {
                }

                if (mh == null) {
                    Class<?> c = Class.forName("net.minecraftforge.common.ForgeVersion");
                    mh = lookup.findStaticGetter(c, "mcVersion", String.class);
                }
                return (String) mh.invokeExact();
            } catch (Throwable t) {
                LOGGER.warn("Failed to get minecraft version on Forge platform for telemetry", t);
                return "";
            }
        }
    }

    private static final class NeoPlatform extends Platform {
        private final Class<?> fmlLoaderClass;

        private NeoPlatform(Class<?> fmlLoaderClass) {
            this.fmlLoaderClass = fmlLoaderClass;
        }

        @Override
        String getName() {
            return "neoforge";
        }

        @Override
        String getMinecraftVersion() {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> versionInfoClass;
            MethodHandle mh = null;

            try {
                versionInfoClass = Class.forName("net.neoforged.fml.loading.VersionInfo");

                try {
                    mh = lookup.findStatic(fmlLoaderClass, "versionInfo", MethodType.methodType(versionInfoClass));
                } catch (NoSuchMethodException ignore) {
                }

                if (mh == null) {
                    MethodHandle getCurrent = lookup.findStatic(fmlLoaderClass, "getCurrent", MethodType.methodType(fmlLoaderClass));
                    mh = MethodHandles.filterReturnValue(getCurrent, lookup.findVirtual(fmlLoaderClass, "getVersionInfo", MethodType.methodType(fmlLoaderClass)));
                }

                mh = MethodHandles.filterReturnValue(mh, lookup.findVirtual(versionInfoClass, "mcVersion", MethodType.methodType(String.class)));
                return (String) mh.invokeExact();
            } catch (Throwable e) {
                LOGGER.warn("Failed to get version info class on Neo platform for telemetry", e);
                return "";
            }
        }
    }

    private static final class UnknownPlatform extends Platform {
        @Override
        String getName() {
            return "unknown";
        }

        @Override
        String getMinecraftVersion() {
            return "";
        }
    }
}
