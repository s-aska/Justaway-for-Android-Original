package justaway.signinwithtwitter;

//import java.util.Locale;

//import twitter4j.auth.OAuthAuthorization;
//import twitter4j.auth.RequestToken;
//import android.net.Uri;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	Twitter twitter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("MainActivity Created.");
		showToast("MainActivity Created.");
		if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(this, TwitterOAuthActivity.class);
            startActivity(intent);
            finish();
        } else {
        	System.out.println("hasAccessToken!");
    		showToast("hasAccessToken!");
    		twitter = TwitterUtils.getTwitterInstance(this);
    		new GetTimeline().execute();
        }
//		findViewById(R.id.action_get_timeline).setOnClickListener(
//				new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						new GetTimeline().execute();
//					}
//				});
//		showToast("hasAccessToken!");
	}

//	public void getTimeline() {
//		try {
//			ResponseList<Status> homeTl = twitter.getHomeTimeline();
//			for (Status status : homeTl) {
//				showToast(status.getText());
//			}
//		} catch (TwitterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			if (e.isCausedByNetworkIssue()) {
//				showToast("ネットワークに接続して下さい");
//		    }else{
//				showToast("エラーが発生しました。");
//		    }
//		}
//	}

	private class GetTimeline extends AsyncTask<String, Void, ResponseList<twitter4j.Status>> {
		@Override
		protected ResponseList<twitter4j.Status> doInBackground(String... params) {
			try {
				System.out.println("getHomeTimeline");
				System.out.println(twitter.getAccountSettings());
				ResponseList<twitter4j.Status> homeTl = twitter.getHomeTimeline();
				return homeTl;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(ResponseList<twitter4j.Status> homeTl) {
			// dismissProgressDialog();
			if (homeTl != null) {
				for (twitter4j.Status status : homeTl) {
					showToast(status.getText());
				}
				// 認証が完了したのでツイート画面を表示する
				// setUpTweetPage();
//				showToast("認証が完了しましあt！１ これであ");
			} else {
//				showToast("OAuthAccessTokenの取得に失敗しました＞＜");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}
