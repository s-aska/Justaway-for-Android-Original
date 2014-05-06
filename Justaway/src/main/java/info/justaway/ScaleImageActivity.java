package info.justaway;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.task.PhotoLoader;
import info.justaway.util.MessageUtil;
import info.justaway.widget.ScaleImageView;

/**
 * 画像の拡大表示用のActivity、かぶせて使う
 *
 * @author aska
 */
public class ScaleImageActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<String> {

    private ScaleImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mImageView = new ScaleImageView(this);
        mImageView.setActivity(this);

        setContentView(mImageView);

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        String url;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            url = data.toString();
        } else {
            Bundle args = intent.getExtras();
            if (args == null) {
                return;
            }
            url = args.getString("url");
        }

        if (url == null) {
            return;
        }

        Pattern pattern = Pattern.compile("https?://twitter\\.com/\\w+/status/(\\d+)/photo/(\\d+)/?.*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            Bundle args = new Bundle(1);
            args.putLong("statusId", Long.valueOf(matcher.group(1)));
            args.putInt("index", Integer.valueOf(matcher.group(2)));
            getSupportLoaderManager().initLoader(0, args, this);
            return;
        }

        ImageLoader.getInstance().displayImage(url, mImageView);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        long statusId = args.getLong("statusId");
        int index = args.getInt("index");
        return new PhotoLoader(this, statusId, index);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String url) {
        ImageLoader.getInstance().displayImage(url, mImageView);
    }

    @Override
    public void onLoaderReset(Loader<String> arg0) {

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
                MessageUtil.showToast(R.string.toast_save_image_success);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
