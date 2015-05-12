package info.justaway.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageResizer {

    /**
     * 画像生成
     * 表示サイズ合わせて画像生成時に可能なかぎり縮小して生成します。
     *
     * @param file 画像ファイル
     * @param maxFileSize 最大ファイルサイズ
     * @return 縮小後画像ファイル
     */
    public static File compress(File file, long maxFileSize) {

        if (file.length() < maxFileSize) {
            return file;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile(file.getName(), ".small.jpg");
            BitmapFactory.Options option = new BitmapFactory.Options();

            option.inJustDecodeBounds = false;
            option.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), option);
            for (int i = 0; i < 10; i++) {
                bitmap = half(bitmap);
                FileOutputStream out = new FileOutputStream(tempFile.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.close();
                if (tempFile.length() < maxFileSize) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tempFile == null) {
            tempFile = file;
        }

        return tempFile;
    }

    /**
     * 画像リサイズ
     * @param bitmap 変換対象ビットマップ
     * @return 変換後Bitmap
     */
    public static Bitmap half(Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }

        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, false);
        bitmap.recycle();

        return resizeBitmap;
    }
}
