package info.justaway;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import info.justaway.view.ScaleImageView;

/**
 * 画像の拡大表示用のActivity、かぶせて使う
 *
 * @author aska
 */
public class ScaleImageActivity extends Activity {

    private ScaleImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        if (args == null) {
            return;
        }

        String url = args.getString("url");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mImageView = new ScaleImageView(this);
        mImageView.setActivity(this);

        ImageLoader.getInstance().displayImage(url, mImageView);

        setContentView(mImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scale_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save) {
            mImageView.setDrawingCacheEnabled(false);
            mImageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = mImageView.getDrawingCache(false);
            if (bitmap == null) {
                return false;
            }
            File root = new File(Environment.getExternalStorageDirectory(), "/Download/");
            try {
                File file = new File(root, new Date().getTime() + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(CompressFormat.JPEG, 100, fos);
                fos.close();
                // ギャラリーに登録
                String[] paths = {file.getPath()};
                String[] types = {"image/jpeg"};
                MediaScannerConnection.scanFile(getApplicationContext(), paths, types, null);
                JustawayApplication.showToast(R.string.toast_save_image_success);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
