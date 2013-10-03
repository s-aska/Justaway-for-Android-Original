package info.justaway;

import com.squareup.picasso.Picasso;

import info.justaway.view.ScaleImageView;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * 画像の拡大表示用のActivity、かぶせて使う
 * @author aska
 */
public class ScaleImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ScaleImageView imageView = new ScaleImageView(this);
        String url = getIntent().getExtras().getString("url");
        Picasso.with(this).load(url).into(imageView);
        setContentView(imageView);
    }
}
