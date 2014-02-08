package info.justaway;

import android.app.Activity;
import android.content.Context;
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

        ListView listView = (ListView) findViewById(R.id.list);

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

            ((TextView) view.findViewById(R.id.word)).setText(accessToken.getScreenName());
            ((TextView) view.findViewById(R.id.trash)).setTypeface(JustawayApplication.getFontello());

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
                    remove(position);
                    // TODO: アカウント削除するか聞いて、削除する
                }
            });
            return view;
        }
    }
}
