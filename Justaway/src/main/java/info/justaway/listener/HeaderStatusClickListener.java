package info.justaway.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;

import info.justaway.adapter.TwitterAdapter;

public class HeaderStatusClickListener extends StatusClickListener {

    public HeaderStatusClickListener(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public TwitterAdapter getAdapter(AdapterView<?> adapterView) {
        HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
        return (TwitterAdapter) headerViewListAdapter.getWrappedAdapter();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        super.onItemClick(adapterView, view, i - 1, l);
    }
}
