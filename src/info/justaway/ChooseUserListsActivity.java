package info.justaway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

import info.justaway.adapter.SubscribeUserListAdapter;
import info.justaway.task.UserListsLoader;
import twitter4j.ResponseList;
import twitter4j.UserList;

public class ChooseUserListsActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<ResponseList<UserList>> {

    private SubscribeUserListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user_lists);

        ListView listView = (ListView) findViewById(R.id.list);

        mAdapter = new SubscribeUserListAdapter(this, R.layout.row_subscribe_user_list);

        listView.setAdapter(mAdapter);

        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> lists = new ArrayList<Integer>();

                // 有効なチェックボックスからリストIDを取得
                ListView listView = (ListView) findViewById(R.id.list);
                int count = listView.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = listView.getChildAt(i);
                    if (view == null) {
                        continue;
                    }
                    CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
                    if (checkbox != null && checkbox.isChecked()) {
                        lists.add((Integer) checkbox.getTag());
                    }
                }

                Intent data = new Intent();
                Bundle bundle = new Bundle();
                bundle.putIntegerArrayList("lists", lists);
                data.putExtras(bundle);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ResponseList<UserList>> onCreateLoader(int arg0, Bundle arg1) {
        return new UserListsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ResponseList<UserList>> arg0, ResponseList<UserList> userLists) {
        if (userLists != null) {
            for (UserList userList : userLists) {
                mAdapter.add(userList);
            }
        }
        JustawayApplication.getApplication().setUserLists(userLists);
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<UserList>> arg0) {
    }
}
