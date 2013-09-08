package justaway.signinwithtwitter;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Twitter twitter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        TwitterAdapter adapter = new TwitterAdapter(this, R.layout.tweet_row);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                Status item = (Status) listView.getItemAtPosition(position);
                
                Long statusId = item.getId();
                new FavoriteTask().execute(statusId.toString());
//                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
//                adapter.clear();
//                adapter.notifyDataSetChanged();
//                showToast("リセット");
                // クリックされたアイテムを取得します
                // Toast.makeText(ListViewSampleActivity.this, item,
                // Toast.LENGTH_LONG).show();
            }
        });
        showToast("MainActivity Created.");
        final Context c = this;
        if (!TwitterUtils.hasAccessToken(c)) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {
            showToast("hasAccessToken!");
            twitter = TwitterUtils.getTwitterInstance(c);
        }

        findViewById(R.id.action_get_timeline).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new GetTimeline().execute();
                    }
                });
        findViewById(R.id.action_signout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TwitterUtils.resetAccessToken(c);
                        Intent intent = new Intent(c, SigninActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        findViewById(R.id.action_tweet).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(c, PostActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private class GetTimeline extends
            AsyncTask<String, Void, ResponseList<twitter4j.Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(
                String... params) {
            try {
                ResponseList<twitter4j.Status> homeTl = twitter
                        .getHomeTimeline();
                return homeTl;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> homeTl) {
            if (homeTl != null) {
                showToast("Timelineの取得に成功しました＞＜");
                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.clear();
                for (twitter4j.Status status : homeTl) {
                    // showToast(status.getText());
                    adapter.add(status);
                }
            } else {
                showToast("Timelineの取得に失敗しました＞＜");
            }
        }
    }

    private class FavoriteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                twitter.createFavorite(Long.valueOf(params[0]).longValue());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {
                showToast("ふぁぼに成功しました>゜))彡");
            } else {
                showToast("ふぁぼに失敗しました＞＜");
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
