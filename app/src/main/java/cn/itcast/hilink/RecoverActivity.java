package cn.itcast.hilink;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecoverActivity extends AppCompatActivity {

    private LinearLayout imageLayout;
    private ProgressBar progressBar;
    private TextView progressText;
    private Button scanBtn;
    private Set<String> md5Set = new HashSet<>();
    private final String[] scanPaths = {
            "/storage/emulated/0/DCIM/.thumbnails",
            "/storage/emulated/0/Pictures/.thumbnails",
            "/storage/emulated/0/Android/data/com.coloros.gallery3d/cache/preload/"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);



        Button wyyBtn = findViewById(R.id.btn_wyy);
        wyyBtn.setOnClickListener(v -> {
            scanBtn.setEnabled(false);
            wyyBtn.setEnabled(false);
            scanWyyImages(() -> {
                runOnUiThread(() -> {
                    scanBtn.setEnabled(true);
                    wyyBtn.setEnabled(true);
                });
            });
        });








        scanBtn = findViewById(R.id.btn_scan);
        imageLayout = findViewById(R.id.image_layout);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        scanBtn.setOnClickListener(v -> {
            scanBtn.setEnabled(false);
            scanBtn.setAlpha(0.5f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    scanBtn.setEnabled(true);
                    scanBtn.setAlpha(1f);
                } else {
                    scanAllThumbnails();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });
    }

    private void scanAllThumbnails() {
        imageLayout.removeAllViews();
        md5Set.clear();

        new Thread(() -> {
            List<File> allFiles = new ArrayList<>();
            for (String path : scanPaths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) allFiles.addAll(Arrays.asList(files));
                }
            }

            allFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            int total = allFiles.size();
            runOnUiThread(() -> {
                progressBar.setMax(total);
                progressBar.setProgress(0);
                progressText.setText("开始扫描，共 " + total + " 个文件");
            });

            int current = 0;
            for (File file : allFiles) {
                current++;
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bmp != null) {
                        String md5 = getBitmapMD5(bmp);
                        if (md5Set.contains(md5)) continue;
                        md5Set.add(md5);
                        Bitmap finalBmp = bmp;
                        File finalFile = file;
                        runOnUiThread(() -> addImageToLayout(finalFile, finalBmp));
                    }
                } catch (Exception e) {
                    Log.e("Scan", "跳过文件：" + file.getName(), e);
                }

                int finalCurrent = current;
                runOnUiThread(() -> {
                    progressBar.setProgress(finalCurrent);
                    progressText.setText("扫描中：" + finalCurrent + "/" + total);
                });
            }

            runOnUiThread(() -> {
                progressText.setText("扫描完成，共加载 " + md5Set.size() + " 张图片");
                scanBtn.setEnabled(true);
                scanBtn.setAlpha(1f);
            });
        }).start();
    }

    private void addImageToLayout(File file, Bitmap bmp) {
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bmp);
        iv.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setPadding(10, 10, 10, 10);

        iv.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra("imagePath", file.getAbsolutePath());
            startActivity(intent);
        });

        iv.setOnLongClickListener(v -> {
            saveBitmapToPictures(bmp, file.getName());
            return true;
        });

        imageLayout.addView(iv);
    }

    private void saveBitmapToPictures(Bitmap bmp, String name) {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "back");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, name);
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "已保存：" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }






    private void scanWyyImages(Runnable onFinish) {
        imageLayout.removeAllViews();
        md5Set.clear();

        new Thread(() -> {
            List<String> wyyPaths = Arrays.asList(
                    "/storage/emulated/0/netease/",
                    "/storage/emulated/0/Android/data/com.netease.cloudmusic/"
            );

            List<File> allFiles = new ArrayList<>();
            for (String path : wyyPaths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    getAllFilesRecursive(dir, allFiles);  // 不判断后缀，全收集
                }
            }

            allFiles.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            int total = allFiles.size();

            runOnUiThread(() -> {
                progressBar.setMax(total);
                progressBar.setProgress(0);
                progressText.setText("网易云背景图扫描中，共 " + total + " 项");
            });

            int current = 0;
            for (File file : allFiles) {
                current++;
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bmp != null && bmp.getWidth() >= 300) {  // 控制太小的跳过
                        String md5 = getBitmapMD5(bmp);
                        if (md5Set.contains(md5)) continue;
                        md5Set.add(md5);
                        Bitmap finalBmp = bmp;
                        File finalFile = file;
                        runOnUiThread(() -> addImageToLayout(finalFile, finalBmp));
                    }
                } catch (Exception ignored) {}

                int finalCurrent = current;
                runOnUiThread(() -> {
                    progressBar.setProgress(finalCurrent);
                    progressText.setText("网易云扫描中：" + finalCurrent + "/" + total);
                });
            }

            runOnUiThread(() -> {
                progressText.setText("网易云图片扫描完成，共加载 " + md5Set.size() + " 张图");
                onFinish.run();
            });
        }).start();
    }
    private void getAllFilesRecursive(File dir, List<File> allFiles) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                getAllFilesRecursive(file, allFiles);
            } else {
                allFiles.add(file);  // 不判断类型
            }
        }
    }












    private String getBitmapMD5(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
