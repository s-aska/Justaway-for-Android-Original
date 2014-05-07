package info.justaway.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.R;
import info.justaway.model.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;

public class UserSearchAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> mStrings = new ArrayList<String>();
    private ArrayList<String> mSavedSearches = new ArrayList<String>();
    private String mSearchWord;
    private LayoutInflater mInflater;
    private int mLayout;
    private Context mContext;
    private boolean mSavedMode = false;

    public UserSearchAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = textViewResourceId;
        new SavedSearchesTask().execute();
    }

    public boolean isSavedMode() {
        return mSavedMode;
    }

    @Override
    public String getItem(int position) {
        return mStrings.get(position);
    }

    @Override
    public int getCount() {
        return mStrings.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
        }

        assert view != null;

        ((TextView) view.findViewById(R.id.word)).setText(getItem(position));

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                mSavedMode = constraint == null || constraint.length() == 0;
                if (mSavedMode) {
                    mStrings = mSavedSearches;
                } else {
                    mSearchWord = constraint.toString();
                    mStrings = new ArrayList<String>();
                    mStrings.add(mSearchWord + mContext.getString(R.string.label_search_tweet));
                    mStrings.add(mSearchWord + mContext.getString(R.string.label_search_user));
                    mStrings.add("@" + mSearchWord + mContext.getString(R.string.label_display_profile));
                }
                filterResults.values = mStrings;
                filterResults.count = mStrings.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public String convertResultToString(Object resultValue) {
                //ここでフィルタリングした値を選択したときに返す値を実装
                if (mSavedMode) {
                    return (String) resultValue;
                } else {
                    return mSearchWord;
                }
            }
        };
    }

    public class SavedSearchesTask extends AsyncTask<Void, Void, ResponseList<SavedSearch>> {
        @Override
        protected ResponseList<SavedSearch> doInBackground(Void... params) {
            try {
                return TwitterManager.getTwitter().getSavedSearches();
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
                mSavedSearches.add(0, savedSearch.getQuery());
            }
        }
    }
}
