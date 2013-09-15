package justaway.signinwithtwitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.Status;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TwitterAdapter extends ArrayAdapter<twitter4j.Status> {
    private Context context;
    private ArrayList<twitter4j.Status> statuses = new ArrayList<twitter4j.Status>();
    private LayoutInflater inflater;
    private int layout;
    private LruCache<String, Bitmap> mMemoryCache;

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
    public void add(twitter4j.Status status) {
        super.add(status);
        this.statuses.add(status);
    }

    @Override
    public void insert(twitter4j.Status status, int index) {
        super.insert(status, index);
        this.statuses.add(index, status);
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
        Status status = (Status) statuses.get(position);
        if (status == null) {
            return view;
        }

        Status retweet = status.getRetweetedStatus();
        if (retweet == null) {
            renderStatus(view, status, null);
        } else {
            renderStatus(view, retweet, status);
        }

        return view;
    }

    private void renderStatus(View view, Status status, Status original) {
        ((TextView) view.findViewById(R.id.display_name)).setText(status
                .getUser().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + status.getUser().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText(status.getText());
        ((TextView) view.findViewById(R.id.datetime)).setText(status
                .getCreatedAt().toString());
        ((TextView) view.findViewById(R.id.via)).setText("via "
                + getClientName(status.getSource()));

        // RTの場合
        if (original != null) {
            ((TextView) view.findViewById(R.id.retweet_by))
                    .setText("Retweet By " + original.getUser().getScreenName()
                            + "(" + original.getUser().getName() + ") and "
                            + String.valueOf(original.getRetweetCount())
                            + " others");
            ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
            ProgressBar wait = (ProgressBar) view
                    .findViewById(R.id.retweet_wait);
            renderIcon(wait, icon, original.getUser().getMiniProfileImageURL());
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
    }

    // アイコンを読み込む
    private void renderIcon(ProgressBar wait, ImageView icon, String url) {
        wait.setVisibility(View.VISIBLE);
        icon.setVisibility(View.GONE);
        String tag = (String) icon.getTag();
        if (tag != null && tag == url) {
            Log.d("Justaway", "[image] " + url + " exists.");
        } else {
            icon.setTag(url);
            Bitmap image = mMemoryCache.get(url);
            if (image == null) {
                Log.d("Justaway", "[cache] " + url + " loading.");
                ImageGetTask task = new ImageGetTask(icon, wait);
                task.execute(url);
            } else {
                icon.setImageBitmap(image);
                icon.setVisibility(View.VISIBLE);
                wait.setVisibility(View.GONE);
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
                bar.setVisibility(View.GONE);
            }
        }
    }
}
