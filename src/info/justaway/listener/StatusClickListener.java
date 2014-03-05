package info.justaway.listener;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;

import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.dialog.StatusMenuFragment;

public class StatusClickListener implements AdapterView.OnItemClickListener {

    private FragmentActivity mFragmentActivity;

    public StatusClickListener(FragmentActivity fragmentActivity) {
        mFragmentActivity = fragmentActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TwitterAdapter twitterAdapter = (TwitterAdapter) adapterView.getAdapter();
        new StatusMenuFragment(twitterAdapter.getItem(i))
                .setStatusActionListener(twitterAdapter.getStatusActionListener())
                .show(mFragmentActivity.getSupportFragmentManager(), "dialog");
    }
}
