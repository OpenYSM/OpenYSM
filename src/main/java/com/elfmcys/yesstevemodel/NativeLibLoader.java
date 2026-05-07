package com.elfmcys.yesstevemodel;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class NativeLibLoader {
    private static boolean available = false;
    private static boolean loaded = false;
    private static boolean isAndroid = false;
    private static ErrorState lastError = null;

    private enum TargetPlatform {
        WINDOWS_X64("windows-x64", "ysm-core.dll"),
        WINDOWS_X86("windows-x86", "ysm-core.dll"),
        LINUX_X64("linux-x64", "libysm-core.so"),
        MACOS_X64("macos-x64", "libysm-core.dylib"),
        MACOS_ARM64("macos-arm64", "libysm-core.dylib"),
        ANDROID_ARM64("android-arm64", "libysm-core.so");

        final String resDir;
        final String fileName;

        TargetPlatform(String resDir, String fileName) {
            this.resDir = resDir;
            this.fileName = fileName;
        }

        String getResourcePath() {
            return "/META-INF/native/" + resDir + "/" + fileName;
        }
    }

    private enum LibcType {UNSUPPORTED, GNU, BIONIC}

    private record ErrorState(Component component, String key, Object[] args, String logMsg) {
    }

    public static void init() throws IOException {
        String path = System.getenv("YSM_CORE_LIB");
        if (StringUtil.isNullOrEmpty(path)) {
            path = extractAndGetLibPath();
        }

        if (path != null && loadNativeLib(path)) {
            loaded = true;
        }
        available = true;
    }

    private static @Nullable String extractAndGetLibPath() throws IOException {
        TargetPlatform platform = resolvePlatform();
        if (platform == null) return null;

        Path storageDir = getDefaultStorageDir(platform);
        if (platform == TargetPlatform.ANDROID_ARM64) {
            String androidRuntime = System.getenv("MOD_ANDROID_RUNTIME");
            if (androidRuntime == null) {
                setUnsupportedLauncherError();
                return null;
            }
            isAndroid = true;
            storageDir = Path.of(androidRuntime);
        }

        byte[] data = readResource(platform.getResourcePath());
        if (data == null) {
            setUnsatisfiedBuildError();
            return null;
        }

        Path targetFile = ensureDirectory(storageDir).resolve(platform.fileName).toAbsolutePath().normalize();
        String finalPath = targetFile.toString();

        writeIfChanged(finalPath, data);
        YesSteveModel.LOGGER.info("[YSM] Native library extracted to {}", finalPath);
        return finalPath;
    }

    private static Path getDefaultStorageDir(TargetPlatform platform) {
        return FMLPaths.GAMEDIR.get().resolve("ysm_native").resolve(platform.resDir);
    }

    private static @Nullable TargetPlatform resolvePlatform() {
        boolean isX86_64 = SystemUtils.OS_ARCH.equals("amd64") || SystemUtils.OS_ARCH.equals("x86_64");
        boolean isAarch64 = SystemUtils.OS_ARCH.equals("aarch64");

        if (SystemUtils.IS_OS_WINDOWS) {
            return isX86_64 ? TargetPlatform.WINDOWS_X64 : TargetPlatform.WINDOWS_X86;
        }

        if (SystemUtils.IS_OS_LINUX) {
            LibcType libc = detectLibcType();
            if (libc == LibcType.GNU) return isX86_64 ? TargetPlatform.LINUX_X64 : null;
            if (libc == LibcType.BIONIC) return isAarch64 ? TargetPlatform.ANDROID_ARM64 : null;
            setUnsupportedPlatformError("Linux (Unknown Libc)");
            return null;
        }

        if (SystemUtils.IS_OS_MAC) {
            if (isAarch64) return TargetPlatform.MACOS_ARM64;
            if (isX86_64) return TargetPlatform.MACOS_X64;
            setUnsupportedPlatformError("macOS (Unsupported Architecture: " + SystemUtils.OS_ARCH + ")");
            return null;
        }

        setUnsupportedPlatformError(SystemUtils.OS_NAME + " " + SystemUtils.OS_ARCH);
        return null;
    }

    private static boolean loadNativeLib(String path) {
        try {
            System.load(path);
            return true;
        } catch (Throwable th) {
            YesSteveModel.LOGGER.error("Failed to load native lib: " + path, th);
            setUnsatisfiedRuntimeError(th.getMessage());
            return false;
        }
    }

    private static void writeIfChanged(String path, byte[] data) throws IOException {
        File file = new File(path);
        if (file.exists() && file.length() == data.length) {
            byte[] existing = FileUtils.readFileToByteArray(file);
            if (Arrays.equals(data, existing)) return;
        }
        FileUtils.writeByteArrayToFile(file, data, false);
    }

    private static byte[] readResource(String path) throws IOException {
        URL url = YesSteveModel.class.getResource(path);
        if (url == null) return null;
        try (InputStream is = url.openStream()) {
            return IOUtils.toByteArray(is);
        }
    }

    private static Path ensureDirectory(Path path) {
        try {
            if (!Files.isDirectory(path)) Files.createDirectories(path);
            return path;
        } catch (Throwable th) {
            return FMLPaths.CONFIGDIR.get().resolve(YesSteveModel.MOD_ID).resolve("cache");
        }
    }

    private static LibcType detectLibcType() {
        try {
            NativeLibrary libc = NativeLibrary.getInstance(Platform.C_LIBRARY_NAME);
            if (libc != null) {
                if (hasFunction(libc, "android_set_abort_message")) return LibcType.BIONIC;
                if (hasFunction(libc, "gnu_get_libc_version")) return LibcType.GNU;
            }
        } catch (Throwable ignored) {
        }
        return LibcType.UNSUPPORTED;
    }

    private static boolean hasFunction(NativeLibrary lib, String name) {
        try {
            return lib.getFunction(name) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    private static void setUnsupportedPlatformError(@Nullable String detail) {
        String info = detail != null ? detail : SystemUtils.OS_NAME + " " + SystemUtils.OS_ARCH;
        lastError = new ErrorState(Component.translatable("error.yes_steve_model.unsupported_platform", info), "error.yes_steve_model.unsupported_platform_ext", new Object[]{info}, "[YSM] Unsupported platform: " + info);
    }

    private static void setUnsatisfiedRuntimeError(@NotNull String msg) {
        lastError = new ErrorState(Component.translatable("error.yes_steve_model.unsatisfied_runtime_env", msg), "error.yes_steve_model.unsatisfied_runtime_env_ext", new Object[]{msg}, "[YSM] Runtime error: " + msg);
    }

    private static void setUnsatisfiedBuildError() {
        String info = SystemUtils.OS_NAME + " " + SystemUtils.OS_ARCH;
        lastError = new ErrorState(Component.translatable("error.yes_steve_model.unsatisfied_build", info), "error.yes_steve_model.unsatisfied_build_ext", new Object[]{info}, "[YSM] No build for platform: " + info);
    }

    private static void setUnsupportedLauncherError() {
        String fcl = System.getenv("FCL_VERSION_CODE");
        if (StringUtils.isNotBlank(fcl)) {
            lastError = createLauncherError("FCL", "1.2.6.7");
            return;
        }
        String zalith = System.getenv("ZALITH_VERSION_CODE");
        if (StringUtils.isNotBlank(zalith)) {
            int ver = Integer.parseInt(zalith);
            lastError = (ver < 190000) ? createLauncherError("Zalith 1", "1.4.1.1") : createLauncherError("Zalith 2", "2.0.0_beta-20251118a");
            return;
        }
        lastError = new ErrorState(Component.translatable("error.yes_steve_model.unsupported_launcher"), null, null, "[YSM] Unsupported Launcher");
    }

    private static ErrorState createLauncherError(String name, String minVer) {
        return new ErrorState(Component.translatable("error.yes_steve_model.old_launcher", name, minVer), "error.yes_steve_model.old_launcher_ext", new Object[]{name, minVer}, "[YSM] Old launcher version: " + name);
    }

    public static boolean isAvailable() {
        return available;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isOnAndroid() {
        return isAndroid;
    }

    public static Component getErrorComponent() {
        return lastError != null ? lastError.component : null;
    }

    public static String getErrorMessage() {
        return lastError != null ? lastError.logMsg : null;
    }

    public static ModLoadingWarning createLoadingWarning() {
        if (lastError == null) return null;
        return new ModLoadingWarning(ModList.get().getModFileById(YesSteveModel.MOD_ID).getFile().getModInfos().get(0), ModLoadingStage.SIDED_SETUP, lastError.key, lastError.args);
    }
}
