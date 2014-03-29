package info.justaway.fragment.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.greenrobot.event.EventBus;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.DestroyStatusEvent;
import info.justaway.event.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

        MainActivity activity = (MainActivity) getActivity();

        // mMainPagerAdapter.notifyDataSetChanged() された時に
        // onCreateView と onActivityCreated インスタンスが生きたまま呼ばれる
        // Adapterの生成とListViewの非表示は初回だけで良い
        if (mAdapter == null) {
            // Status(ツイート)をViewに描写するアダプター
            mAdapter = new TwitterAdapter(activity, R.layout.row_tweet);
            mListView.setVisibility(View.GONE);
        }

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new StatusClickListener(activity));

        mListView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, activity));
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(StatusActionEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DestroyStatusEvent event) {
        mAdapter.removeStatus(event.getStatusId());
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
