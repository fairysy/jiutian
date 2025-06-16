package cn.itcast.hilink;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class LanTransferActivity extends AppCompatActivity {

    private ImageView ivQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_transfer); // 替换为你的布局文件

        // 初始化 ImageView
        ivQrCode = findViewById(R.id.iv_qr_code); // 替换为你的 ImageView ID

        // 示例：生成二维码
        String url = "https://example.com"; // 替换为你的 URL
        generateQr(url);
    }

    /**
     * 生成二维码并显示到 ImageView
     * @param url 要编码的 URL 或文本
     */
    private void generateQr(String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "URL 不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 使用 ZXing 生成二维码
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrCodeBitmap = barcodeEncoder.encodeBitmap(
                    url,          // 要编码的内容
                    BarcodeFormat.QR_CODE,  // 格式（二维码）
                    400,          // 宽度（像素）
                    400           // 高度（像素）
            );

            // 显示到 ImageView
            ivQrCode.setImageBitmap(qrCodeBitmap);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "生成二维码失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}