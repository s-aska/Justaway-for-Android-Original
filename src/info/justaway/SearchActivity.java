package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;

public class SearchActivity extends FragmentActivity {

    private Context mContext;
    private EditText mSearchWords;
    private TwitterAdapter mAdapter;
    private SearchWordAdapter mSearchWordAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private Query mNextQuery;
    private ListView mSearchListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = this;

        Button search = (Button) findViewById(R.id.search);
        Button tweet = (Button) findViewById(R.id.tweet);
        Typeface fontello = JustawayApplication.getFontello();
        search.setTypeface(fontello);
        tweet.setTypeface(fontello);

        mSearchWords = (EditText) findViewById(R.id.searchWords);
        mFooter = (ProgressBar) findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        mSearchListView = (ListView) findViewById(R.id.search_list_view);

        // 保存された検索をViewに描写するアダプター
        mSearchWordAdapter = new SearchWordAdapter(mContext, R.layout.row_word);
        mSearchListView.setAdapter(mSearchWordAdapter);
        new GetSavedSearchesTask().execute();

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
                JustawayApplication.getApplication().hideKeyboard(mSearchWords);

                Query query = new Query(mSearchWords.getText().toString());
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

        mSearchWords.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                //EnterKeyが押されたかを判定
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {
                    JustawayApplication.getApplication().hideKeyboard(mSearchWords);

                    Query query = new Query(mSearchWords.getText().toString());
                    mAdapter.clear();
                    mListView.setVisibility(View.GONE);
                    mFooter.setVisibility(View.VISIBLE);
                    mNextQuery = null;
                    new SearchTask().execute(query);

                    return true;
                }
                return false;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        JustawayApplication.getApplication().onCreateContextMenu(this, menu, v, menuInfo, new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return JustawayApplication.getApplication().onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_search:
                new CreateSavedSearchTask().execute(mSearchWords.getText().toString());
                break;
        }
        return true;
    }

    private void additionalReading() {
        if (mNextQuery != null) {
            mFooter.setVisibility(View.VISIBLE);
            new SearchTask().execute(mNextQuery);
            mNextQuery = null;
        }
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
                mSearchListView.setVisibility(View.GONE);
            }
            mFooter.setVisibility(View.GONE);

            // インテント経由で検索時にうまく閉じてくれないので入れている
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mSearchWords.getWindowToken(), 0);
        }
    }

    private class CreateSavedSearchTask extends AsyncTask<String, Void, SavedSearch> {
        @Override
        protected SavedSearch doInBackground(String... params) {
            String query = params[0];
            try {
                SavedSearch savedSearch = JustawayApplication.getApplication().getTwitter().createSavedSearch(query);
                return savedSearch;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SavedSearch savedSearch) {
            if (savedSearch == null) {
                return;
            }
            JustawayApplication.showToast(getString(R.string.toast_save_success));
        }
    }

    private class DestroySavedSearchTask extends AsyncTask<Integer, Void, SavedSearch> {
        @Override
        protected SavedSearch doInBackground(Integer... params) {
            Integer id = params[0];
            try {
                SavedSearch savedSearch = JustawayApplication.getApplication().getTwitter().destroySavedSearch(id);
                return savedSearch;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(SavedSearch savedSearch) {
            if (savedSearch == null) {
                return;
            }
            JustawayApplication.showToast(getString(R.string.toast_destroy_success));
        }
    }

    private class GetSavedSearchesTask extends AsyncTask<Void, Void, ResponseList<SavedSearch>> {
        @Override
        protected ResponseList<SavedSearch> doInBackground(Void... params) {
            try {
                ResponseList<SavedSearch> savedSearches = JustawayApplication.getApplication().getTwitter().getSavedSearches();
                return savedSearches;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<SavedSearch> savedSearches) {
            if (savedSearches == null) {
                return;
            }
            for (SavedSearch savedSearch : savedSearches) {
                mSearchWordAdapter.insert(savedSearch, 0);
            }
        }
    }

    public class SearchWordAdapter extends ArrayAdapter<SavedSearch> {

        private ArrayList<SavedSearch> mWordLists = new ArrayList<SavedSearch>();
        private Context mContext;
        private LayoutInflater mInflater;
        private int mLayout;

        public SearchWordAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mContext = context;
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(SavedSearch word) {
            super.add(word);
            mWordLists.add(word);
        }

        @Override
        public void insert(SavedSearch word, int index) {
            super.insert(word, index);
            mWordLists.add(index, word);
        }

        public void remove(int position) {
            super.remove(mWordLists.remove(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            final SavedSearch word = mWordLists.get(position);

            ((TextView) view.findViewById(R.id.word)).setText(word.getQuery());
            ((TextView) view.findViewById(R.id.trash)).setTypeface(JustawayApplication.getFontello());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSearchWords.setText(word.getQuery());
                    Query query = new Query(word.getQuery());
                    new SearchTask().execute(query);
                }
            });

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                    new DestroySavedSearchTask().execute(word.getId());
                }
            });
            return view;
        }
    }
}
