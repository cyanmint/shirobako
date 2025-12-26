package top.niunaijun.blackbox.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import top.niunaijun.blackbox.core.QemuManager;


/**
 * updated by alex5402 on 2/24/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * TFNQw5HgWUS33Ke1eNmSFTwoQySGU7XNsK (USDT TRC20)
 */
public class NativeUtils {
    public static final String TAG = "VirtualM";

    public static void copyNativeLib(File apk, File nativeLibDir) throws Exception {
        long startTime = System.currentTimeMillis();
        if (!nativeLibDir.exists()) {
            nativeLibDir.mkdirs();
        }
        try (ZipFile zipfile = new ZipFile(apk.getAbsolutePath())) {
            // Try native architecture first
            if (findAndCopyNativeLib(zipfile, Build.CPU_ABI, nativeLibDir)) {
                return;
            }
            
            // Try all supported ABIs with QEMU fallback
            QemuManager qemuManager = QemuManager.getInstance();
            if (qemuManager.isInitialized()) {
                Set<String> nativeAbis = qemuManager.getNativeSupportedAbis();
                for (String abi : nativeAbis) {
                    if (findAndCopyNativeLib(zipfile, abi, nativeLibDir)) {
                        return;
                    }
                }
                
                // Try emulated ABIs (with QEMU support)
                Set<String> emulatedAbis = qemuManager.getEmulatedAbis();
                for (String abi : emulatedAbis) {
                    if (qemuManager.isQemuAvailable(abi)) {
                        if (findAndCopyNativeLib(zipfile, abi, nativeLibDir)) {
                            Log.i(TAG, "Will use QEMU for " + abi + " libraries");
                            return;
                        }
                    }
                }
            }

            // Fallback to armeabi for compatibility
            findAndCopyNativeLib(zipfile, "armeabi", nativeLibDir);
        } finally {
            Log.d(TAG, "Done! +" + (System.currentTimeMillis() - startTime) + "ms");
        }
    }


    private static boolean findAndCopyNativeLib(ZipFile zipfile, String cpuArch, File nativeLibDir) throws Exception {
        Log.d(TAG, "Try to copy plugin's cup arch: " + cpuArch);
        boolean findLib = false;
        boolean findSo = false;
        byte buffer[] = null;
        String libPrefix = "lib/" + cpuArch + "/";
        ZipEntry entry;
        Enumeration e = zipfile.entries();

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();
            if (!findLib && !entryName.startsWith("lib/")) {
                continue;
            }
            findLib = true;
            if (!entryName.endsWith(".so") || !entryName.startsWith(libPrefix)) {
                continue;
            }

            if (buffer == null) {
                findSo = true;
                Log.d(TAG, "Found plugin's cup arch dir: " + cpuArch);
                buffer = new byte[8192];
            }

            String libName = entryName.substring(entryName.lastIndexOf('/') + 1);
            Log.d(TAG, "verify so " + libName);
//            File abiDir = new File(nativeLibDir, cpuArch);
//            if (!abiDir.exists()) {
//                abiDir.mkdirs();
//            }

            File libFile = new File(nativeLibDir, libName);
            if (libFile.exists() && libFile.length() == entry.getSize()) {
                Log.d(TAG, libName + " skip copy");
                continue;
            }
            FileOutputStream fos = new FileOutputStream(libFile);
            Log.d(TAG, "copy so " + entry.getName() + " of " + cpuArch);
            copySo(buffer, zipfile.getInputStream(entry), fos);
        }

        if (!findLib) {
            Log.d(TAG, "Fast skip all!");
            return true;
        }

        return findSo;
    }

    private static void copySo(byte[] buffer, InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;

        while ((count = bufferedInput.read(buffer)) > 0) {
            bufferedOutput.write(buffer, 0, count);
        }
        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }
}
