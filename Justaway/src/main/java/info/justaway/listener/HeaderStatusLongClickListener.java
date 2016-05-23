package info.justaway.listener;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;

import info.justaway.adapter.TwitterAdapter;

public class HeaderStatusLongClickListener extends StatusLongClickListener {
    public HeaderStatusLongClickListener(Activity activity) {
        super(activity);
    }

    @Override
    public TwitterAdapter getAdapter(AdapterView<?> adapterView) {
        HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
        return (TwitterAdapter) headerViewListAdapter.getWrappedAdapter();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        return super.onItemLongClick(adapterView, view, position - 1, id);
    }
}
