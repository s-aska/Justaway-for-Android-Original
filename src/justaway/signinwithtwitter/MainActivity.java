package justaway.signinwithtwitter;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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

        // Guy that does not sleep.
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        listView = (ListView) findViewById(R.id.list);

        // コンテキストメニューが使える様になる（デフォルトでロングタップで開く）
        registerForContextMenu(listView);

        // Status(ツイート)をViewに変換するアダプター
        TwitterAdapter adapter = new TwitterAdapter(this, R.layout.tweet_row);
        listView.setAdapter(adapter);

        // シングルタップ時の挙動
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // コンテキストメニュー
                view.showContextMenu();
                // ListView listView = (ListView) parent;
                // Status item = (Status) listView.getItemAtPosition(position);
                // new FavoriteTask().execute(item.getId());
            }
        });
        final Context c = this;
        if (!TwitterUtils.hasAccessToken(c)) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {
            twitter = TwitterUtils.getTwitterInstance(c);
            twitterStream = TwitterUtils.getTwitterStreamInstance(c);
            new GetTimeline().execute();
            startStreamingTimeline();
        }

        // findViewById(R.id.action_get_timeline).setOnClickListener(
        // new View.OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // new GetTimeline().execute();
        // twitterStream.cleanUp();
        // twitterStream.shutdown();
        // twitterStream.user();
        // }
        // });
        findViewById(R.id.action_tweet).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(c, PostActivity.class);
                        startActivity(intent);
                    }
                });
    }

    static final int CONTEXT_MENU_REPLY_ID = 1;
    static final int CONTEXT_MENU_FAV_ID = 2;
    static final int CONTEXT_MENU_FAVRT_ID = 3;
    static final int CONTEXT_MENU_RT_ID = 4;
    static final int CONTEXT_MENU_QT_ID = 5;
    static final int CONTEXT_MENU_LINK_ID = 6;

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;

        Status item = (Status) listView.getItemAtPosition(info.position);
        menu.setHeaderTitle(item.getText());
        URLEntity[] urls = item.getURLEntities();
        URLEntity[] medias = item.getMediaEntities();
        menu.add(0, CONTEXT_MENU_REPLY_ID, 0, "リプ");
        menu.add(0, CONTEXT_MENU_QT_ID, 0, "引用");
        menu.add(0, CONTEXT_MENU_FAV_ID, 0, "ふぁぼ");
        menu.add(0, CONTEXT_MENU_FAVRT_ID, 0, "ふぁぼ＆公式RT");
        menu.add(0, CONTEXT_MENU_RT_ID, 0, "公式RT");
        for (URLEntity url : urls) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL()
                    .toString());
        }
        for (URLEntity url : medias) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL()
                    .toString());
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        Status status = (Status) listView.getItemAtPosition(info.position);
        Intent intent;

        switch (item.getItemId()) {
        case CONTEXT_MENU_REPLY_ID:
            intent = new Intent(this, PostActivity.class);
            String text = "@" + status.getUser().getScreenName() + " ";
            intent.putExtra("status", text);
            intent.putExtra("selection", text.length());
            intent.putExtra("inReplyToStatusId", status.getId());
            startActivity(intent);
            return true;
        case CONTEXT_MENU_QT_ID:
            intent = new Intent(this, PostActivity.class);
            intent.putExtra("status",
                    " https://twitter.com/" + status.getUser().getScreenName()
                            + "/status/" + String.valueOf(status.getId()));
            intent.putExtra("inReplyToStatusId", status.getId());
            startActivity(intent);
            return true;
        case CONTEXT_MENU_RT_ID:
            new RetweetTask().execute(status.getId());
            return true;
        case CONTEXT_MENU_FAV_ID:
            new FavoriteTask().execute(status.getId());
            return true;
        case CONTEXT_MENU_FAVRT_ID:
            new FavoriteTask().execute(status.getId());
            new RetweetTask().execute(status.getId());
            return true;
        case CONTEXT_MENU_LINK_ID:
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle()
                    .toString()));
            startActivity(intent);

            return true;
        default:
            return super.onContextItemSelected(item);
        }
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

    private class FavoriteTask extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                twitter.createFavorite(params[0]);
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

    private class RetweetTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long super_sugoi = params[0];
            try {
                twitter.retweetStatus(super_sugoi);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                showToast("RTに成功しました>゜))彡");
            } else {
                showToast("RTに失敗しました＞＜");
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

                        // 表示している要素の位置
                        int position = listView.getFirstVisiblePosition();

                        // 縦スクロール位置
                        View view = listView.getChildAt(0);
                        int y = view != null ? view.getTop() : 0;

                        // 要素を上に追加（ addだと下に追加されてしまう ）
                        TwitterAdapter adapter = (TwitterAdapter) listView
                                .getAdapter();
                        adapter.insert(s, 0);

                        // 少しでもスクロールさせている時は画面を動かさない様にスクロー位置を復元する
                        if (position != 0 || y != 0) {
                            listView.setSelectionFromTop(position + 1, y);
                        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.signout:
            TwitterUtils.resetAccessToken(this);
            finish();
            break;
        case R.id.reload:
            new GetTimeline().execute();
            twitterStream.cleanUp();
            twitterStream.shutdown();
            twitterStream.user();
            break;
        }
        return true;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
