package cn.itcast.hilink;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MagneticFieldActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private TextView magneticFieldTextView, highMagneticFieldWarning;
    private float[] magneticValues = new float[3];
    private Handler handler = new Handler();
    private Runnable updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic_field);

        magneticFieldTextView = findViewById(R.id.magneticFieldTextView);
        highMagneticFieldWarning = findViewById(R.id.highMagneticFieldWarning);

        // 获取系统的传感器管理服务
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            if (magneticSensor == null) {
                magneticFieldTextView.setText("磁场传感器不可用");
                magneticFieldTextView.setTextColor(Color.RED);
                return;
            }
        } else {
            return;
        }

        // 定时刷新任务（每100ms）
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (magneticValues == null) return;

                // 计算磁场强度
                float x = magneticValues[0];
                float y = magneticValues[1];
                float z = magneticValues[2];
                float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

                String text = String.format("磁场强度：%.2f μT", magnitude);
                magneticFieldTextView.setText(text);

                // 根据磁场强度来改变颜色及显示警告
                if (magnitude < 70.0f) {
                    magneticFieldTextView.setTextColor(Color.GREEN);
                    highMagneticFieldWarning.setVisibility(View.GONE); // 隐藏警告
                } else {
                    magneticFieldTextView.setTextColor(Color.RED);
                    highMagneticFieldWarning.setVisibility(View.VISIBLE); // 显示警告
                }

                // 每100ms刷新一次
                handler.postDelayed(this, 100);
            }
        };
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValues = event.values.clone(); // 保存磁场数据
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 不处理
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (magneticSensor != null) {
            sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_UI);
            handler.post(updateTask); // 启动定时刷新
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        handler.removeCallbacks(updateTask); // 停止刷新
    }
}
