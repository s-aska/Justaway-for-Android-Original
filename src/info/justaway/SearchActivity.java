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
import twitter4j.Twitter;

public class SearchActivity extends FragmentActivity {

    private Context context;
    private Twitter twitter;
    private EditText searchWords;
    private TwitterAdapter adapter;
    private ListView listView;
    private ProgressBar mFooter;
    private Query nextQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;

        final JustawayApplication application = JustawayApplication.getApplication();
        twitter = application.getTwitter();

        Button search = (Button) findViewById(R.id.search);
        Button tweet = (Button) findViewById(R.id.tweet);
        Typeface fontello = Typeface.createFromAsset(context.getAssets(), "fontello.ttf");
        search.setTypeface(fontello);
        tweet.setTypeface(fontello);

        searchWords = (EditText) findViewById(R.id.searchWords);
        mFooter = (ProgressBar) findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.listView);
        listView.setVisibility(View.GONE);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        adapter = new TwitterAdapter(context, R.layout.row_tweet);
        listView.setAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = new Query(searchWords.getText().toString());
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                adapter.clear();
                listView.setVisibility(View.GONE);
                mFooter.setVisibility(View.VISIBLE);
                nextQuery = null;
                new SearchTask().execute(query);
            }
        });
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("status", " " + searchWords.getText().toString());
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        if (query != null) {
            searchWords.setText(query);
            search.performClick();
        } else {
            application.showKeyboard(searchWords);
        }

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
        if (nextQuery != null) {
            mFooter.setVisibility(View.VISIBLE);
            new SearchTask().execute(nextQuery);
            nextQuery = null;
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
                QueryResult queryResult = twitter.search(query);
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
                nextQuery = queryResult.nextQuery();
            }
            int count = adapter.getCount();
            List<twitter4j.Status> statuses = queryResult.getTweets();
            for (twitter4j.Status status : statuses) {
                adapter.add(Row.newStatus(status));
            }

            listView.setVisibility(View.VISIBLE);
            if (count == 0) {
                listView.setSelection(0);
            }
            mFooter.setVisibility(View.GONE);

            // インテント経由で検索時にうまく閉じてくれないので入れている
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchWords.getWindowToken(), 0);
        }
    }
}