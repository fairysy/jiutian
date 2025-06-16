package cn.itcast.hilink;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class FakeLocationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 1000; // 1秒更新一次
    private static final String MOCK_PROVIDER = LocationManager.GPS_PROVIDER; // 恢复使用GPS_PROVIDER

    private EditText etLatLng;
    private Button btnToggle;
    private boolean isMocking = false;
    private LocationManager locationManager;
    private Handler handler = new Handler();
    private Runnable locationUpdater;
    private double lat = 0.0;
    private double lon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_location);

        etLatLng = findViewById(R.id.et_latlng);
        btnToggle = findViewById(R.id.btn_toggle_mock);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnToggle.setOnClickListener(v -> {
            if (!isMocking) {
                startMocking();
            } else {
                stopMocking();
            }
        });
    }

    private void startMocking() {
        // 检查位置权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return;
        }

        // 检查模拟定位权限
        if (!isMockLocationEnabled(this)) {
            Toast.makeText(this, "请在开发者选项中将本应用设置为允许模拟位置", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, "无法打开开发者选项，请手动打开", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String input = etLatLng.getText().toString().trim();
        if (!input.contains(",")) {
            Toast.makeText(this, "请输入有效的经纬度，用英文逗号分隔", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String[] parts = input.split(",");
            lat = Double.parseDouble(parts[0]);
            lon = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "经纬度格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        setupMockProvider();
        startLocationUpdates();

        isMocking = true;
        btnToggle.setText("停止模拟");
        Toast.makeText(this, "虚拟定位已开启", Toast.LENGTH_SHORT).show();
    }

    private void setupMockProvider() {
        try {
            // 先尝试移除已有的provider
            try {
                if (locationManager.getProvider(MOCK_PROVIDER) != null) {
                    locationManager.removeTestProvider(MOCK_PROVIDER);
                }
            } catch (IllegalArgumentException e) {
                // 如果provider不存在，忽略异常
            }

            // 添加测试provider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ 需要这样设置
                locationManager.addTestProvider(
                        MOCK_PROVIDER,
                        false,  // requiresNetwork
                        false,  // requiresSatellite
                        false,  // requiresCell
                        false,  // hasMonetaryCost
                        true,   // supportsAltitude
                        true,   // supportsSpeed
                        true,   // supportsBearing
                        ProviderProperties.POWER_USAGE_LOW,
                        ProviderProperties.ACCURACY_FINE
                );
            } else {
                // 旧版本Android
                locationManager.addTestProvider(
                        MOCK_PROVIDER,
                        false,  // requiresNetwork
                        false,  // requiresSatellite
                        false,  // requiresCell
                        false,  // hasMonetaryCost
                        true,   // supportsAltitude
                        true,   // supportsSpeed
                        true,   // supportsBearing
                        ProviderProperties.POWER_USAGE_LOW,
                        ProviderProperties.ACCURACY_FINE
                );
            }

            locationManager.setTestProviderEnabled(MOCK_PROVIDER, true);
            locationManager.setTestProviderStatus(MOCK_PROVIDER,
                    LocationProvider.AVAILABLE,
                    null,
                    System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "设置模拟位置提供者失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startLocationUpdates() {
        if (locationUpdater != null) {
            handler.removeCallbacks(locationUpdater);
        }

        locationUpdater = new Runnable() {
            @Override
            public void run() {
                try {
                    Location mockLocation = new Location(MOCK_PROVIDER);
                    mockLocation.setLatitude(lat);
                    mockLocation.setLongitude(lon);
                    mockLocation.setAltitude(0);
                    mockLocation.setAccuracy(5f); // 设置为5米精度更真实
                    mockLocation.setTime(System.currentTimeMillis());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        mockLocation.setElapsedRealtimeNanos(System.nanoTime());
                    }
                    mockLocation.setSpeed(0.5f); // 设置低速移动
                    mockLocation.setBearing(90f); // 设置朝向东方

                    locationManager.setTestProviderLocation(MOCK_PROVIDER, mockLocation);
                } catch (SecurityException e) {
                    Toast.makeText(FakeLocationActivity.this, "权限不足: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    stopMocking();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(FakeLocationActivity.this, "提供者不可用: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    stopMocking();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FakeLocationActivity.this, "设置位置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    stopMocking();
                }
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        };
        handler.post(locationUpdater);
    }

    private void stopMocking() {
        if (locationUpdater != null) {
            handler.removeCallbacks(locationUpdater);
            locationUpdater = null;
        }
        try {
            if (locationManager.getProvider(MOCK_PROVIDER) != null) {
                locationManager.setTestProviderEnabled(MOCK_PROVIDER, false);
                locationManager.removeTestProvider(MOCK_PROVIDER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isMocking = false;
        btnToggle.setText("开始模拟");
        Toast.makeText(this, "虚拟定位已停止", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMocking();
    }

    public static boolean isMockLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = opsManager.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } else {
            return !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "位置权限已授予，请重新点击开始模拟", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "必须授权位置权限才能进行模拟", Toast.LENGTH_LONG).show();
            }
        }
    }
}