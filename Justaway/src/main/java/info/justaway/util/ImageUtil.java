package info.justaway.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.JustawayApplication;
import info.justaway.ScaleImageActivity;
import info.justaway.display.FadeInRoundedBitmapDisplayer;
import info.justaway.downloader.CustomImageDownaloder;
import info.justaway.settings.BasicSettings;
import twitter4j.Status;

public class ImageUtil {
    private static DisplayImageOptions sRoundedDisplayImageOptions;
    private static final Pattern PIXIV_PATTERN = Pattern.compile("^http://www.pixiv.net/member_illust.php");
    private static final HashMap<String, String> mPixivURL = new HashMap<>();

    public static void init() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(JustawayApplication.getApplication())
                .imageDownloader(new CustomImageDownaloder(JustawayApplication.getApplication()))
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public static String getPixivImageURL(String url) {
        return mPixivURL.get(url);
    }

    public static void displayImage(String url, ImageView view) {
        if (mPixivURL.containsKey(url)) {
            url = mPixivURL.get(url);
        }
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        ImageLoader.getInstance().displayImage(url, view);
    }

    public static void displayRoundedImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        if (BasicSettings.getUserIconRoundedOn()) {
            if (sRoundedDisplayImageOptions == null) {
                sRoundedDisplayImageOptions = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisc(true)
                        .resetViewBeforeLoading(true)
                        .displayer(new FadeInRoundedBitmapDisplayer(5))
                        .build();
            }
            ImageLoader.getInstance().displayImage(url, view, sRoundedDisplayImageOptions);
        } else {
            ImageLoader.getInstance().displayImage(url, view);
        }
    }

    public static void displayPixivImage(final String url, final ImageView view) {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object[] objects) {
                String content = null;
                try {
                    Document document = Jsoup.connect(url).get();
                    Elements elements = document.select("meta[property=og:image]");
                    if (elements.size() > 0) {
                        content = elements.get(0).attr("content");
                        mPixivURL.put(url, content);
                        Log.d("info.justaway", "displayPixivImage:" + url);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return content;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    displayRoundedImage(url, view);
                }
            }
        }.execute();
    }

    public static void displayThumbnailImage(String url, ImageView view) {
        if (mPixivURL.containsKey(url)) {
            displayRoundedImage(mPixivURL.get(url), view);
        } else {
            Matcher pixiv_matcher = PIXIV_PATTERN.matcher(url);
            if (pixiv_matcher.find()) {
                displayPixivImage(url, view);
            } else {
                displayRoundedImage(url, view);
            }
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
                displayThumbnailImage(url, image);

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
