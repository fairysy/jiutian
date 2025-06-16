package cn.itcast.hilink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 获取 "开始" 按钮并设置点击事件
        ImageButton btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MainActivity，并传递 auto_search 参数
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.putExtra("auto_search", true); // 自动开始扫描设备
                startActivity(intent);
            }
        });

        // 获取 "关于" 按钮并设置点击事件
        Button btnAbout = findViewById(R.id.aboutButton);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 AboutActivity
                Intent intent = new Intent(WelcomeActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        // 获取 "设备详情" 按钮并设置点击事件
        Button btnDeviceInfo = findViewById(R.id.btnArpAssign);
        btnDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 DeviceInfoActivity
                Intent intent = new Intent(WelcomeActivity.this, DeviceInfoActivity.class);
                startActivity(intent);
            }
        });

        // 获取“磁场探测”按钮并设置点击事件
        Button btnMagnetic = findViewById(R.id.btnArp);
        btnMagnetic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MagneticFieldActivity
                Intent intent = new Intent(WelcomeActivity.this, MagneticFieldActivity.class);
                startActivity(intent);
            }
        });

        // 获取 "局域网传输" 按钮并设置点击事件
        Button btnLanTransfer = findViewById(R.id.btnLanTransfer);
        btnLanTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LanTransferActivity.class);
            startActivity(intent);
        });

        // 获取“反弹Shell”按钮并设置点击事件
        Button btnReverseShell = findViewById(R.id.btnReverseShell);
        btnReverseShell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, ReverseShellActivity.class);
                startActivity(intent);
            }
        });

        // 获取 "环境检查" 按钮并设置点击事件
        Button btnGoCheck = findViewById(R.id.btn_go_check);
        btnGoCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, EnvCheckActivity.class);
                startActivity(intent);
            }
        });

        // 新增：获取 "跳转到PackageActivity" 按钮并设置点击事件
        Button btnGoPackage = findViewById(R.id.btnGoPackage);
        btnGoPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 PackageActivity
                Intent intent = new Intent(WelcomeActivity.this, PackageActivity.class);
                startActivity(intent);
            }
        });



        Button enterButton = findViewById(R.id.btnnc);
        enterButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, ShellActivity.class);
            startActivity(intent);
        });

        Button yjButton = findViewById(R.id.btnyj);
        yjButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, chinaant.class);
            startActivity(intent);
        });



        Button calladdButton = findViewById(R.id.btncalladd);
        calladdButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, AddCallLogActivity.class);
            startActivity(intent);
        });

        // dirb扫描按钮
        Button btnEnterDirb = findViewById(R.id.btnEnterDirb);
        btnEnterDirb.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, DirbScanActivity.class);
            startActivity(intent);
        });
        //nmap
        Button btnNmap = findViewById(R.id.btnNmap);
        btnNmap.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, NmapScanActivity.class);
            startActivity(intent);
        });

        Button btnDomainToIp  = findViewById(R.id.btnDomainToIp);
        btnDomainToIp.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, DomainIpActivity.class);
            startActivity(intent);
        });

        Button btnpicture_back  = findViewById(R.id.btnpicture_back);
        btnpicture_back.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RecoverActivity.class);
            startActivity(intent);
        });
        Button btnipgeo  = findViewById(R.id.btnipgeo);
        btnipgeo.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, IpGeoActivity.class);
            startActivity(intent);
        });
        Button btnFakeLocation  = findViewById(R.id.btnFakeLocation);
        btnFakeLocation.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, FakeLocationActivity.class);
            startActivity(intent);
        });

    }
}
