package info.justaway;

import java.util.ArrayList;

import info.justaway.adapter.UserListAdapter;
import info.justaway.task.UserListsLoader;
import twitter4j.ResponseList;
import twitter4j.UserList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ChooseUserListsActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<ResponseList<UserList>> {

    private UserListAdapter adapter;

    // private ArrayList<Integer> lists = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user_lists);

        ListView listView = (ListView) findViewById(R.id.list);

        adapter = new UserListAdapter(this, R.layout.row_user_list);

        listView.setAdapter(adapter);

        // registerForContextMenu(listView);

        // listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        // {
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view, int
        // position, long id) {
        // view.showContextMenu();
        // }
        // });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ResponseList<UserList>> onCreateLoader(int arg0, Bundle arg1) {
        return new UserListsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ResponseList<UserList>> arg0, ResponseList<UserList> userLists) {
        if (userLists != null && adapter != null) {
            for (UserList userList : userLists) {
                adapter.add(userList);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<UserList>> arg0) {
    }

    // public void onCreateContextMenu(ContextMenu menu, View view,
    // ContextMenuInfo menuInfo) {
    // super.onCreateContextMenu(menu, view, menuInfo);
    //
    // AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    // ListView listView = (ListView) view;
    // UserList userList = (UserList) listView.getItemAtPosition(info.position);
    //
    // int id = userList.getId();
    //
    // lists.add(id);
    //
    // JustawayApplication.showToast("戻ったら沢山スワイプしてみよう。(id:" + id + ")");
    // }

    /**
     * finish前に色々セットしておく、ここでセットした値は onActivityResult で取れる
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            ArrayList<Integer> lists = new ArrayList<Integer>();

            // 有効なチェックボックスからリストIDを取得
            ListView listView = (ListView) findViewById(R.id.list);
            int count = listView.getChildCount();
            for (int i = 0; i < count; i++) {
                CheckBox checkbox = (CheckBox) listView.getChildAt(i);
                if (checkbox.isChecked()) {
                    lists.add((Integer) checkbox.getTag());
                }
            }

            if (lists.size() > 0) {
                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("lists", lists);
                data.putExtras(bundle);
                setResult(RESULT_OK, data);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
        return false;
    }
}
