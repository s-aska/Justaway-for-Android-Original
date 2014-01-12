package info.justaway.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import info.justaway.BaseActivity;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import info.justaway.view.PullToRefreshListView;

/**
 * タブのベースクラス
 */
public abstract class BaseFragment extends Fragment {

    private TwitterAdapter mAdapter;
    private PullToRefreshListView mListView;

    public PullToRefreshListView getListView() {
        return mListView;
    }

    public TwitterAdapter getListAdapter() {
        return mAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);
        if (v == null) {
            return null;
        }

        mListView = (PullToRefreshListView) v.findViewById(R.id.list_view);
        v.findViewById(R.id.guruguru).setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

//        PullToRefreshListView listView = (PullToRefreshListView) getListView();

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(activity, R.layout.row_tweet);

        mListView.setAdapter(mAdapter);
        mListView.setSelection(1);
        mListView.setVisibility(View.GONE);

        // シングルタップでコンテキストメニューを開くための指定
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });
    }

    public void goToTop() {
        ListView listView = getListView();
        if (listView == null) {
            getActivity().finish();
            return;
        }
        listView.setSelection(1);
    }

    public Boolean isTop() {
        ListView listView = getListView();
        return listView != null && listView.getFirstVisiblePosition() == 1;
    }

    /**
     * UserStreamでonStatusを受信した時の挙動
     */
    public abstract void add(Row row);

    public void removeStatus(final long statusId) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {

                HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) listView.getAdapter();
                TwitterAdapter adapter = (TwitterAdapter) headerViewListAdapter.getWrappedAdapter();
                adapter.removeStatus(statusId);
            }
        });
    }

//    public void replaceStatus(final Status status) {
//        final ListView listView = getListView();
//        if (listView == null) {
//            return;
//        }
//
//        listView.post(new Runnable() {
//            @Override
//            public void run() {
//
//                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
//                adapter.replaceStatus(status);
//            }
//        });
//    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.onCreateContextMenuForStatus(menu, view, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        BaseActivity baseActivity = (BaseActivity) getActivity();
        return baseActivity.onContextItemSelected(item);
    }
}
