package info.justaway.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;

/**
 * タブのベースクラス
 */
public abstract class BaseFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        ListView listView = getListView();

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        TwitterAdapter adapter = new TwitterAdapter(activity, R.layout.row_tweet);
        setListAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        listView.setSelection(0);
    }

    public Boolean isTop() {
        ListView listView = getListView();
        if (listView == null) {
            return false;
        }
        return listView.getFirstVisiblePosition() == 0;
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

                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
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
        JustawayApplication application = JustawayApplication.getApplication();
        application.onCreateContextMenuForStatus(menu, view, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        return application.onContextItemSelected(getActivity(), item);
    }
}
