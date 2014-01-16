package info.justaway;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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

public class SearchActivity extends BaseActivity {

    private Context mContext;
    private EditText mSearchWords;
    private TwitterAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private View mHeader;
    private Query mNextQuery;
    private ListView mSearchListView;

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
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        mSearchListView = (ListView) findViewById(R.id.search_list_view);
        SearchWordAdapter searchWordAdapter = new SearchWordAdapter(mContext, R.layout.row_word);
        mSearchListView.setAdapter(searchWordAdapter);

        SaveLoadTraining saveLoadTraining = new SaveLoadTraining();
        ArrayList<String> searchWords = saveLoadTraining.loadArray();

        for (String searchWord : searchWords) {
            searchWordAdapter.add(searchWord);
        }

        mHeader = View.inflate(getApplicationContext(), R.layout.row_seve_word, null);

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

        mHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SearchActivity.this)
                        .setTitle(mSearchWords.getText().toString() + " を検索に保存しますか？")
                        .setPositiveButton(
                                R.string.save,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 検索単語を保存する
                                        SaveLoadTraining saveLoadTraining = new SaveLoadTraining();
                                        ArrayList<String> searchList = saveLoadTraining.loadArray();
                                        searchList.add(mSearchWords.getText().toString());
                                        saveLoadTraining.saveArray(searchList);

                                    }
                                })
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();
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
        onCreateContextMenuForStatus(menu, v, menuInfo);
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

            mListView.removeHeaderView(mHeader);
            mListView.setVisibility(View.VISIBLE);
            if (count == 0) {
                mListView.setSelection(0);
                mSearchListView.setVisibility(View.GONE);
                // TODO: 既に登録されているものなら表示させない
                mListView.addHeaderView(mHeader);
            }
            mFooter.setVisibility(View.GONE);

            // インテント経由で検索時にうまく閉じてくれないので入れている
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mSearchWords.getWindowToken(), 0);
        }
    }

    public class SearchWordAdapter extends ArrayAdapter<String> {

        private ArrayList<String> mWordLists = new ArrayList<String>();
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
        public void add(String draft) {
            super.add(draft);
            mWordLists.add(draft);
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

            final String word = mWordLists.get(position);

            ((TextView) view.findViewById(R.id.word)).setText(word);
            ((TextView) view.findViewById(R.id.trash)).setTypeface(Typeface.createFromAsset(getAssets(), "fontello.ttf"));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSearchWords.setText(word);
                    mWordLists.remove(position);
                    SaveLoadTraining saveLoadTraining = new SaveLoadTraining();
                    saveLoadTraining.saveArray(mWordLists);

                }
            });

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                    SaveLoadTraining saveLoadTraining = new SaveLoadTraining();
                    saveLoadTraining.saveArray(mWordLists);
                }
            });
            return view;
        }
    }

    /**
     * SharedPreferencesにArrayListを突っ込む
     */
    public class SaveLoadTraining {

        private Context context;
        public static final String PREFS_NAME = "SearchListFile";
        private ArrayList<String> list;

        public SaveLoadTraining() {
            this.context = mContext;
        }

        public void saveArray(ArrayList<String> list) {
            this.list = list;

            SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            int size = list.size();
            editor.putInt("list_size", size);

            for (int i = 0; i < size; i++) {
                editor.remove("list_" + i);
            }
            for (int i = 0; i < size; i++) {
                editor.putString("list_" + i, list.get(i));
            }
            editor.commit();
        }

        public ArrayList<String> loadArray() {
            SharedPreferences file = context.getSharedPreferences(PREFS_NAME, 0);
            list = new ArrayList<String>();
            int size = file.getInt("list_size", 0);

            for (int i = 0; i < size; i++) {
                String draft = file.getString("list_" + i, null);
                list.add(draft);
            }
            return list;
        }
    }
}