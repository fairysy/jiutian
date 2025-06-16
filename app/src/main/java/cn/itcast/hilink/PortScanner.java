package cn.itcast.hilink;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PortScanner {
    private static final int THREAD_POOL_SIZE = 100; // 线程池大小
    private static final int TIMEOUT_MS = 1000; // 连接超时时间

    // 扫描回调接口
    public interface ScanCallback {
        void onScanning(int currentPort);  // 当前扫描的端口
        void onPortFound(int openPort);    // 发现开放端口
        void onScanComplete(List<Integer> openPorts); // 扫描完成
    }

    // 多线程扫描设备端口
    public static void scanDevicePorts(String ipAddress, int startPort, int endPort, ScanCallback callback) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger currentPort = new AtomicInteger(startPort);

        // 提交扫描任务
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executor.submit(() -> {
                while (true) {
                    int port = currentPort.getAndIncrement();
                    if (port > endPort) {
                        break;
                    }

                    // 更新扫描进度
                    if (callback != null) {
                        callback.onScanning(port);
                    }

                    // 检查端口
                    if (isPortOpen(ipAddress, port)) {
                        openPorts.add(port);
                        if (callback != null) {
                            callback.onPortFound(port);
                        }
                    }
                }
            });
        }

        // 关闭线程池并等待完成
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 扫描完成回调
        if (callback != null) {
            callback.onScanComplete(openPorts);
        }
    }

    // 检测端口是否开放
    private static boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(ip, port), TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}