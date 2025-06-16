package cn.itcast.hilink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private Button checkForUpdateButton;
    private TextView customerService;
    private long lastClickTime = 0;  // 记录上次点击时间
    private static final long MIN_CLICK_INTERVAL = 30000;  // 10秒内防止重复点击
    private int clickCount = 0;  // 记录点击次数
    private Handler handler = new Handler();  // 延时任务的处理
    private boolean isInCooldown = false;  // 防止短时间内多次点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        checkForUpdateButton = findViewById(R.id.checkForUpdateButton);
        customerService = findViewById(R.id.customerService);

        // 点击客服电话拨打电话
        customerService.setOnClickListener(view -> {
            String phoneNumber = "10086"; // 客服电话
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);
        });

        // 点击“检查更新”按钮
        checkForUpdateButton.setOnClickListener(view -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < MIN_CLICK_INTERVAL && isInCooldown) {
                // 如果在10秒内点击过一次，且当前处于冷却期
                clickCount++;
                handleClickBehavior();
            } else {
                lastClickTime = currentTime;
                clickCount = 1; // 重置点击次数
                isInCooldown = true;  // 进入冷却状态，阻止短时间内重复点击
                checkForUpdates();
            }
        });
    }

    /**
     * 检查更新的功能
     */
    private void checkForUpdates() {
        // 此处可以通过网络请求检查是否有新版本
        // 这里只是模拟一个提示
        Toast.makeText(this, "正在检查更新...", Toast.LENGTH_SHORT).show();

        // 模拟延时，10秒后显示结果
        handler.postDelayed(() -> {
            // 如果没有更新，提示已是最新版本
            Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
            resetClickState();  // 重置点击状态，开始新的延时周期
        }, 1000);  // 模拟 1 秒钟后显示
    }

    /**
     * 根据点击次数判断行为
     */
    private void handleClickBehavior() {
        if (clickCount == 2) {
            Toast.makeText(this, "你瞅啥，没更新", Toast.LENGTH_SHORT).show();
        } else if (clickCount >= 3) {
            Toast.makeText(this, "你点你m", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重置点击状态，准备开始下一个周期
     */
    private void resetClickState() {
        // 重置点击状态，准备下一次点击
        handler.postDelayed(() -> {
            lastClickTime = 0;
            clickCount = 0;
            isInCooldown = false;  // 结束冷却期，允许下一次点击
        }, MIN_CLICK_INTERVAL);  // 10秒后重置
    }
}
