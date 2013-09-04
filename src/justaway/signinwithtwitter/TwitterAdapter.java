package justaway.signinwithtwitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwitterAdapter extends ArrayAdapter<twitter4j.Status> {
	private Context context;
	private ArrayList<twitter4j.Status> statuses = new ArrayList<twitter4j.Status>();
	private LayoutInflater inflater;
	private int layout;

	public TwitterAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.layout = textViewResourceId;
	}

	@Override
	public void add(twitter4j.Status status) {
		super.add(status);
		this.statuses.add(status);
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

		TextView screenName = (TextView) view.findViewById(R.id.toptext);
		screenName.setTypeface(Typeface.DEFAULT_BOLD);

		// スクリーンネームをビューにセット
		if (item != null) {

			if (screenName != null) {
				screenName.setText(item.getUser().getName());
			}

			// テキストをビューにセット
			TextView text = (TextView) view.findViewById(R.id.bottomtext);
			if (text != null) {
				text.setText(item.getText());
			}

			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			if (icon != null) {
				String url = item.getUser().getOriginalProfileImageURL();
				icon.setTag(url);
				ImageGetTask task = new ImageGetTask(icon);
				task.execute(url);
			}
		}
		return view;
	}

	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView image;
		private String tag;

		public ImageGetTask(ImageView image) {
			// 対象の項目を保持しておく
			this.image = image;
			tag = image.getTag().toString();
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// ここでHttp経由で画像を取得します。取得後Bitmapで返します。
			synchronized (context) {
				try {
					// キャッシュより画像データを取得
					// Bitmap image = ImageCache.getImage(params[0]);
					Bitmap image = null;
					if (image == null) {
						// キャッシュにデータが存在しない場合はwebより画像データを取得
						URL imageUrl = new URL(params[0]);
						InputStream imageIs;
						imageIs = imageUrl.openStream();
						image = BitmapFactory.decodeStream(imageIs);
						// 取得した画像データをキャッシュに保持
						// ImageCache.setImage(params[0], image);
					}
					return image;
				} catch (MalformedURLException e) {
					return null;
				} catch (IOException e) {
					return null;
				}
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// Tagが同じものか確認して、同じであれば画像を設定する
			// （Tagの設定をしないと別の行に画像が表示されてしまう）
			if (tag.equals(image.getTag())) {
				if (result != null) {
					// 画像の設定
					image.setImageBitmap(result);
				} else {
					// エラーの場合は×印を表示
					// image.setImageDrawable(context.getResources().getDrawable(
					// R.drawable.x));
				}
				// プログレスバーを隠し、取得した画像を表示
				image.setVisibility(View.VISIBLE);
			}
		}
	}
}
