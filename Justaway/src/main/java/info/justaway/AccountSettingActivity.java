package info.justaway;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import info.justaway.adapter.account.AccessTokenAdapter;
import info.justaway.fragment.dialog.AccountSwitchDialogFragment;
import info.justaway.listener.OnTrashListener;
import info.justaway.listener.RemoveAccountListener;
import info.justaway.model.AccessTokenManager;
import twitter4j.auth.AccessToken;

public class AccountSettingActivity extends FragmentActivity implements RemoveAccountListener {

    private JustawayApplication mApplication;
    private AccessTokenManager mAccessTokenManager;
    private AccessTokenAdapter mAccountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = JustawayApplication.getApplication();
        mApplication.setTheme(this);
        setContentView(R.layout.activity_account_setting);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAccessTokenManager = mApplication.getAccessTokenManager();
        mAccountAdapter = new AccessTokenAdapter(this, R.layout.row_account);
        for (AccessToken accessToken : mAccessTokenManager.getAccessTokens()) {
            mAccountAdapter.add(accessToken);
        }

        mAccountAdapter.setOnTrashListener(new OnTrashListener() {
            @Override
            public void onTrash(int position) {
                AccountSwitchDialogFragment.newInstance(mAccountAdapter.getItem(position)).show(
                    getSupportFragmentManager(), "dialog");
            }
        });

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAccountAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AccessToken accessToken = mAccountAdapter.getItem(i);
                if (mApplication.getUserId() != accessToken.getUserId()) {
                    Intent data = new Intent();
                    data.putExtra("accessToken", accessToken);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_account:
                Intent intent = new Intent(this, SignInActivity.class);
                intent.putExtra("add_account", true);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void removeAccount(AccessToken accessToken) {
        mAccountAdapter.remove(accessToken);
        mAccessTokenManager.removeAccessToken(accessToken);
    }
}
