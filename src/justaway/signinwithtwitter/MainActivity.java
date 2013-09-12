package justaway.signinwithtwitter;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Twitter twitter;
    private ListView listView;
    private static TwitterStream twitterStream;

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
            twitterStream = TwitterUtils.getTwitterStreamInstance(c);
        }

        findViewById(R.id.action_get_timeline).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new GetTimeline().execute();
                        startStreamingTimeline();
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
                    }
                });
    }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      // タイムラインを残す為にアクティビティをfinish()させずホームに戻す、ホームボタンを押した時と同じ動き
      if (keyCode == KeyEvent.KEYCODE_BACK) {
          moveTaskToBack(true);
      }
      return false;
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

    public void startStreamingTimeline() {
        UserStreamListener listener = new UserStreamListener() {

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                System.out.println("deletionnotice");
            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                System.out.println("scrubget");
            }

            @Override
            public void onStatus(Status status) {
                // FIXME もうちょっとカッコよく書けそう
                final Status s = status;
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        TwitterAdapter adapter = (TwitterAdapter) listView
                                .getAdapter();
                        adapter.insert(s, 0);
                    }
                });
            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                System.out.println("trackLimitation");
            }

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onBlock(User arg0, User arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onDeletionNotice(long arg0, long arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onDirectMessage(DirectMessage arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFollow(User arg0, User arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFriendList(long[] arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUnblock(User arg0, User arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListCreation(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListDeletion(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListMemberAddition(User arg0, User arg1,
                    UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListMemberDeletion(User arg0, User arg1,
                    UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListSubscription(User arg0, User arg1,
                    UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListUnsubscription(User arg0, User arg1,
                    UserList arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserListUpdate(User arg0, UserList arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onUserProfileUpdate(User arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStallWarning(StallWarning arg0) {
                // TODO Auto-generated method stub

            }
        };
        twitterStream.addListener(listener);
        twitterStream.user();
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
