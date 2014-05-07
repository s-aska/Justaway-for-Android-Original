package info.justaway.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static File writeToTempFile(File cacheDir, InputStream inputStream) {
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                return null;
            }
        }
        File file = new File(cacheDir, "justaway-temp-" + System.currentTimeMillis() + ".jpg");
        try {
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int size;
            while (-1 != (size = inputStream.read(buffer))) {
                outputStream.write(buffer, 0, size);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            return null;
        }
        return file;
    }
}
