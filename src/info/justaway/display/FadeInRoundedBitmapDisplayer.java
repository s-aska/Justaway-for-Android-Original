package info.justaway.display;

import android.graphics.Bitmap;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FadeInRoundedBitmapDisplayer extends RoundedBitmapDisplayer {
    int durationMillis;

    public FadeInRoundedBitmapDisplayer(int durationMillis, int roundPixels) {
        super(roundPixels);
        this.durationMillis = durationMillis;
    }

    @Override
    public Bitmap display(Bitmap bitmap, ImageView imageView, LoadedFrom loadedFrom) {
        imageView.setImageBitmap(super.display(bitmap, imageView, loadedFrom));
        if (loadedFrom != LoadedFrom.MEMORY_CACHE) {
            animate(imageView, durationMillis);
        }
        return bitmap;
    }

    public static void animate(ImageView imageView, int durationMillis) {
        AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
        fadeImage.setDuration(durationMillis);
        fadeImage.setInterpolator(new DecelerateInterpolator());
        imageView.startAnimation(fadeImage);
    }
}
