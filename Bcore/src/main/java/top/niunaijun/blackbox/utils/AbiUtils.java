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
        
        if (BlackBoxCore.is64Bit()) {
            // On 64-bit host (x86_64 or aarch64): Accept all architectures
            // - 64-bit apps run natively or via QEMU
            // - 32-bit apps run natively (compatibility) or via QEMU
            return true;
        } else {
            // On 32-bit host: Accept only 32-bit apps, reject 64-bit-only apps
            // - 32-bit hosts can run 32-bit apps (native or QEMU for cross-arch)
            // - 32-bit hosts CANNOT run 64-bit apps (no 64-bit QEMU on 32-bit host)
            if (has32Bit) {
                return true; // Accept if app has 32-bit libraries
            }
            if (has64Bit && !has32Bit) {
                return false; // Reject if app only has 64-bit libraries
            }
            return false; // Reject if app only has 64-bit libraries
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
                } else if (name.startsWith("lib/x86_64")) {
                    mLibs.add("x86_64");
                } else if (name.startsWith("lib/x86")) {
                    mLibs.add("x86");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(zipFile);
        }
    }

    public boolean is64Bit() {
        return mLibs.contains("arm64-v8a") || mLibs.contains("x86_64");
    }

    public boolean is32Bit() {
        return mLibs.contains("armeabi") || mLibs.contains("armeabi-v7a") || mLibs.contains("x86");
    }

    public boolean isEmptyAib() {
        return mLibs.isEmpty();
    }

    public Set<String> getAbiList() {
        return new HashSet<>(mLibs);
    }

    public String getAbiString() {
        if (mLibs.isEmpty()) {
            return "none";
        }
        return String.join(", ", mLibs);
    }
}
