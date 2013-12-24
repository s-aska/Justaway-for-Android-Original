package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Query;
import twitter4j.QueryResult;

public class SearchActivity extends FragmentActivity {

    private Context mContext;
    private EditText mSearchWords;
    private TwitterAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private Query mNextQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;

        Button search = (Button) findViewById(R.id.search);
        Button tweet = (Button) findViewById(R.id.tweet);
        Typeface fontello = Typeface.createFromAsset(mContext.getAssets(), "fontello.ttf");
        search.setTypeface(fontello);
        tweet.setTypeface(fontello);

        mSearchWords = (EditText) findViewById(R.id.searchWords);
        mFooter = (ProgressBar) findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setVisibility(View.GONE);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(mContext, R.layout.row_tweet);
        mListView.setAdapter(mAdapter);

        // シングルタップでコンテキストメニューを開くための指定
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = new Query(mSearchWords.getText().toString());
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                mAdapter.clear();
                mListView.setVisibility(View.GONE);
                mFooter.setVisibility(View.VISIBLE);
                mNextQuery = null;
                new SearchTask().execute(query);
            }
        });
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra("status", " " + mSearchWords.getText().toString());
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        if (query != null) {
            mSearchWords.setText(query);
            search.performClick();
        } else {
            JustawayApplication.getApplication().showKeyboard(mSearchWords);
        }

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });
    }

    private void additionalReading() {
        if (mNextQuery != null) {
            mFooter.setVisibility(View.VISIBLE);
            new SearchTask().execute(mNextQuery);
            mNextQuery = null;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        JustawayApplication application = JustawayApplication.getApplication();
        application.onCreateContextMenuForStatus(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        return application.onContextItemSelected(this, item);
    }

    private class SearchTask extends AsyncTask<Query, Void, QueryResult> {
        @Override
        protected QueryResult doInBackground(Query... params) {
            Query query = params[0];
            try {
                QueryResult queryResult = JustawayApplication.getApplication().getTwitter().search(query);
                return queryResult;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(QueryResult queryResult) {
            if (queryResult == null) {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
                return;
            }
            if (queryResult.hasNext()) {
                mNextQuery = queryResult.nextQuery();
            }
            int count = mAdapter.getCount();
            List<twitter4j.Status> statuses = queryResult.getTweets();
            for (twitter4j.Status status : statuses) {
                mAdapter.add(Row.newStatus(status));
            }

            mListView.setVisibility(View.VISIBLE);
            if (count == 0) {
                mListView.setSelection(0);
            }
            mFooter.setVisibility(View.GONE);

            // インテント経由で検索時にうまく閉じてくれないので入れている
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mSearchWords.getWindowToken(), 0);
        }
    }
}