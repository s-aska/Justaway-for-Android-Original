package info.justaway.fragment.main.tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.NewRecordEvent;
import info.justaway.event.action.AccountChangePostEvent;
import info.justaway.event.model.CreateStatusEvent;
import info.justaway.event.model.DestroyStatusEvent;
import info.justaway.event.action.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class BaseFragment extends Fragment implements OnRefreshListener {

    private TwitterAdapter mAdapter;
    private ListView mListView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private Boolean mBusy = false;
    private ArrayList<Row> mStackRows = new ArrayList<Row>();

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

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        mBusy = false;
                        render();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        mBusy = true;
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount > 0 && totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });
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

    public void onEventMainThread(DestroyStatusEvent event) {
        int removePosition = mAdapter.removeStatus(event.getStatusId());
        if (removePosition >= 0) {
            int visiblePosition = mListView.getFirstVisiblePosition();
            if (visiblePosition > removePosition) {
                View view = mListView.getChildAt(0);
                int y = view != null ? view.getTop() : 0;
                mListView.setSelectionFromTop(visiblePosition - 1, y);
            }
        }
    }

    public void onEventMainThread(CreateStatusEvent event) {
        add(event.getRow());
    }

    public void onEventMainThread(AccountChangePostEvent event) {
        if (event.getTabId() == getTabId()) {
            reload();
        } else {
            clear();
        }
    }

    public boolean goToTop() {
        if (mListView == null) {
            getActivity().finish();
            return false;
        }
        mListView.setSelection(0);
        if (mStackRows.size() > 0) {
            render();
            return false;
        } else {
            return true;
        }
    }

    public Boolean isTop() {
        return mListView != null && mListView.getFirstVisiblePosition() == 0;
    }

    private Runnable mRender = new Runnable() {
        @Override
        public void run() {
            if (mBusy) {
                return;
            }
            if (mListView == null || mAdapter == null) {
                return;
            }

            // 表示している要素の位置
            int position = mListView.getFirstVisiblePosition();

            // 縦スクロール位置
            View view = mListView.getChildAt(0);
            int y = view != null ? view.getTop() : 0;

            // 要素を上に追加（ addだと下に追加されてしまう ）
            int count = mStackRows.size();
            for (Row row : mStackRows) {
                mAdapter.insert(row, 0);
            }
            mStackRows.clear();

            boolean autoScroll = position == 0 && y == 0 && count < 5;

            if (count > 0) {
                EventBus.getDefault().post(new NewRecordEvent(getTabId(), autoScroll));
            }

            if (autoScroll) {
                mListView.setSelection(0);
            } else {
                // 少しでもスクロールさせている時は画面を動かさない様にスクロー位置を復元する
                mListView.setSelectionFromTop(position + count, y);
            }
        }
    };

    private void render() {
        if (mListView == null) {
            return;
        }
        mListView.removeCallbacks(mRender);
        mListView.postDelayed(mRender, 250);
    }

    public void add(Row row) {
        if (skip(row)) {
            return;
        }
        mStackRows.add(row);
        render();
    }

    /**
     * UserStreamでonStatusを受信した時の挙動
     */
    protected abstract boolean skip(Row row);

    public abstract void reload();

    public abstract void clear();

    public abstract long getTabId();

    protected abstract void additionalReading();
}
