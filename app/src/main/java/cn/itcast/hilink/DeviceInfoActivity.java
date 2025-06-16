package cn.itcast.hilink;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceInfoAdapter adapter;
    private List<DeviceInfoItem> infoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        infoList = new ArrayList<>();

        loadDeviceInfo();

        adapter = new DeviceInfoAdapter(infoList);
        recyclerView.setAdapter(adapter);
    }

    private void loadDeviceInfo() {
        infoList.add(new DeviceInfoItem("电池信息", getBatteryInfo()));
        infoList.add(new DeviceInfoItem("设备型号", Build.MODEL));
        infoList.add(new DeviceInfoItem("Android 版本", Build.VERSION.RELEASE));
        infoList.add(new DeviceInfoItem("设备品牌", Build.BRAND));
        infoList.add(new DeviceInfoItem("设备 ID", getAndroidId()));
        infoList.add(new DeviceInfoItem("CPU 信息", getCpuInfo()));
        infoList.add(new DeviceInfoItem("总内存", getMemoryInfo()));
        infoList.add(new DeviceInfoItem("可用内存", getAvailableMemory()));
        infoList.add(new DeviceInfoItem("总存储空间", getStorageInfo()));
        infoList.add(new DeviceInfoItem("可用存储空间", getAvailableStorage()));
        infoList.add(new DeviceInfoItem("蓝牙支持", isBluetoothSupported()));
        infoList.add(new DeviceInfoItem("传感器信息", getSensorInfo()));
    }

    private String getAndroidId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private String getBatteryInfo() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) return "无法获取电池信息";

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float percentage = level * 100 / (float) scale;

        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        float tempC = temperature / 10f;

        int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        String healthStr;
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthStr = "良好"; break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthStr = "过热"; break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthStr = "已损坏"; break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthStr = "电压过高"; break;
            default:
                healthStr = "未知";
        }

        return "电量: " + percentage + "%\n温度: " + tempC + " ℃\n健康状态: " + healthStr;
    }

    private String getMemoryInfo() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memInfo);
        return memInfo.totalMem / 1024 / 1024 + " MB";
    }

    private String getAvailableMemory() {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memInfo);
        return memInfo.availMem / 1024 / 1024 + " MB";
    }

    private String getStorageInfo() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long size = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
        return size / 1024 / 1024 + " MB";
    }

    private String getAvailableStorage() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long size = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
        return size / 1024 / 1024 + " MB";
    }

    private String isBluetoothSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null ? "支持" : "不支持";
    }

    private String getSensorInfo() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuilder sb = new StringBuilder();
        for (Sensor s : sensors) {
            sb.append(s.getName()).append(" (").append(getSensorDescription(s.getType())).append(")\n");
        }
        return sb.toString().isEmpty() ? "无可用传感器" : sb.toString();
    }

    private String getSensorDescription(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return "加速度";
            case Sensor.TYPE_MAGNETIC_FIELD: return "磁场";
            case Sensor.TYPE_GYROSCOPE: return "陀螺仪";
            case Sensor.TYPE_LIGHT: return "光线";
            case Sensor.TYPE_PRESSURE: return "气压";
            case Sensor.TYPE_PROXIMITY: return "距离";
            case Sensor.TYPE_GRAVITY: return "重力";
            default: return "其他";
        }
    }

    private String getCpuInfo() {
        String[] abis = Build.SUPPORTED_ABIS;
        StringBuilder sb = new StringBuilder();
        sb.append("型号: ").append(Build.HARDWARE).append("\n");
        sb.append("架构类型:\n");
        for (String abi : abis) {
            sb.append(abi).append("\n");
        }

        return sb.toString();
    }
}
