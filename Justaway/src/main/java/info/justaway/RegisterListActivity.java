package info.justaway;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import info.justaway.adapter.RegisterListAdapter;
import info.justaway.model.UserListWithRegistered;
import twitter4j.ResponseList;
import twitter4j.UserList;


public class RegisterListActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<ResponseList<UserList>>> {

    private RegisterListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = (ListView) findViewById(R.id.list);

        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        args.putLong("userId", intent.getLongExtra("userId", -1));

        mAdapter = new RegisterListAdapter(this, R.layout.row_subscribe_user_list, intent.getLongExtra("userId", -1));
        listView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, args, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public Loader<ArrayList<ResponseList<UserList>>> onCreateLoader(int arg0, Bundle arg1) {
        return new RegisterUserListsLoader(this, arg1.getLong("userId"));
    }

    @Override
    @SuppressLint("UseSparseArrays")
    public void onLoadFinished(Loader<ArrayList<ResponseList<UserList>>> arg0, ArrayList<ResponseList<UserList>> responseLists) {
        if (responseLists == null) {
            return;
        }
        ResponseList<UserList> userLists = responseLists.get(0);
        ResponseList<UserList> registeredUserLists = responseLists.get(1);
        HashMap<Long, Boolean> registeredMap = new HashMap<Long, Boolean>();
        for (UserList registeredUserList : registeredUserLists) {
            registeredMap.put(registeredUserList.getId(), true);
        }
        for (UserList userList : userLists) {
            UserListWithRegistered userListWithRegistered = new UserListWithRegistered();
            userListWithRegistered.setRegistered(registeredMap.get(userList.getId()) != null);
            userListWithRegistered.setUserList(userList);
            mAdapter.add(userListWithRegistered);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ResponseList<UserList>>> arg0) {
    }
}
