package info.justaway.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.model.TwitterManager;
import info.justaway.util.MessageUtil;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;

public class SearchAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> mStrings = new ArrayList<>();
    private ArrayList<twitter4j.SavedSearch> mSavedSearches = new ArrayList<>();
    private String mSearchWord;
    private LayoutInflater mInflater;
    private int mLayout;
    private Context mContext;
    private boolean mSavedMode = false;

    public SearchAdapter(Context context, int textViewResourceId) {
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
        if (mStrings.size() > position) {
            return mStrings.get(position);
        } else {
            return "";
        }
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

        final String word = getItem(position);

        ((TextView) view.findViewById(R.id.word)).setText(word);

        if (mSavedMode) {
            final twitter4j.SavedSearch savedSearch = mSavedSearches.get(position);
            view.findViewById(R.id.trash).setVisibility(View.VISIBLE);
            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) mContext;
                    activity.cancelSearch();
                    new AlertDialog.Builder(activity)
                            .setMessage(String.format(mContext.getString(R.string.confirm_destroy_saved_search), word))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new DestroySavedSearchesTask().execute(savedSearch.getId());
                                            mSavedSearches.remove(savedSearch);
                                            mStrings.remove(savedSearch.getQuery());
                                            if (mSavedSearches.size() > 0) {
                                                notifyDataSetChanged();
                                            } else {
                                                notifyDataSetInvalidated();
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }
            });
        } else {
            view.findViewById(R.id.trash).setVisibility(View.GONE);
        }

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
                    mStrings = new ArrayList<>();
                    for (twitter4j.SavedSearch savedSearche : mSavedSearches) {
                        mStrings.add(savedSearche.getQuery());
                    }
                } else {
                    mSearchWord = constraint.toString();
                    mStrings = new ArrayList<>();
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

    public void reload() {
        new SavedSearchesTask().execute();
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
            mSavedSearches.clear();
            for (SavedSearch savedSearch : savedSearches) {
                mSavedSearches.add(0, savedSearch);
            }
        }
    }

    public class DestroySavedSearchesTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                TwitterManager.getTwitter().destroySavedSearch(params[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                MessageUtil.showToast(R.string.toast_destroy_success);
            }
        }
    }
}
