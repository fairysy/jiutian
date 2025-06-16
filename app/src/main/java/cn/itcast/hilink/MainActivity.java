package cn.itcast.hilink;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements DeviceAdapter.OnConnectClickListener {

    private DeviceAdapter deviceAdapter; // 自定义适配器
    private List<Device> deviceList; // 存储设备的列表
    private ExecutorService executorService; // 用于并发扫描设备
    private static final String TAG = "MainActivity"; // 日志标签
    private String localIpAddress; // 本机 IP 地址
    private String gatewayIp; // 网关 IP 地址

    private static final List<Integer> COMMON_PORTS = new ArrayList<>();

    static {
        for (int i = 1; i <= 19999; i++) {
            COMMON_PORTS.add(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView deviceRecyclerView = findViewById(R.id.deviceRecyclerView);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(deviceList, this);
        deviceRecyclerView.setAdapter(deviceAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        executorService = Executors.newFixedThreadPool(20);
        localIpAddress = getLocalIpAddress();
        gatewayIp = getGatewayIp();

        boolean autoSearch = getIntent().getBooleanExtra("auto_search", false);
        if (autoSearch) {
            searchDevices(null);
        }
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int ipAddress = dhcpInfo.ipAddress;
            return String.format("%d.%d.%d.%d",
                    (ipAddress & 0xFF), (ipAddress >> 8 & 0xFF),
                    (ipAddress >> 16 & 0xFF), (ipAddress >> 24 & 0xFF));
        }
        return null;
    }

    private String getGatewayIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int gateway = dhcpInfo.gateway;
            return String.format("%d.%d.%d.%d",
                    (gateway & 0xFF), (gateway >> 8 & 0xFF),
                    (gateway >> 16 & 0xFF), (gateway >> 24 & 0xFF));
        }
        return null;
    }

    public void searchDevices(View view) {
        Log.d(TAG, "开始搜索局域网设备...");

        if (!isWifiEnabled()) {
            Toast.makeText(this, "请确保 Wi-Fi 已开启并连接到网络", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gatewayIp == null) {
            Toast.makeText(this, "无法获取网关 IP，请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        String subnet = gatewayIp.substring(0, gatewayIp.lastIndexOf('.') + 1);
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();

        for (int i = 1; i <= 254; i++) {
            final String ip = subnet + i;
            executorService.submit(() -> scanDevice(ip));
        }
    }

    private boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.isWifiEnabled();
    }

    private void scanDevice(String ip) {
        try {
            long startTime = SystemClock.elapsedRealtime();
            InetAddress inetAddress = InetAddress.getByName(ip);
            boolean isOnline = inetAddress.isReachable(1000);
            long responseTime = SystemClock.elapsedRealtime() - startTime;

            if (isOnline) {
                String deviceType = getDeviceType(ip);
                String macAddress = getMacAddress(ip);
                String deviceName = ip.equals(localIpAddress) ? "本机" : "设备 " + ip;

                if (ip.equals(gatewayIp)) {
                    deviceName = "网关 (" + ip + ")";
                }

                final Device device = new Device(deviceName, ip, deviceType, macAddress, true, responseTime, new ArrayList<>());
                runOnUiThread(() -> {
                    deviceList.add(device);
                    deviceAdapter.notifyDataSetChanged();
                });
            }
        } catch (IOException e) {
            Log.e(TAG, "扫描 IP " + ip + " 时发生错误", e);
        }
    }

    private String getDeviceType(String ip) {
        if (ip.endsWith(".1")) {
            return "路由器";
        } else if (ip.endsWith(".2") || ip.endsWith(".3")) {
            return "电脑";
        } else if (ip.endsWith(".4") || ip.endsWith(".5")) {
            return "手机";
        }
        return "未知设备";
    }

    private String getMacAddress(String ip) {
        try {
            String command = "arp -a " + ip;
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(ip)) {
                    String[] tokens = line.split(" ");
                    return tokens[tokens.length - 1];
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "获取 MAC 地址失败", e);
        }
        return "未知 MAC 地址";
    }

    @Override
    public void onConnectClick(String ip, int position) {
        Device device = deviceList.get(position);
        if (device.isScanning()) {
            Toast.makeText(this, "该设备正在扫描中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "开始扫描 " + ip + " 的端口", Toast.LENGTH_SHORT).show();

        // 更新设备状态为扫描中
        device.setScanning(true);
        device.setScanProgress(0);
        device.setScannedPorts(0);
        deviceAdapter.notifyItemChanged(position);

        // 提交扫描任务
        executorService.submit(() -> scanPorts(ip, position));
    }

    private void scanPorts(String ip, int position) {
        Device device = deviceList.get(position);
        List<Integer> openPorts = new ArrayList<>();
        ExecutorService portExecutor = Executors.newFixedThreadPool(200);// 核心线程数
        List<Future<?>> futures = new ArrayList<>();

        final int totalPorts = 65535;
        AtomicInteger scannedCount = new AtomicInteger(0);
        AtomicInteger lastProgress = new AtomicInteger(0);

        // 创建UI更新Handler
        Handler uiHandler = new Handler(Looper.getMainLooper());

        for (int port = 1; port <= totalPorts; port++) {
            final int currentPort = port;
            futures.add(portExecutor.submit(() -> {
                try {
                    if (isPortOpen(ip, currentPort)) {
                        synchronized (openPorts) {
                            openPorts.add(currentPort);
                        }
                    }

                    // 更新扫描计数
                    int count = scannedCount.incrementAndGet();
                    int progress = (count * 100) / totalPorts;

                    // 避免频繁更新UI，每1%更新一次
                    if (progress > lastProgress.get()) {
                        lastProgress.set(progress);

                        // 更新设备状态
                        device.setScannedPorts(count);
                        device.setScanProgress(progress);

                        // 在UI线程更新视图
                        uiHandler.post(() -> {
                            // 检查设备是否还在列表中
                            if (position < deviceList.size() &&
                                    deviceList.get(position).getIp().equals(ip)) {
                                deviceAdapter.notifyItemChanged(position);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "扫描端口 " + currentPort + " 时出错", e);
                }
            }));
        }

        portExecutor.shutdown();
        try {
            portExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "端口扫描中断", e);
        }

        // 扫描完成
        runOnUiThread(() -> {
            device.setScanning(false);
            device.setOpenPorts(openPorts);
            deviceAdapter.notifyItemChanged(position);

            Toast.makeText(MainActivity.this,
                    "扫描完成: " + openPorts.size() + "个开放端口",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}