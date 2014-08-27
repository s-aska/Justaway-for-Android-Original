package info.justaway.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.ScaleImageActivity;
import info.justaway.display.RoundedTransformation;
import info.justaway.settings.BasicSettings;
import twitter4j.Status;

public class ImageUtil {
    private static Picasso sPicasso;

    public static void init() {
        sPicasso = Picasso.with(JustawayApplication.getApplication());
    }

    public static void displayImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        sPicasso.load(url).into(view);
    }

    public static void displayRoundedImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        if (BasicSettings.getUserIconRoundedOn()) {
            sPicasso.load(url).transform(new RoundedTransformation(5, 0)).into(view);
        } else {
            sPicasso.load(url).into(view);
        }
    }

    /**
     * ツイートに含まれる画像をサムネイル表示
     *
     * @param context   Activity
     * @param viewGroup サムネイルを表示するView
     * @param status    ツイート
     */
    public static void displayThumbnailImages(final Context context, ViewGroup viewGroup, Status status) {
        // ツイートに含まれる画像のURLをすべて取得
        ArrayList<String> imageUrls = StatusUtil.getImageUrls(status);
        if (imageUrls.size() > 0) {

            // 画像を貼るスペースをクリア
            viewGroup.removeAllViews();
            for (final String url : imageUrls) {
                ImageView image = new ImageView(context);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewGroup.addView(image,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 120));
                displayRoundedImage(url, image);

                // 画像タップで拡大表示（ピンチイン・ピンチアウトいつかちゃんとやる）
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ScaleImageActivity.class);
                        intent.putExtra("url", url);
                        context.startActivity(intent);
                    }
                });
            }
            viewGroup.setVisibility(View.VISIBLE);
        } else {
            viewGroup.setVisibility(View.GONE);
        }
    }
}
