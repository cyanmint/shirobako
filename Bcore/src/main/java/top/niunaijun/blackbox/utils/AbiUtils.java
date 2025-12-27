package top.niunaijun.blackbox.utils;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.QemuManager;

/**
 * updated by alex5402 on 3/2/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * TFNQw5HgWUS33Ke1eNmSFTwoQySGU7XNsK (USDT TRC20)
 */
public class AbiUtils {
    private final Set<String> mLibs = new HashSet<>();
    private static final Map<File, AbiUtils> sAbiUtilsMap = new HashMap<>();

    public static boolean isSupport(File apkFile) {
        AbiUtils abiUtils = sAbiUtilsMap.get(apkFile);
        if (abiUtils == null) {
            abiUtils = new AbiUtils(apkFile);
            sAbiUtilsMap.put(apkFile, abiUtils);
        }
        if (abiUtils.isEmptyAib()) {
            return true;
        }

        // Check if app has 64-bit libraries
        boolean has64Bit = abiUtils.is64Bit();
        // Check if app has 32-bit libraries
        boolean has32Bit = abiUtils.is32Bit();
        
        // Get QEMU manager to check for emulation support
        QemuManager qemuManager = QemuManager.getInstance();
        boolean qemuInitialized = qemuManager.isInitialized();
        
        if (BlackBoxCore.is64Bit()) {
            // On 64-bit device: accept 64-bit apps natively
            if (has64Bit) {
                return true;
            }
            // On 64-bit device: accept 32-bit apps if QEMU is available for armeabi-v7a
            if (has32Bit && qemuInitialized && qemuManager.isQemuAvailable("armeabi-v7a")) {
                return true;
            }
            // Fallback: if we have armeabi-v7a native libraries built, we can run 32-bit apps
            // (This handles the case where QEMU isn't initialized yet during install)
            if (has32Bit) {
                return true; // We now build armeabi-v7a libraries with Dobby32
            }
            return false;
        } else {
            // On 32-bit device: only accept 32-bit apps
            return has32Bit;
        }
    }

    public AbiUtils(File apkFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith("lib/arm64-v8a")) {
                    mLibs.add("arm64-v8a");
                } else if (name.startsWith("lib/armeabi")) {
                    mLibs.add("armeabi");
                } else if (name.startsWith("lib/armeabi-v7a")) {
                    mLibs.add("armeabi-v7a");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(zipFile);
        }
    }

    public boolean is64Bit() {
        return mLibs.contains("arm64-v8a");
    }

    public boolean is32Bit() {
        return mLibs.contains("armeabi") || mLibs.contains("armeabi-v7a");
    }

    public boolean isEmptyAib() {
        return mLibs.isEmpty();
    }
}
