package justaway.signinwithtwitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
        twitter4j.Status item = (twitter4j.Status) statuses.get(position);

        TextView displayName = (TextView) view.findViewById(R.id.display_name);
        TextView screenName = (TextView) view.findViewById(R.id.screen_name);
        TextView status = (TextView) view.findViewById(R.id.status);
        TextView datetime = (TextView) view.findViewById(R.id.datetime);
        TextView via = (TextView) view.findViewById(R.id.via);
        // screenName.setTypeface(Typeface.DEFAULT_BOLD);

        // スクリーンネームをビューにセット
        if (item != null) {

            if (displayName != null) {
                displayName.setText(item.getUser().getName());
            }

            String name = item.getUser().getScreenName();
            if (screenName != null) {
                screenName.setText("@" + item.getUser().getScreenName());
            }

            if (status != null) {
                status.setText(item.getText());
            }

            if (datetime != null) {
                datetime.setText(item.getCreatedAt().toString());
            }

            if (via != null) {
                via.setText("via " + getClientName(item.getSource()));
            }

            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            ProgressBar waitBar = (ProgressBar) view.findViewById(R.id.WaitBar);
            waitBar.setVisibility(View.VISIBLE);
            icon.setVisibility(View.GONE);
            if (icon != null) {
                String tag = (String) icon.getTag();
                String url = item.getUser().getBiggerProfileImageURL();
                if (tag != null && tag == url) {
                    Log.d("Justaway", "[image] " + name + " exists.");
                } else {
                    icon.setTag(url);
                    Bitmap image = mMemoryCache.get(url);
                    if (image == null) {
                        Log.d("Justaway", "[cache] " + name + " loading.");
                        ImageGetTask task = new ImageGetTask(icon, waitBar);
                        task.execute(url);
                    } else {
                        // Log.d("Justaway", "[cache] " + url + " loading.");
                        icon.setImageBitmap(image);
                        icon.setVisibility(View.VISIBLE);
                        waitBar.setVisibility(View.GONE);
                    }
                }
            }
        }
        return view;
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
