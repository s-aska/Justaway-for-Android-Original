package info.justaway;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import info.justaway.adapter.SubscribeUserListAdapter;
import info.justaway.event.AlertDialogEvent;
import info.justaway.event.DestroyUserListEvent;
import info.justaway.model.UserListWithRegistered;
import info.justaway.task.UserListsLoader;
import twitter4j.ResponseList;
import twitter4j.UserList;

public class ChooseUserListsActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<ResponseList<UserList>> {

    private SubscribeUserListAdapter mAdapter;
    private JustawayApplication mApplication;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = JustawayApplication.getApplication();
        mApplication.setTheme(this);
        setContentView(R.layout.activity_choose_user_lists);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                HashMap<Long, Boolean> checkMap = new HashMap<Long, Boolean>();
                ArrayList<UserList> checkList = new ArrayList<UserList>();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    UserListWithRegistered userListWithRegistered = mAdapter.getItem(i);
                    UserList userList = userListWithRegistered.getUserList();
                    if (userListWithRegistered.isRegistered()) {
                        checkMap.put(userList.getId(), true);
                        checkList.add(userList);
                    }
                }
                HashMap<Long, Boolean> tabMap = new HashMap<Long, Boolean>();
                ArrayList<JustawayApplication.Tab> tabs = new ArrayList<JustawayApplication.Tab>();
                for (JustawayApplication.Tab tab : mApplication.loadTabs()) {
                    if (tabMap.get(tab.id) != null) {
                        continue;
                    }
                    if (tab.id < 0 || checkMap.get(tab.id) != null) {
                        tabs.add(tab);
                        tabMap.put(tab.id, true);
                    }
                }
                for (UserList userList : checkList) {
                    if (tabMap.get(userList.getId()) != null) {
                        continue;
                    }
                    JustawayApplication.Tab tab = new JustawayApplication.Tab(userList.getId());
                    if (userList.getUser().getId() == mApplication.getUserId()) {
                        tab.name = userList.getName();
                    } else {
                        tab.name = userList.getFullName();
                    }
                    tabs.add(tab);
                    tabMap.put(tab.id, true);
                }
                mApplication.saveTabs(tabs);
                setResult(RESULT_OK);
                finish();
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AlertDialogEvent event) {
        event.getDialogFragment().show(getSupportFragmentManager(), "dialog");
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DestroyUserListEvent event) {
        UserListWithRegistered userListWithRegistered = mAdapter.findByUserListId(event.getUserListId());
        if (userListWithRegistered != null) {
            mAdapter.remove(userListWithRegistered);
        }
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
    public Loader<ResponseList<UserList>> onCreateLoader(int arg0, Bundle arg1) {
        return new UserListsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ResponseList<UserList>> arg0, ResponseList<UserList> userLists) {
        JustawayApplication application = JustawayApplication.getApplication();
        if (userLists != null) {
            for (UserList userList : userLists) {
                UserListWithRegistered userListWithRegistered = new UserListWithRegistered();
                userListWithRegistered.setRegistered(application.hasTabId(userList.getId()));
                userListWithRegistered.setUserList(userList);
                mAdapter.add(userListWithRegistered);
            }
        }
        application.setUserLists(userLists);
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<UserList>> arg0) {
    }
}
