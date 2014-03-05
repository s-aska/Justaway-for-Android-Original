package info.justaway.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.PostActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.listener.StatusClickListener;
import info.justaway.fragment.AroundFragment;
import info.justaway.fragment.TalkFragment;
import info.justaway.listener.StatusActionListener;
import info.justaway.model.Row;
import twitter4j.Status;
import twitter4j.UserMentionEntity;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * タブのベースクラス
 */
public abstract class BaseFragment extends Fragment implements
        OnRefreshListener {

    private TwitterAdapter mAdapter;
    private ListView mListView;
    private PullToRefreshLayout mPullToRefreshLayout;

    public ListView getListView() {
        return mListView;
    }

    public TwitterAdapter getListAdapter() {
        return mAdapter;
    }

    public PullToRefreshLayout getPullToRefreshLayout() {
        return mPullToRefreshLayout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pull_to_refresh_list, container, false);
        if (v == null) {
            return null;
        }

        mListView = (ListView) v.findViewById(R.id.list_view);
        mPullToRefreshLayout = (PullToRefreshLayout) v.findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .theseChildrenArePullable(R.id.list_view)
                .listener(this)
                .setup(mPullToRefreshLayout);

        v.findViewById(R.id.guruguru).setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity activity = (MainActivity) getActivity();

        // mMainPagerAdapter.notifyDataSetChanged() された時に
        // onCreateView と onActivityCreated インスタンスが生きたまま呼ばれる
        // Adapterの生成とListViewの非表示は初回だけで良い
        if (mAdapter == null) {
            // Status(ツイート)をViewに描写するアダプター
            mAdapter = new TwitterAdapter(activity, R.layout.row_tweet);
            mListView.setVisibility(View.GONE);
        }

        mListView.setAdapter(mAdapter);

        // ツイートに関するアクション（ふぁぼ / RT / ツイ消し）のリスナー
        mAdapter.setStatusActionListener(new StatusActionListener(activity));

        mListView.setOnItemClickListener(new StatusClickListener(activity));

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Bundle args = new Bundle();
                String action = JustawayApplication.getApplication().getLongTapAction();

                Status status = mAdapter.getItem(position).getStatus();
                if (action.equals("nothing")) {
                    return false;
                } else if (action.equals("quote")) {
                    String text = " https://twitter.com/" + status.getUser().getScreenName()
                            + "/status/" + String.valueOf(status.getId());
                    tweet(text, text.length(), status.getId());
                } else if (action.equals("talk")) {
                    TalkFragment dialog = new TalkFragment();
                    args.putLong("statusId", status.getId());
                    dialog.setArguments(args);
                    dialog.show(activity.getSupportFragmentManager(), "dialog");
                } else if (action.equals("show_around")) {
                    AroundFragment aroundFragment = new AroundFragment();
                    Bundle aroundArgs = new Bundle();
                    aroundArgs.putSerializable("status", status);
                    aroundFragment.setArguments(aroundArgs);
                    aroundFragment.show(activity.getSupportFragmentManager(), "dialog");
                } else if (action.equals("share_url")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" + status.getUser().getScreenName()
                            + "/status/" + String.valueOf(status.getId()));
                    startActivity(intent);
                } else if (action.equals("reply_all")) {
                    String text = "";
                    if (status.getUser().getId() != JustawayApplication.getApplication().getUserId()) {
                        text = "@" + status.getUser().getScreenName() + " ";
                    }
                    for (UserMentionEntity mention : status.getUserMentionEntities()) {
                        if (status.getUser().getScreenName().equals(mention.getScreenName())) {
                            continue;
                        }
                        if (JustawayApplication.getApplication().getScreenName().equals(mention.getScreenName())) {
                            continue;
                        }
                        text = text.concat("@" + mention.getScreenName() + " ");
                    }
                    tweet(text, text.length(), status.getId());
                }
                return true;
            }
        });
    }

    private void tweet(String text, int selection, long inReplyToStatusId) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("status", text);
        if (selection > 0) {
            intent.putExtra("selection", selection);
        }
        if (inReplyToStatusId > 0L) {
            intent.putExtra("inReplyToStatusId", inReplyToStatusId);
        }
        startActivity(intent);
    }

    public void goToTop() {
        ListView listView = getListView();
        if (listView == null) {
            getActivity().finish();
            return;
        }
        listView.setSelection(0);
    }

    public Boolean isTop() {
        ListView listView = getListView();
        return listView != null && listView.getFirstVisiblePosition() == 0;
    }

    /**
     * UserStreamでonStatusを受信した時の挙動
     */
    public abstract void add(Row row);

    public abstract void reload();

    public void removeStatus(final long statusId) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {
                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.removeStatus(statusId);
            }
        });
    }
}
