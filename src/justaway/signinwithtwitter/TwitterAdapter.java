package justaway.signinwithtwitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TwitterAdapter extends ArrayAdapter<Row> {
    private Context context;
    private ArrayList<Row> statuses = new ArrayList<Row>();
    private LayoutInflater inflater;
    private int layout;
    private LruCache<String, Bitmap> mMemoryCache;
    private static int limit = 500;

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layout = textViewResourceId;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 2;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void add(Row row) {
        super.add(row);
        this.statuses.add(row);
        this.limitation();
    }

    @Override
    public void insert(Row row, int index) {
        super.insert(row, index);
        this.statuses.add(index, row);
        this.limitation();
    }

    public void limitation() {
        int size = this.statuses.size();
        if (size > limit) {
            int count = size - limit;
            for (int i = 0; i < count; i++) {
                super.remove(this.statuses.remove(size - i - 1));
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.statuses.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = inflater.inflate(this.layout, null);
        }

        // 表示すべきデータの取得
        Row row = (Row) statuses.get(position);

        if (row.isDirectMessage()) {
            DirectMessage message = row.getMessage();
            if (message == null) {
                return view;
            }
            renderMessage(view, message);
        } else {
            Status status = row.getStatus();
            if (status == null) {
                return view;
            }

            Status retweet = status.getRetweetedStatus();
            if (row.isFavorite()) {
                renderStatus(view, status, null, row.getSource());
            } else if (retweet == null) {
                renderStatus(view, status, null, null);
            } else {
                renderStatus(view, retweet, status, null);
            }
        }

        if (position == 0) {
            ((MainActivity) context).showTopView();
        }

        return view;
    }

    private void renderMessage(View view, DirectMessage message) {
        ((TextView) view.findViewById(R.id.display_name)).setText(message
                .getSender().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + message.getSender().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText("D "
                + message.getRecipientScreenName() + " " + message.getText());
        SimpleDateFormat date_format = new SimpleDateFormat(
                "MM'/'dd' 'hh':'mm':'ss", Locale.ENGLISH);
        ((TextView) view.findViewById(R.id.datetime)).setText(date_format
                .format(message.getCreatedAt()));
        view.findViewById(R.id.via).setVisibility(View.GONE);
        view.findViewById(R.id.retweet).setVisibility(View.GONE);
        view.findViewById(R.id.images).setVisibility(View.GONE);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        ProgressBar wait = (ProgressBar) view.findViewById(R.id.WaitBar);
        renderIcon(wait, icon, message.getSender().getBiggerProfileImageURL());
    }

    private void renderStatus(View view, Status status, Status retweet,
            User favorite) {
        ((TextView) view.findViewById(R.id.display_name)).setText(status
                .getUser().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + status.getUser().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText(status.getText());
        SimpleDateFormat date_format = new SimpleDateFormat(
                "MM'/'dd' 'hh':'mm':'ss", Locale.ENGLISH);
        ((TextView) view.findViewById(R.id.datetime)).setText(date_format
                .format(status.getCreatedAt()));
        ((TextView) view.findViewById(R.id.via)).setText("via "
                + getClientName(status.getSource()));
        view.findViewById(R.id.via).setVisibility(View.VISIBLE);

        // favの場合
        if (favorite != null) {
            ((TextView) view.findViewById(R.id.retweet_by))
                    .setText("favorited by " + favorite.getScreenName() + "("
                            + favorite.getName() + ")");
            ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
            ProgressBar wait = (ProgressBar) view
                    .findViewById(R.id.retweet_wait);
            renderIcon(wait, icon, favorite.getMiniProfileImageURL());
            view.findViewById(R.id.retweet).setVisibility(View.VISIBLE);
        }
        // RTの場合
        else if (retweet != null) {
            ((TextView) view.findViewById(R.id.retweet_by))
                    .setText("retweeted by "
                            + retweet.getUser().getScreenName() + "("
                            + retweet.getUser().getName() + ") and "
                            + String.valueOf(status.getRetweetCount())
                            + " others");
            ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
            ProgressBar wait = (ProgressBar) view
                    .findViewById(R.id.retweet_wait);
            renderIcon(wait, icon, retweet.getUser().getMiniProfileImageURL());
            view.findViewById(R.id.retweet).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.retweet).setVisibility(View.GONE);
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        ProgressBar wait = (ProgressBar) view.findViewById(R.id.WaitBar);
        renderIcon(wait, icon, status.getUser().getBiggerProfileImageURL());
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: UserProfileActivityへ
                System.out.println("icon touch!");
            }
        });

        MediaEntity[] medias = retweet != null ? retweet.getMediaEntities()
                : status.getMediaEntities();
        LinearLayout images = (LinearLayout) view.findViewById(R.id.images);
        images.removeAllViews();
        if (medias.length > 0) {
            for (final MediaEntity url : medias) {
                ImageView image = new ImageView(context);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                images.addView(image, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 120));
                renderIcon(null, image, url.getMediaURL());
                // 画像タップで拡大表示（ピンチイン・ピンチアウトをWebViewにやらせる）
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WebView webView = new WebView(context);
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);
                        webView.getSettings().setBuiltInZoomControls(true);
                        String htmlData = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>html,body {margin:0;padding:0}img {max-width:100%}</style><img src=\"" + url.getMediaURL() + "\">";
                        webView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", null);
                        Dialog dialog = new Dialog(context);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(webView);
                        dialog.show();
                    }
                });
            }
            images.setVisibility(View.VISIBLE);
        } else {
            images.setVisibility(View.GONE);
        }
    }

    // アイコンを読み込む
    private void renderIcon(ProgressBar wait, ImageView icon, String url) {
        if (wait != null) {
            wait.setVisibility(View.VISIBLE);
        }
        icon.setVisibility(View.GONE);
        String tag = (String) icon.getTag();
        if (tag != null && tag == url) {
        } else {
            icon.setTag(url);
            Bitmap image = mMemoryCache.get(url);
            if (image == null) {
                ImageGetTask task = new ImageGetTask(icon, wait);
                task.execute(url);
            } else {
                icon.setImageBitmap(image);
                icon.setVisibility(View.VISIBLE);
                if (wait != null) {
                    wait.setVisibility(View.GONE);
                }
            }
        }
    }

    private String getClientName(String source) {
        String[] tokens = source.split("[<>]");
        if (tokens.length > 1) {
            return tokens[2];
        } else {
            return tokens[0];
        }
    }

    class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView image;
        private String tag;
        private ProgressBar bar;

        public ImageGetTask(ImageView image, ProgressBar bar) {
            // 対象の項目を保持しておく
            this.image = image;
            this.bar = bar;
            this.tag = image.getTag().toString();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // ここでHttp経由で画像を取得します。取得後Bitmapで返します。
            synchronized (context) {
                try {
                    URL imageUrl = new URL(params[0]);
                    InputStream imageIs;
                    imageIs = imageUrl.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(imageIs);
                    return bitmap;
                } catch (MalformedURLException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Tagが同じものか確認して、同じであれば画像を設定する
            // （Tagの設定をしないと別の行に画像が表示されてしまう）
            if (tag.equals(image.getTag())) {
                if (bitmap != null) {
                    // 画像の設定
                    mMemoryCache.put(tag, bitmap);
                    image.setImageBitmap(bitmap);
                } else {
                    // エラーの場合は×印を表示
                    // image.setImageDrawable(context.getResources().getDrawable(
                    // R.drawable.x));
                }
                // プログレスバーを隠し、取得した画像を表示
                image.setVisibility(View.VISIBLE);
                if (bar != null) {
                    bar.setVisibility(View.GONE);
                }
            }
        }
    }
}
