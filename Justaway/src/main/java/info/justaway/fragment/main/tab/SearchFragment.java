package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import info.justaway.model.Row;
import info.justaway.model.TabManager;
import info.justaway.model.TwitterManager;
import twitter4j.Query;
import twitter4j.QueryResult;

/**
 * 検索タブ
 */
public class SearchFragment extends BaseFragment {

    /**
     * このタブを表す固有のID、ユーザーリストで正数を使うため負数を使う
     */
    public long getTabId() {
        return TabManager.SEARCH_TAB_ID - Math.abs(mSearchWord.hashCode());
    }

    public String getSearchWord() {
        return mSearchWord;
    }

    private Query mQuery;
    private String mSearchWord;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mSearchWord == null) {
            mSearchWord = getArguments().getString("searchWord");
        }
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * このタブに表示するツイートの定義
     * @param row ストリーミングAPIから受け取った情報（ツイート＋ふぁぼ）
     *            CreateFavoriteEventをキャッチしている為、ふぁぼイベントを受け取ることが出来る
     * @return trueは表示しない、falseは表示する
     */
    @Override
    protected boolean isSkip(Row row) {
        if (row.isStatus()) {
            if (row.getStatus().getText().contains(mSearchWord)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void taskExecute() {
        new SearchTask().execute();
    }

    private class SearchTask extends AsyncTask<Void, Void, QueryResult> {
        @Override
        protected QueryResult doInBackground(Void... params) {
            try {
                Query query;
                if (mQuery != null && !mReloading) {
                    query = mQuery;
                } else {
                    query = new Query(mSearchWord.concat(" exclude:retweets"));
                }
                return TwitterManager.getTwitter().search(query);
            } catch (OutOfMemoryError e) {
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(QueryResult queryResult) {
            mFooter.setVisibility(View.GONE);
            if (queryResult == null) {
                mReloading = false;
                mPullToRefreshLayout.setRefreshComplete();
                mListView.setVisibility(View.VISIBLE);
                mQuery = null;
                return;
            }
            if (mReloading) {
                clear();
                for (twitter4j.Status status : queryResult.getTweets()) {
                    mAdapter.add(Row.newStatus(status));
                }
                mReloading = false;
                if (queryResult.hasNext()) {
                    mQuery = queryResult.nextQuery();
                    mAutoLoader = true;
                } else {
                    mQuery = null;
                    mAutoLoader = false;
                }
                mPullToRefreshLayout.setRefreshComplete();
            } else {
                for (twitter4j.Status status : queryResult.getTweets()) {
                    mAdapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                mQuery = queryResult.nextQuery();
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }
}
