package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import twitter4j.auth.AccessToken;

public class AccountSettingActivity extends FragmentActivity {

    private Activity mActivity;
    private JustawayApplication mApplication;
    private AccountAdapter mAccountAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = JustawayApplication.getApplication();
        mApplication.setTheme(this);
        setContentView(R.layout.activity_account_setting);

        mActivity = this;
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = (ListView) findViewById(R.id.list_view);

        mAccountAdapter = new AccountAdapter(this, R.layout.row_account);
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

        for (AccessToken accessToken : mApplication.getAccessTokens()) {
            mAccountAdapter.add(accessToken);
        }
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

    public class AccountAdapter extends ArrayAdapter<AccessToken> {

        private ArrayList<AccessToken> mAccountLists = new ArrayList<AccessToken>();
        private LayoutInflater mInflater;
        private int mLayout;

        public AccountAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(AccessToken account) {
            super.add(account);
            mAccountLists.add(account);
        }

        public void remove(int position) {
            super.remove(mAccountLists.remove(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            final AccessToken accessToken = mAccountLists.get(position);

            assert view != null;

            mApplication.displayUserIcon(accessToken.getUserId(), (ImageView) view.findViewById(R.id.icon));

            TextView screenName = (TextView) view.findViewById(R.id.screen_name);
            screenName.setText(accessToken.getScreenName());

            TextView trash = (TextView) view.findViewById(R.id.trash);
            trash.setTypeface(JustawayApplication.getFontello());

            if (mApplication.getUserId() == accessToken.getUserId()) {
                screenName.setTextColor(mApplication.getThemeTextColor(mActivity, R.attr.holo_blue));
                trash.setVisibility(View.GONE);
            }

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AccountSwitchDialogFragment.newInstance(accessToken, position)
                            .show(getSupportFragmentManager(), "dialog");
                }
            });

            return view;
        }
    }

    public static final class AccountSwitchDialogFragment extends DialogFragment {

        private static AccountSwitchDialogFragment newInstance(AccessToken accessToken, int position) {
            final Bundle args = new Bundle(1);
            args.putSerializable("accessToken", accessToken);
            args.putInt("position", position);

            final AccountSwitchDialogFragment f = new AccountSwitchDialogFragment();
            f.setArguments(args);
            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AccessToken accessToken = (AccessToken) getArguments().getSerializable("accessToken");
            final int position = getArguments().getInt("position");

            assert accessToken != null;

            final AccountSettingActivity activity = (AccountSettingActivity) getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(String.format(getString(R.string.confirm_remove_account), accessToken.getScreenName()));
            builder.setPositiveButton(
                    R.string.button_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.mAccountAdapter.remove(position);
                            JustawayApplication.getApplication().removeAccessToken(position);
                        }
                    }
            );
            builder.setNegativeButton(
                    R.string.button_no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }
            );
            return builder.create();
        }
    }
}
