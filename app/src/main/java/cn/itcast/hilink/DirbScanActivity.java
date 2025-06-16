package cn.itcast.hilink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DirbScanActivity extends AppCompatActivity {

    private EditText etTargetUrl, etUserAgent;
    private TextView tvResult;
    private Button btnStart, btnCancel;
    private DirbTaskThreaded currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dirb_scan);

        etTargetUrl = findViewById(R.id.etTargetUrl);
        etUserAgent = findViewById(R.id.etUserAgent);
        tvResult = findViewById(R.id.tvResult);
        btnStart = findViewById(R.id.btnStart);
        btnCancel = findViewById(R.id.btnCancel);

        btnStart.setOnClickListener(v -> {
            String baseUrl = etTargetUrl.getText().toString().trim();
            String ua = etUserAgent.getText().toString().trim();
            if (!baseUrl.isEmpty()) {
                tvResult.setText("开始扫描...\n");
                currentTask = new DirbTaskThreaded(baseUrl, tvResult, ua);
                new Thread(currentTask).start();
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (currentTask != null) {
                currentTask.cancel();
                tvResult.append("❌ 已取消扫描。\n");
            }
        });
    }
}
