package info.justaway;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import twitter4j.auth.AccessToken;

public class AccountSettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        ListView listView = (ListView) findViewById(R.id.list_view);

        AccountAdapter adapter = new AccountAdapter(this, R.layout.row_word);
        listView.setAdapter(adapter);

        ArrayList<AccessToken> accessTokens = JustawayApplication.getApplication().getAccessTokens();

        if (accessTokens != null) {
            for (AccessToken accessToken : accessTokens) {
                adapter.add(accessToken);
            }
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
            TextView screenName = (TextView) view.findViewById(R.id.word);
            TextView trash = (TextView) view.findViewById(R.id.trash);
            trash.setTypeface(JustawayApplication.getFontello());
            screenName.setText("@".concat(accessToken.getScreenName()));
            if (JustawayApplication.getApplication().getUserId() == accessToken.getUserId()) {
                screenName.setTextColor(getResources().getColor(R.color.holo_blue_bright));
                trash.setVisibility(View.GONE);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JustawayApplication.getApplication().setAccessToken(accessToken);
                    finish();
                }
            });

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(AccountSettingActivity.this)
                            .setTitle("@".concat(accessToken.getScreenName().concat(getString(R.string.confirm_remove_account))))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove(position);
                                            JustawayApplication.getApplication().removeAccessToken(position);

                                            finish();
                                        }
                                    })
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .show();
                }
            });
            return view;
        }
    }
}
