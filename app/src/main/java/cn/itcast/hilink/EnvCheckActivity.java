package cn.itcast.hilink;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EnvCheckActivity extends AppCompatActivity {
    private Button btnRootCheck, btnVmCheck, btnVulkanCheck;
    private TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_env_check);

        btnRootCheck = findViewById(R.id.btn_root_check);
        btnVmCheck = findViewById(R.id.btn_vm_check);
        btnVulkanCheck = findViewById(R.id.btn_vulkan_check);
        tvLog = findViewById(R.id.tv_log);

        btnRootCheck.setOnClickListener(v -> {
            tvLog.setText("");
            appendLog("Magisk/Xposed 检测: " + (checkMagiskXposed() ? "存在" : "不存在"));
            appendLog("Frida进程检测: " + (checkFridaXposed() ? "发现 Frida 或 Xposed 进程" : "未发现 Frida 或 Xposed 进程"));
            appendLog("Frida注入器检测: " + (checkFridaInjector() ? "发现 Frida 注入" : "未发现"));
            appendLog("Bootloader 锁定状态: " + (checkBootloader() ? "锁定" : "解锁"));
            appendLog("调试模式检测: " + (checkDebugging() ? "开启" : "未开启"));
            appendLog("Root Cloak 检测: " + (checkRootCloak() ? "检测到Root Cloak类隐藏软件" : "未检测到隐藏软件"));

            String rootApps = checkRootApps();
            if (!rootApps.isEmpty()) {
                appendLog("检测到以下Root相关软件:\n" + rootApps);
                appendLog("推测Root概率: 高");
            } else {
                appendLog("未检测到常见Root相关软件");
                appendLog("包名反推测Root概率: 低");
            }

            appendLog("本地su存放位置检测结果: " + (checkRoot() ? "设备已Root\n" : "设备未Root\n"));
            appendLog("代理检测: " + (checkProxy() ? "通过代理连接" : "未通过代理连接"));
            appendLog("VPN检测: " + (checkVPN() ? "通过 VPN 连接" : "未通过 VPN 连接"));
            appendLog("SELinux 状态: " + (checkSelinux() ? "已启用" : "未启用"));
        });

        /*btnVmCheck.setOnClickListener(v -> {
            tvLog.setText("");
            String emulatorApps = checkEmulatorApps();
            if (!emulatorApps.isEmpty()) {
                appendLog("检测到以下模拟器相关软件:\n" + emulatorApps);
                appendLog("推测模拟器概率: 高");
            } else {
                appendLog("未检测到常见模拟器相关软件");
                appendLog("推测模拟器概率: 低");
            }
        });*/

        btnVmCheck.setOnClickListener(v -> {
            tvLog.setText("");
            appendLog("=== 模拟器环境检测 ===");

            // 1. 检测模拟器特征
            boolean hasEmulatorFeatures = checkIsEmulator();
            appendLog("\n[特征检测]");
            appendLog("模拟器特征匹配: " + (hasEmulatorFeatures ? "✅ 发现模拟器特征" : "❌ 未发现明显特征"));

            // 显示关键特征信息
            appendLog("设备指纹: " + Build.FINGERPRINT);
            appendLog("硬件信息: " + Build.HARDWARE);
            appendLog("产品型号: " + Build.PRODUCT);

            // 2. 检测模拟器应用
            String emulatorApps = checkEmulatorApps();
            appendLog("\n[软件检测]");
            if (!emulatorApps.isEmpty()) {
                appendLog("检测到模拟器相关应用:\n" + emulatorApps);
            } else {
                appendLog("未检测到常见模拟器应用");
            }

            // 3. 检测CPU信息
            appendLog("\n[CPU检测]");
            String cpuInfo = getCpuInfo();
            appendLog("CPU架构: " + Build.SUPPORTED_ABIS[0]);
            appendLog("CPU信息: " + (cpuInfo.isEmpty() ? "未知" : cpuInfo));

            // 4. 综合判断
            appendLog("\n[综合判断]");
            if (!emulatorApps.isEmpty() && hasEmulatorFeatures) {
                appendLog("⚠️ 高危: 检测到模拟器应用且存在硬件特征");
            } else if (!emulatorApps.isEmpty()) {
                appendLog("⚠️ 中危: 仅检测到模拟器应用");
            } else if (hasEmulatorFeatures) {
                appendLog("⚠️ 低危: 仅存在模拟器特征");
            } else {
                appendLog("✅ 安全: 未发现模拟器证据");
            }
        });





        btnVulkanCheck.setOnClickListener(v -> {
            tvLog.setText("");
            appendLog("=== Vulkan & 图形引擎检测 ===");

            // 1. 检测Vulkan支持
            boolean vulkanSupported = checkVulkanSupport();
            appendLog("\n[Vulkan支持检测]");
            appendLog("基础Vulkan支持: " + (vulkanSupported ? "✅ 支持" : "❌ 不支持"));

            // 2. 检测默认图形渲染引擎
            appendLog("\n[默认图形引擎]");
            String renderer = getDefaultRenderer();
            appendLog("当前渲染引擎: " + renderer);
            appendLog("是否使用Vulkan: " + (renderer.toLowerCase().contains("vulkan") ? "✅ 是" : "❌ 否"));

            // 3. 详细Vulkan信息
            if (vulkanSupported) {
                appendLog("\n[Vulkan详细信息]");
                appendLog("OpenGL ES版本: " + getOpenGLVersion());

                List<String> vulkanFiles = checkVulkanFiles();
                if (!vulkanFiles.isEmpty()) {
                    appendLog("\n检测到Vulkan相关文件:");
                    for (String file : vulkanFiles) {
                        appendLog(" - " + file);
                    }
                }

                // 4. 检测系统默认图形设置
                appendLog("\n[系统图形设置]");
                String hwuiRenderer = executeCommand("getprop debug.hwui.renderer");
                appendLog("HWUI渲染器: " + (hwuiRenderer.isEmpty() ? "默认" : hwuiRenderer));

                String angleRenderer = executeCommand("getprop persist.graphics.egl");
                appendLog("ANGLE后端: " + (angleRenderer.isEmpty() ? "未使用" : angleRenderer));
            }

            // 5. 性能建议
            appendLog("\n[建议]");
            if (vulkanSupported && renderer.toLowerCase().contains("vulkan")) {
                appendLog("✅ 设备正在使用Vulkan渲染，性能最佳");
            } else if (vulkanSupported) {
                appendLog("⚠️ 设备支持Vulkan但未默认使用，可尝试强制启用");
            } else {
                appendLog("❌ 设备不支持Vulkan，将使用OpenGL ES渲染");
            }
        });
    }

    // 新增方法：获取默认渲染引擎
    private String getDefaultRenderer() {
        try {
            // 方法1：通过系统属性获取
            String renderer = executeCommand("getprop ro.hwui.renderer");
            if (!renderer.isEmpty()) {
                return renderer;
            }

            // 方法2：通过dumpsys获取
            renderer = executeCommand("dumpsys SurfaceFlinger | grep 'GLES'");
            if (renderer.contains("GLES")) {
                return renderer.split(":")[1].trim();
            }

            // 方法3：通过OpenGL ES版本推断
            return "OpenGL ES " + getOpenGLVersion();
        } catch (Exception e) {
            return "未知";
        }
    }

    // 获取CPU信息
    private String getCpuInfo() {
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("model name") || line.contains("Hardware")) {
                    result.append(line.split(":")[1].trim()).append(" ");
                }
            }
            return result.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    // 检测Vulkan基础支持
    private boolean checkVulkanSupport() {
        // 方法1：检查系统属性
        String vulkanSupport = System.getProperty("ro.vulkan.support");
        if (vulkanSupport != null && vulkanSupport.equals("1")) {
            return true;
        }

        // 方法2：检查Vulkan库文件
        String[] vulkanLibPaths = {
                "/system/lib/libvulkan.so",
                "/system/lib64/libvulkan.so",
                "/vendor/lib/libvulkan.so",
                "/vendor/lib64/libvulkan.so"
        };

        for (String path : vulkanLibPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }

        // 方法3：检查Vulkan设备属性
        try {
            Process process = Runtime.getRuntime().exec("getprop ro.hardware.vulkan");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 获取OpenGL ES版本
    private String getOpenGLVersion() {
        return Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    // 获取GPU渲染器信息
    private String getGPURenderer() {
        try {
            Process process = Runtime.getRuntime().exec("getprop ro.hardware.egl");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String renderer = reader.readLine();
            return renderer != null ? renderer : "未知";
        } catch (IOException e) {
            return "未知";
        }
    }

    // 检查Vulkan相关文件
    private List<String> checkVulkanFiles() {
        List<String> vulkanFiles = new ArrayList<>();
        String[] paths = {
                "/system/lib/libvulkan.so",
                "/system/lib64/libvulkan.so",
                "/vendor/lib/libvulkan.so",
                "/vendor/lib64/libvulkan.so",
                "/vendor/lib/hw/vulkan.*.so",
                "/vendor/lib64/hw/vulkan.*.so"
        };

        for (String path : paths) {
            if (new File(path.replace("*", "")).exists()) {
                vulkanFiles.add(path);
            } else if (path.contains("*")) {
                // 处理通配符路径
                File dir = new File(path.substring(0, path.lastIndexOf("/")));
                String prefix = path.substring(path.lastIndexOf("/") + 1).replace("*", "");
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((dir1, name) -> name.startsWith(prefix));
                    if (files != null) {
                        for (File file : files) {
                            vulkanFiles.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return vulkanFiles;
    }

    // 检查Vulkan设备属性
    private void checkVulkanDeviceProperties() {
        try {
            // 通过系统属性获取部分信息
            String vulkanVersion = System.getProperty("ro.vulkan.version");
            if (vulkanVersion != null) {
                appendLog("Vulkan版本: " + vulkanVersion);
            }

            // 尝试获取GPU信息
            String gpuVendor = executeCommand("getprop ro.hardware.vulkan.vendor");
            String gpuRenderer = executeCommand("getprop ro.hardware.vulkan.renderer");

            if (gpuVendor != null && !gpuVendor.isEmpty()) {
                appendLog("GPU供应商: " + gpuVendor);
            }
            if (gpuRenderer != null && !gpuRenderer.isEmpty()) {
                appendLog("GPU渲染器: " + gpuRenderer);
            }

            // 检查Vulkan扩展
            String vulkanExtensions = executeCommand("dumpsys SurfaceFlinger | grep Vulkan");
            if (vulkanExtensions != null && !vulkanExtensions.isEmpty()) {
                appendLog("\nVulkan扩展支持:");
                appendLog(vulkanExtensions);
            }
        } catch (Exception e) {
            appendLog("\n无法获取详细Vulkan信息: " + e.getMessage());
        }
    }

    // 执行shell命令
    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString().trim();
        } catch (IOException e) {
            return null;
        }
    }

    private void appendLog(String message) {
        tvLog.append(message + "\n");
    }
    // Root检测
    private boolean checkRoot() {
        String[] paths = {
                "/system/bin/su", "/system/xbin/su", "/system/sbin/su",
                "/sbin/su", "/vendor/bin/su", "/su/bin/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = in.readLine();
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // 检查已安装的Root相关应用
    private String checkRootApps() {
        String[] rootAppPackages = {
                "com.topjohnwu.magisk",
                "eu.chainfire.supersu",
                "com.noshufou.android.su",
                "com.kingroot.kinguser",
                "com.koushikdutta.superuser",
                "com.thirdparty.superuser",
                "com.yellowes.su",
                "com.zachspong.temprootremovejb",
                "com.ramdroid.appquarantine",
                "com.devadvance.rootcloak",
                "io.github.huskydg.magisk",
                "io.github.vvb2060.magisk",
                "io.github.vvb2060.magiskdetector", //magisk检测应用
                "org.lsposed.manager",
                "com.houvven.guise",
                "com.coderstory.toolkit",
                "com.bluestacks.home",       // 蓝叠模拟器
                "com.bignox.app",            // 夜神模拟器
                "com.microvirt.download",    // 逍遥模拟器
                "com.netease.mumu",          // 网易MuMu模拟器
                "com.ldmnq.launcher",        // LDPlayer模拟器
                "com.mumu.launcher"          // 其他模拟器
        };
        StringBuilder foundApps = new StringBuilder();
        PackageManager pm = getPackageManager();
        for (String packageName : rootAppPackages) {
            try {
                pm.getPackageInfo(packageName, 0);
                foundApps.append(packageName).append("\n");
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return foundApps.toString();
    }
    // 检查Magisk或Xposed
    private boolean checkMagiskXposed() {
        String[] paths = {
                "/sbin/.magisk", "/system/xposed", "/system/framework/XposedBridge.jar"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }
    // 检测Frida或Xposed进程
    private boolean checkFridaXposed() {
        String[] suspiciousProcesses = {"frida-server", "xposed"};
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                for (String keyword : suspiciousProcesses) {
                    if (line.contains(keyword)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 检测Frida注入器
    private boolean checkFridaInjector() {
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/self/maps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frida") || line.contains("gum-js-loop") || line.contains("libfrida")) {
                    return true;
                }
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return false;
    }
    // 代理检测
    private boolean checkProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        return proxyHost != null && !proxyHost.isEmpty();
    }
    // VPN检测
    private boolean checkVPN() {
        try {
            java.net.NetworkInterface vpn = java.net.NetworkInterface.getByName("tun0");
            if (vpn != null && vpn.isUp()) {
                return true;
            }
            vpn = java.net.NetworkInterface.getByName("ppp0");
            if (vpn != null && vpn.isUp()) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    // 检查SELinux状态
    private boolean checkSelinux() {
        try {
            Process process = Runtime.getRuntime().exec("getenforce");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            return result != null && result.equalsIgnoreCase("Enforcing");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 检查Bootloader是否锁定
    private boolean checkBootloader() {
        try {
            String bootloader = Build.BOOTLOADER;
            return !(bootloader == null || bootloader.toLowerCase().contains("unlocked"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    // 检查USB调试
    private boolean checkDebugging() {
        int adb = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        return adb == 1;
    }
    // 检查Root Cloak等隐藏工具
    private boolean checkRootCloak() {
        String[] hideRootApps = {
                "com.devadvance.rootcloak",
                "com.devadvance.rootcloakplus",
                "com.formyhm.hideroot",
                "com.formyhm.magiskhide"
        };
        PackageManager pm = getPackageManager();
        for (String packageName : hideRootApps) {
            try {
                pm.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return false;
    }

    //增强版模拟器检测
    private boolean checkIsEmulator() {
        // Build特征检测
        boolean buildCheck = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("vbox")
                || Build.FINGERPRINT.contains("test-keys")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.equals("google_sdk")
                || Build.HARDWARE.equals("goldfish")
                || Build.HARDWARE.equals("ranchu")
                || Build.HARDWARE.contains("vbox")
                || Build.SERIAL == null || Build.SERIAL.equals("unknown");

        // 系统属性检测
        boolean propertyCheck = false;
        try {
            propertyCheck = "1".equals(executeCommand("getprop ro.boot.qemu"));
        } catch (Exception e) {
            // 忽略异常
        }

        // CPU特征检测
        boolean cpuCheck = false;
        String cpuInfo = getCpuInfo().toLowerCase();
        if (cpuInfo.contains("intel") || cpuInfo.contains("amd")) {
            // 移动设备通常使用ARM处理器
            cpuCheck = true;
        }

        return buildCheck || propertyCheck || cpuCheck;
    }


    // 检查已安装的模拟器相关应用
    private String checkEmulatorApps() {
        String[] emulatorAppPackages = {
                "com.bluestacks.home",      // 蓝叠模拟器
                "com.bignox.app",            // 夜神模拟器
                "com.microvirt.download",    // 逍遥模拟器
                "com.netease.mumu",          // 网易MuMu模拟器
                "com.ldmnq.launcher",         // LDPlayer模拟器
                "com.genymotion.superuser",  // Genymotion模拟器
                "com.vphone.launcher",        // vPhone模拟器
                "com.mumu.launcher",          //mumu
                "com.android.flysilkworm"     // 其他模拟器
        };

        StringBuilder foundApps = new StringBuilder();
        PackageManager pm = getPackageManager();
        for (String packageName : emulatorAppPackages) {
            try {
                pm.getPackageInfo(packageName, 0);
                foundApps.append(packageName).append("\n");
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return foundApps.toString();
    }
}