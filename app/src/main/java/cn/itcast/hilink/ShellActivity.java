package cn.itcast.hilink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class ShellActivity extends AppCompatActivity {

    private TextView tvLog;
    private EditText etPort, etCommand;
    private Button btnRun, btnStop, btnExec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);

        etPort = findViewById(R.id.et_port);
        etCommand = findViewById(R.id.et_command);
        btnRun = findViewById(R.id.btn_run);
        btnStop = findViewById(R.id.btn_stop);
        btnExec = findViewById(R.id.btn_exec);
        tvLog = findViewById(R.id.tv_log);

        // 显示本机 IP
        TextView tvIP = findViewById(R.id.tv_ip);
        tvIP.setText("本机IP: " + getLocalIpAddress());

        btnRun.setOnClickListener(v -> {
            String portStr = etPort.getText().toString().trim();
            if (portStr.isEmpty()) {
                tvLog.setText("请输入端口号");
                return;
            }

            try {
                int port = Integer.parseInt(portStr);
                ShellServer.startServer(port, tvLog);
                tvLog.setText("监听中，端口：" + port);
            } catch (NumberFormatException e) {
                tvLog.setText("端口号格式错误");
            }
        });

        btnStop.setOnClickListener(v -> {
            ShellServer.stopServer();
            tvLog.append("\n已停止监听");
        });

        btnExec.setOnClickListener(v -> {
            String cmd = etCommand.getText().toString().trim();
            if (cmd.isEmpty()) {
                tvLog.append("\n请输入命令");
                return;
            }
            sendCommandToConnectedClient(cmd);
        });
    }

    private void sendCommandToConnectedClient(String command) {
        new Thread(() -> {
            try {
                Socket socket = ShellServer.getClientSocket();
                if (socket == null || socket.isClosed()) {
                    runOnUiThread(() -> tvLog.append("\n未连接任何客户端"));
                    return;
                }

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                writer.write(command + "\n");
                writer.flush();
                runOnUiThread(() -> tvLog.append("\n已发送命令: " + command));
            } catch (Exception e) {
                runOnUiThread(() -> tvLog.append("\n命令发送失败: " + e.getMessage()));
            }
        }).start();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() &&
                            inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "无法获取IP";
    }
}
