package cn.itcast.hilink;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        // 获取 TextView 和 Button 控件
        TextView textView = findViewById(R.id.text_view);
        Button btnCopy = findViewById(R.id.btn_copy);

        // 设置文本内容为 PowerShell 代码
        String codeText = "$listener = [System.Net.Sockets.TcpListener]9999\n" +
                "$listener.Start()\n" +
                "$client = $listener.AcceptTcpClient()\n" +
                "$stream = $client.GetStream()\n" +
                "$reader = New-Object System.IO.StreamReader($stream)\n" +
                "while ($true) {\n" +
                "    $line = $reader.ReadLine()\n" +
                "    Write-Host $line\n" +
                "}\n";

        // 设置 TextView 的文本
        textView.setText(codeText);

        // 设置一键复制按钮的点击监听器
        btnCopy.setOnClickListener(v -> {
            // 获取系统剪贴板管理器
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // 创建剪贴板内容并设置为需要复制的文本
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", codeText);
            clipboard.setPrimaryClip(clip);

            // 提示用户复制成功
            Toast.makeText(CodeActivity.this, "代码已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });
    }
}
