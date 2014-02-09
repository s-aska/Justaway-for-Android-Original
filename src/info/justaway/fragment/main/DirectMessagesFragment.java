package info.justaway.fragment.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Collections;
import java.util.Comparator;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;

public class DirectMessagesFragment extends BaseFragment {

    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mDirectMessagesMaxId = 0L;
    private long mSentDirectMessagesMaxId = 0L;
    private ProgressBar mFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });

        if (mDirectMessagesMaxId == 0L && mSentDirectMessagesMaxId == 0L) {
            new DirectMessagesTask().execute();
        }
    }

    @Override
    public void reload() {
        mReload = true;
        mDirectMessagesMaxId = 0L;
        mSentDirectMessagesMaxId = 0L;
        new DirectMessagesTask().execute();
    }

    @Override
    public void onRefreshStarted(View view) {
        reload();
    }

    private void additionalReading() {
        if (!mAutoLoader || mReload) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new DirectMessagesTask().execute();
    }

    /**
     * ページ最上部だと自動的に読み込まれ、スクロールしていると動かないという美しい挙動
     */
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (!row.isDirectMessage()) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {

                // 表示している要素の位置
                int position = listView.getFirstVisiblePosition();

                // 縦スクロール位置
                View view = listView.getChildAt(0);
                int y = view != null ? view.getTop() : 0;

                // 要素を上に追加（ addだと下に追加されてしまう ）
                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.insert(row, 0);

                // 少しでもスクロールさせている時は画面を動かさない様にスクロー位置を復元する
                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) {
                    return;
                }
                if (position != 0 || y != 0) {
                    listView.setSelectionFromTop(position + 1, y);
                    activity.onNewDirectMessage(false);
                } else {
                    activity.onNewDirectMessage(true);
                }
            }
        });
    }

    public void remove(final long directMessageId) {
        ListView listView = getListView();
        if (listView == null) {
            return;
        }

        final TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
        listView.post(new Runnable() {
            @Override
            public void run() {
                adapter.removeDirectMessage(directMessageId);
            }
        });
    }

    private class DirectMessagesTask extends AsyncTask<Void, Void, ResponseList<DirectMessage>> {
        @Override
        protected ResponseList<DirectMessage> doInBackground(Void... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();

                // 受信したDM
                Paging directMessagesPaging = new Paging();
                if (mDirectMessagesMaxId > 0) {
                    directMessagesPaging.setMaxId(mDirectMessagesMaxId - 1);
                    directMessagesPaging.setCount(200);
                } else {
                    directMessagesPaging.setCount(25);
                }
                ResponseList<DirectMessage> directMessages = twitter.getDirectMessages(directMessagesPaging);
                for (DirectMessage directMessage : directMessages) {
                    if (mDirectMessagesMaxId == 0L || mDirectMessagesMaxId > directMessage.getId()) {
                        mDirectMessagesMaxId = directMessage.getId();
                    }
                }

                // 送信したDM
                Paging sentDirectMessagesPaging = new Paging();
                if (mSentDirectMessagesMaxId > 0) {
                    sentDirectMessagesPaging.setMaxId(mSentDirectMessagesMaxId - 1);
                    sentDirectMessagesPaging.setCount(200);
                } else {
                    sentDirectMessagesPaging.setCount(25);
                }
                ResponseList<DirectMessage> sentDirectMessages = twitter.getSentDirectMessages(sentDirectMessagesPaging);
                for (DirectMessage directMessage : sentDirectMessages) {
                    if (mSentDirectMessagesMaxId == 0L || mSentDirectMessagesMaxId > directMessage.getId()) {
                        mSentDirectMessagesMaxId = directMessage.getId();
                    }
                }

                directMessages.addAll(sentDirectMessages);

                // 日付でソート
                Collections.sort(directMessages, new Comparator<DirectMessage>() {

                    @Override
                    public int compare(DirectMessage arg0, DirectMessage arg1) {
                        return arg1.getCreatedAt().compareTo(
                                arg0.getCreatedAt());
                    }
                });
                return directMessages;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<DirectMessage> statuses) {
            mFooter.setVisibility(View.GONE);
            if (statuses == null || statuses.size() == 0) {
                return;
            }
            TwitterAdapter adapter = getListAdapter();
            if (mReload) {
                adapter.clear();
                for (DirectMessage status : statuses) {
                    adapter.add(Row.newDirectMessage(status));
                }
                mReload = false;
                getPullToRefreshLayout().setRefreshComplete();
                return;
            }
            for (DirectMessage status : statuses) {
                adapter.extensionAdd(Row.newDirectMessage(status));
            }
            mAutoLoader = true;
            getListView().setVisibility(View.VISIBLE);
        }
    }
}
