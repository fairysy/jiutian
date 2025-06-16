package cn.itcast.hilink;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpGeoActivity extends AppCompatActivity {

    private EditText etIp;
    private TextView tvResult;
    private final String TENCENT_MAP_KEY = "QVWBZ-U3QCW-ECJRT-3I2Z5-3XVWO-CKFGW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_geo);

        etIp = findViewById(R.id.et_ip);
        tvResult = findViewById(R.id.tv_result);

        Button btnTencent = findViewById(R.id.btn_tencent);
        Button btnIp138 = findViewById(R.id.btn_ip138);

        btnTencent.setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            if (!ip.isEmpty()) {
                queryIpLocationTencent(ip);
            } else {
                Toast.makeText(this, "请输入IP地址", Toast.LENGTH_SHORT).show();
            }
        });

        btnIp138.setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            if (!ip.isEmpty()) {
                queryIp138(ip);
            } else {
                Toast.makeText(this, "请输入IP地址", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 腾讯地图接口，先获取经纬度，再逆地理编码
    private void queryIpLocationTencent(String ip) {
        new Thread(() -> {
            try {
                // 第一步：通过 ip.sb 获取经纬度等基础信息
                URL url = new URL("https://api.ip.sb/geoip/" + ip);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setRequestMethod("GET");

                int code = conn.getResponseCode();
                if (code != 200) {
                    throw new Exception("ip.sb接口响应错误，Code: " + code);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject data = new JSONObject(result.toString());

                String ipAddress = data.optString("ip", "未知");
                String country = data.optString("country", "未知");
                String region = data.optString("region", "未知");
                String city = data.optString("city", "未知");
                String isp = data.optString("organization", "未知");
                double lat = data.optDouble("latitude", 0);
                double lon = data.optDouble("longitude", 0);

                // 第二步：调用腾讯地图接口进行逆地理编码
                String address = getAddressFromTencent(lat, lon);

                String locationInfo = "IP: " + ipAddress + "\n" +
                        "国家: " + country + "\n" +
                        "省份: " + region + "\n" +
                        "城市: " + city + "\n" +
                        "经度: " + lon + "\n" +
                        "纬度: " + lat + "\n" +
                        "运营商: " + isp + "\n" +
                        "详细地址: " + address;

                runOnUiThread(() -> tvResult.setText(locationInfo));
            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("查询失败: " + e.getMessage()));
            }
        }).start();
    }

    private String getAddressFromTencent(double lat, double lon) {
        try {
            String urlStr = "https://apis.map.qq.com/ws/geocoder/v1/?location=" +
                    lat + "," + lon + "&key=" + TENCENT_MAP_KEY + "&get_poi=1";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setReadTimeout(7000);
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            if (code != 200) {
                return "腾讯地图接口响应错误，Code: " + code;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(result.toString());
            if (json.getInt("status") == 0) {
                JSONObject resultObj = json.getJSONObject("result");

                // 先尝试取formatted_addresses下的recommend字段（更友好）
                if (resultObj.has("formatted_addresses")) {
                    JSONObject formatted = resultObj.getJSONObject("formatted_addresses");
                    if (formatted.has("recommend")) {
                        return formatted.getString("recommend");
                    }
                }
                // 其次取address字段
                if (resultObj.has("address")) {
                    return resultObj.getString("address");
                }

                return "无详细地址信息";
            } else {
                return "腾讯接口错误: " + json.optString("message", "未知错误");
            }

        } catch (Exception e) {
            return "地址解析失败: " + e.getMessage();
        }
    }

    // ip138网页解析版
    private void queryIp138(String ip) {
        new Thread(() -> {
            try {
                String urlStr = "https://www.ip138.com/iplookup.asp?ip=" + ip + "&action=2";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)");

                int code = conn.getResponseCode();
                if (code != 200) {
                    throw new Exception("ip138接口响应错误，Code: " + code);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GBK"));
                StringBuilder html = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    html.append(line);
                }
                reader.close();

                String htmlStr = html.toString();
                String info = "未找到IP信息";

                // 先找 <ul class="ul1"> 匹配
                Pattern ulPattern = Pattern.compile("<ul class=\"ul1\">(.*?)</ul>", Pattern.DOTALL);
                Matcher ulMatcher = ulPattern.matcher(htmlStr);

                if (ulMatcher.find()) {
                    String ulContent = ulMatcher.group(1);
                    Pattern liPattern = Pattern.compile("<li>(.*?)</li>", Pattern.DOTALL);
                    Matcher liMatcher = liPattern.matcher(ulContent);

                    StringBuilder infoBuilder = new StringBuilder();
                    while (liMatcher.find()) {
                        String li = liMatcher.group(1);
                        li = li.replaceAll("<.*?>", "").trim();
                        infoBuilder.append(li).append("\n");
                    }
                    info = infoBuilder.toString();
                } else {
                    // 备选方案：找 id=result 的div里面的内容
                    Pattern divPattern = Pattern.compile("<div id=\"result\".*?>(.*?)</div>", Pattern.DOTALL);
                    Matcher divMatcher = divPattern.matcher(htmlStr);
                    if (divMatcher.find()) {
                        String divContent = divMatcher.group(1);
                        divContent = divContent.replaceAll("<.*?>", "").trim();
                        info = divContent;
                    }
                }

                final String finalInfo = info;
                runOnUiThread(() -> tvResult.setText(finalInfo));

            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("查询失败: " + e.getMessage()));
            }
        }).start();
    }
}
