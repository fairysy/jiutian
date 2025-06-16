package cn.itcast.hilink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DomainIpActivity extends AppCompatActivity {

    private static final String TAG = "DomainResolver";
    private EditText etDomain;
    private Button btnResolve;
    private TextView tvResult;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(generateLayout());

        btnResolve.setOnClickListener(v -> {
            String domain = etDomain.getText().toString().trim();
            if (TextUtils.isEmpty(domain)) {
                showToast("请输入域名");
                return;
            }

            if (!isNetworkAvailable()) {
                showToast("网络不可用，请检查连接");
                return;
            }

            // 清理域名输入（移除http://等前缀）
            domain = cleanDomainInput(domain);
            if (!isValidDomain(domain)) {
                showToast("域名格式不正确");
                return;
            }

            startResolution(domain, 20);
        });
    }

    private void startResolution(String domain, int times) {
        tvResult.setText("开始解析 " + domain + "...");
        btnResolve.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        executor.submit(() -> {
            Map<String, Integer> ipCountMap = new HashMap<>();
            int successCount = 0;
            int totalAttempts = times;

            for (int i = 1; i <= totalAttempts; i++) {
                final int attempt = i;
                handler.post(() -> {
                    tvResult.append("\n尝试 #" + attempt + "/" + totalAttempts);
                    progressBar.setProgress((attempt * 100) / totalAttempts);
                });

                try {
                    // 使用系统默认DNS解析
                    InetAddress[] addresses = InetAddress.getAllByName(domain);
                    if (addresses.length > 0) {
                        successCount++;
                        for (InetAddress addr : addresses) {
                            String ip = addr.getHostAddress();
                            ipCountMap.put(ip, ipCountMap.getOrDefault(ip, 0) + 1);
                        }
                        Log.d(TAG, "解析成功: " + Arrays.toString(addresses));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析尝试 #" + attempt + " 失败: " + e.getMessage());
                    handler.post(() -> tvResult.append("\n尝试 #" + attempt + " 失败: " + e.getMessage()));
                }

                try {
                    Thread.sleep(300); // 适当延迟
                } catch (InterruptedException e) {
                    break;
                }
            }

            final int finalSuccessCount = successCount;
            handler.post(() -> {
                btnResolve.setEnabled(true);
                progressBar.setVisibility(View.GONE);

                if (finalSuccessCount == 0) {
                    tvResult.append("\n\n所有尝试均失败，请检查:\n1. 域名是否正确\n2. 网络连接\n3. 尝试更换网络");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("\n\n解析完成 (").append(finalSuccessCount).append("/").append(totalAttempts).append(" 成功)\n");
                sb.append("统计结果:\n");

                for (Map.Entry<String, Integer> entry : ipCountMap.entrySet()) {
                    double percent = (entry.getValue() * 100.0) / finalSuccessCount;
                    sb.append(entry.getKey())
                            .append(" - 出现 ")
                            .append(entry.getValue())
                            .append(" 次 (")
                            .append(String.format("%.1f", percent))
                            .append("%)\n");
                }

                tvResult.append(sb.toString());
            });
        });
    }

    private String cleanDomainInput(String input) {
        // 移除http:// https://等前缀
        return input.replaceAll("^https?://", "").split("/")[0].trim();
    }

    private boolean isValidDomain(String domain) {
        // 简单域名格式校验
        return domain.matches("^([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private View generateLayout() {
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setPadding(50, 50, 50, 50);

        // 域名输入框
        etDomain = new EditText(this);
        etDomain.setHint("输入域名 (如 google.com)");
        etDomain.setTextSize(16);
        root.addView(etDomain, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        // 解析按钮
        btnResolve = new Button(this);
        btnResolve.setText("解析域名");
        btnResolve.setTextSize(16);
        root.addView(btnResolve, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        // 进度条
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                20));

        // 结果显示
        tvResult = new TextView(this);
        tvResult.setTextSize(14);
        tvResult.setTextColor(0xFF333333);
        tvResult.setMovementMethod(new ScrollingMovementMethod());
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0);
        lp.weight = 1;
        lp.topMargin = 20;
        root.addView(tvResult, lp);

        return root;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}