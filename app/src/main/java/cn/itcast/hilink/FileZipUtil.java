package cn.itcast.hilink;

import android.content.Context;
import android.net.Uri;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZipUtil {
    public static File zipFiles(Context context, List<Uri> uris) throws IOException {
        File output = new File(context.getCacheDir(), "shared.zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));

        for (Uri uri : uris) {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) continue;
            String name = Utils.getFileName(context, uri);
            zos.putNextEntry(new ZipEntry(name));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
            is.close();
        }

        zos.close();
        return output;
    }
}
