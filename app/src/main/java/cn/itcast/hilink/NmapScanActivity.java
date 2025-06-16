package cn.itcast.hilink;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class NmapScanActivity extends AppCompatActivity {

    private EditText etTargetIp;
    private Button btnStartScan, btnCancelScan;
    private TextView tvScanResult;
    private ScrollView scrollView;

    private ExecutorService executor;
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private Handler handler = new Handler(Looper.getMainLooper());

    // 常用 + 漏洞相关端口（100个左右）
    private final int[] commonPorts = new int[]{
            20, 21, 22, 23, 25, 53, 67, 68, 69, 80,
            110, 111, 123, 135, 137, 138, 139, 143, 161, 162,
            179, 389, 443, 445, 465, 514, 515, 587, 631, 636,
            993, 995, 1080, 1194, 1433, 1521, 1723, 2049, 2082, 2083,
            2086, 2087, 2095, 2096, 3306, 3389, 5060, 5432, 5900, 6000,
            8080, 8443, 8888, 9000, 9090, 10000, 32768, 49152, 49153, 49154,
            6379, 7001, 11211, 9200, 2375, 27017
    };

    // 用于存开放端口，辅助OS识别
    private final List<Integer> openPorts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(generateLayout());

        btnStartScan.setOnClickListener(v -> {
            if (isScanning.get()) {
                Toast.makeText(this, "扫描进行中，请先取消", Toast.LENGTH_SHORT).show();
                return;
            }
            String targetIp = etTargetIp.getText().toString().trim();
            if (targetIp.isEmpty()) {
                Toast.makeText(this, "请输入目标IP", Toast.LENGTH_SHORT).show();
                return;
            }
            startPortScan(targetIp);
        });

        btnCancelScan.setOnClickListener(v -> cancelScan());
    }

    private void startPortScan(String targetIp) {
        isScanning.set(true);
        openPorts.clear();
        btnStartScan.setEnabled(false);
        btnCancelScan.setEnabled(true);
        tvScanResult.setText("");

        executor = Executors.newFixedThreadPool(50);
        Toast.makeText(this, "开始扫描: " + targetIp, Toast.LENGTH_SHORT).show();

        for (int port : commonPorts) {
            if (!isScanning.get()) break;
            final int currentPort = port;
            executor.submit(() -> {
                if (!isScanning.get()) return;

                long startTime = System.currentTimeMillis();
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(targetIp, currentPort), 200);
                    socket.setSoTimeout(200);

                    long responseTime = System.currentTimeMillis() - startTime;

                    // 读取 banner
                    String banner = "";
                    try {
                        byte[] buffer = new byte[100];
                        int readLen = socket.getInputStream().read(buffer);
                        if (readLen > 0) {
                            banner = new String(buffer, 0, readLen).replaceAll("[\\r\\n]+", " ").trim();
                            if (banner.length() > 60) banner = banner.substring(0, 60) + "...";
                        }
                    } catch (IOException ignored) {}

                    synchronized (openPorts) {
                        openPorts.add(currentPort);
                    }

                    String service = getServiceName(currentPort);
                    String risk = getRiskTip(currentPort);

                    String result = "端口 " + currentPort + " - 开放 " + service + " (响应时间: " + responseTime + " ms)";
                    if (!banner.isEmpty()) {
                        result += " [Banner: " + banner + "]";
                    }
                    if (!risk.isEmpty()) {
                        result += " " + risk;
                    }
                    result += "\n";
                    postResult(result, true);

                } catch (IOException e) {
                    postResult("端口 " + currentPort + " - 关闭\n", false);
                }
            });
        }

        executor.shutdown();

        new Thread(() -> {
            try {
                while (!executor.isTerminated()) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                Toast.makeText(this, "扫描结束", Toast.LENGTH_SHORT).show();
                btnStartScan.setEnabled(true);
                btnCancelScan.setEnabled(false);
                isScanning.set(false);

                String osGuess = guessOperatingSystem(openPorts);
                tvScanResult.append("\n" + osGuess + "\n");
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            });
        }).start();
    }

    private void cancelScan() {
        if (isScanning.get() && executor != null) {
            isScanning.set(false);
            executor.shutdownNow();
            Toast.makeText(this, "扫描已取消", Toast.LENGTH_SHORT).show();
            btnStartScan.setEnabled(true);
            btnCancelScan.setEnabled(false);
        }
    }

    private void postResult(String text, boolean isOpen) {
        handler.post(() -> {
            Spannable spannable = new SpannableString(text);
            if (isOpen) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#008000")), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 绿色高亮
            }
            tvScanResult.append(spannable);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private String getServiceName(int port) {
        switch (port) {
            case 21: return "[FTP]";
            case 22: return "[SSH]";
            case 23: return "[Telnet]";
            case 25: return "[SMTP]";
            case 53: return "[DNS]";
            case 80: return "[HTTP]";
            case 110: return "[POP3]";
            case 135: return "[RPC]";
            case 139: return "[NetBIOS]";
            case 143: return "[IMAP]";
            case 389: return "[LDAP]";
            case 443: return "[HTTPS]";
            case 445: return "[SMB]";
            case 1433: return "[MSSQL]";
            case 3306: return "[MySQL]";
            case 3389: return "[RDP]";
            case 5432: return "[PostgreSQL]";
            case 5900: return "[VNC]";
            case 6379: return "[Redis]";
            case 7001: return "[WebLogic]";
            case 9200: return "[Elasticsearch]";
            case 11211: return "[Memcached]";
            case 2375: return "[Docker]";
            case 27017: return "[MongoDB]";
            default: return "";
        }
    }

    private String getRiskTip(int port) {
        switch (port) {
            case 445: return "⚠️ 可能存在 MS17-010（EternalBlue）漏洞";
            case 139: return "⚠️ 文件共享信息泄露风险";
            case 135: return "⚠️ RPC 服务可能存在 RCE 漏洞";
            case 389: return "⚠️ 可能存在 LDAP 信息泄露或认证绕过";
            case 2049: return "⚠️ NFS 未授权访问风险";
            case 6379: return "⚠️ Redis 未授权访问风险";
            case 11211: return "⚠️ Memcached 未授权访问或反射放大攻击";
            case 9200: return "⚠️ Elasticsearch 未授权访问或 RCE 风险";
            case 7001: return "⚠️ WebLogic RCE（如 CVE-2017-10271）";
            case 2375: return "⚠️ Docker Remote API 未授权访问";
            case 27017: return "⚠️ MongoDB 未授权访问风险";
            default: return "";
        }
    }

    // 简单基于开放端口推测操作系统
    private String guessOperatingSystem(List<Integer> openPorts) {
        boolean has135 = openPorts.contains(135);
        boolean has139 = openPorts.contains(139);
        boolean has445 = openPorts.contains(445);
        boolean has22 = openPorts.contains(22);
        boolean has23 = openPorts.contains(23);
        boolean has5900 = openPorts.contains(5900);

        if (has135 || has139 || has445) {
            return "系统推测：Windows 系统（含 SMB、RPC 端口开放）";
        } else if (has22 || has23 || has5900) {
            return "系统推测：Linux/Unix 系统（开放 SSH/Telnet/VNC 端口）";
        } else {
            return "系统推测：未知或无法准确识别";
        }
    }

    private View generateLayout() {
        View root = getLayoutInflater().inflate(R.layout.activity_nmap_scan, null);
        etTargetIp = root.findViewById(R.id.etTargetIp);
        btnStartScan = root.findViewById(R.id.btnStartScan);
        btnCancelScan = root.findViewById(R.id.btnCancelScan);
        tvScanResult = root.findViewById(R.id.tvScanResult);
        scrollView = root.findViewById(R.id.scrollView);
        return root;
    }
}
