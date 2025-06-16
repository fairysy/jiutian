package cn.itcast.hilink;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class AddCallLogActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_CALL_LOG_PERMISSION = 1;

    private EditText phoneNumberEditText;
    private EditText callDurationEditText;
    private Spinner callTypeSpinner;
    private Spinner callDateSpinner;
    private Button addCallLogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_call_log);

        // 初始化视图
        phoneNumberEditText = findViewById(R.id.phone_number_edittext);
        callDurationEditText = findViewById(R.id.call_duration_edittext);
        callTypeSpinner = findViewById(R.id.call_type_spinner);
        callDateSpinner = findViewById(R.id.call_date_spinner);
        addCallLogButton = findViewById(R.id.add_call_log_button);

        // 设置通话类型下拉选项
        ArrayAdapter<CharSequence> callTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.call_types,
                android.R.layout.simple_spinner_item
        );
        callTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        callTypeSpinner.setAdapter(callTypeAdapter);

        // 设置通话时间下拉选项
        ArrayAdapter<CharSequence> callDateAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.call_dates,
                android.R.layout.simple_spinner_item
        );
        callDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        callDateSpinner.setAdapter(callDateAdapter);

        // 添加通话记录按钮点击事件
        addCallLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCallLogToSystem();
            }
        });
    }

    private void addCallLogToSystem() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALL_LOG},
                    REQUEST_WRITE_CALL_LOG_PERMISSION);
            return;
        }

        // 获取输入的值
        String phoneNumber = phoneNumberEditText.getText().toString();
        String durationStr = callDurationEditText.getText().toString();
        String callType = callTypeSpinner.getSelectedItem().toString();
        String callDate = callDateSpinner.getSelectedItem().toString();

        // 验证输入
        if (phoneNumber.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "请输入手机号和通话时长", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            long callTime = getCallTimeMillis(callDate);

            // 确定通话类型
            int callLogType;
            if (callType.equals("呼入")) {
                callLogType = CallLog.Calls.INCOMING_TYPE;
            } else if (callType.equals("呼出")) {
                callLogType = CallLog.Calls.OUTGOING_TYPE;
            } else {
                callLogType = CallLog.Calls.MISSED_TYPE;
            }

            // 创建通话记录
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, phoneNumber);
            values.put(CallLog.Calls.DATE, callTime);
            values.put(CallLog.Calls.DURATION, duration);
            values.put(CallLog.Calls.TYPE, callLogType);
            values.put(CallLog.Calls.NEW, 1); // 标记为新通话
            values.put(CallLog.Calls.CACHED_NAME, ""); // 名称
            values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0); // 号码类型
            values.put(CallLog.Calls.CACHED_NUMBER_LABEL, ""); // 号码标签

            // 插入通话记录
            getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

            Toast.makeText(this, "通话记录已添加", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的通话时长", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "添加通话记录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private long getCallTimeMillis(String callDate) {
        Calendar calendar = Calendar.getInstance();

        switch (callDate) {
            case "刚刚":
                // 使用当前时间
                break;
            case "1小时前":
                calendar.add(Calendar.HOUR, -1);
                break;
            case "今天早些时候":
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) / 2);
                break;
            case "昨天":
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "一周前":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            default:
                // 使用当前时间
                break;
        }

        return calendar.getTimeInMillis();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_CALL_LOG_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addCallLogToSystem();
            } else {
                Toast.makeText(this, "需要通话记录写入权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
}