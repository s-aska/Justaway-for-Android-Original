package info.justaway.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.R;

public class UserSearchAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> mStrings = new ArrayList<String>();
    private String mSearchWord;
    private LayoutInflater mInflater;
    private int mLayout;
    private Context mContext;

    public UserSearchAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mLayout = textViewResourceId;
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
        final String str = mStrings.get(position);

        ((TextView) view.findViewById(R.id.word)).setText(str);

        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    mStrings = new ArrayList<String>();
                    mSearchWord = constraint.toString();
                    mStrings.add(mSearchWord + mContext.getString(R.string.label_search_tweet));
                    mStrings.add(mSearchWord + mContext.getString(R.string.label_search_user));
                    mStrings.add("@" + mSearchWord + mContext.getString(R.string.label_display_profile));

                    filterResults.values = mStrings;
                    filterResults.count = mStrings.size();
                }
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
                return mSearchWord;
            }
        };
        return filter;
    }
}
