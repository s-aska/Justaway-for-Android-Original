package info.justaway.listener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;

import info.justaway.JustawayApplication;
import info.justaway.PostActivity;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.AroundFragment;
import info.justaway.fragment.TalkFragment;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class StatusLongClickListener implements AdapterView.OnItemLongClickListener {

    private TwitterAdapter mAdapter;
    private FragmentActivity mActivity;

    public StatusLongClickListener(TwitterAdapter adapter, Activity activity) {
        mAdapter = adapter;
        mActivity = (FragmentActivity) activity;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        Bundle args = new Bundle();
        String action = JustawayApplication.getApplication().getLongTapAction();

        if (mAdapter.getItem(position).isDirectMessage()) {
            return true;
        }

        Status status = mAdapter.getItem(position).getStatus();

        if (action.equals("quote")) {
            String text = " https://twitter.com/" + status.getUser().getScreenName()
                    + "/status/" + String.valueOf(status.getId());
            tweet(text, text.length(), status.getId());
        } else if (action.equals("talk")) {
            TalkFragment dialog = new TalkFragment();
            args.putLong("statusId", status.getId());
            dialog.setArguments(args);
            dialog.show(mActivity.getSupportFragmentManager(), "dialog");
        } else if (action.equals("show_around")) {
            AroundFragment aroundFragment = new AroundFragment();
            Bundle aroundArgs = new Bundle();
            aroundArgs.putSerializable("status", status);
            aroundFragment.setArguments(aroundArgs);
            aroundFragment.show(mActivity.getSupportFragmentManager(), "dialog");
        } else if (action.equals("share_url")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" + status.getUser().getScreenName()
                    + "/status/" + String.valueOf(status.getId()));
            mActivity.startActivity(intent);
        } else if (action.equals("reply_all")) {
            String text = "";
            if (status.getUser().getId() != JustawayApplication.getApplication().getUserId()) {
                text = "@" + status.getUser().getScreenName() + " ";
            }
            for (UserMentionEntity mention : status.getUserMentionEntities()) {
                if (status.getUser().getScreenName().equals(mention.getScreenName())) {
                    continue;
                }
                if (JustawayApplication.getApplication().getScreenName().equals(mention.getScreenName())) {
                    continue;
                }
                text = text.concat("@" + mention.getScreenName() + " ");
            }
            tweet(text, text.length(), status.getId());
        }
        return true;
    }

    private void tweet(String text, int selection, long inReplyToStatusId) {
        Intent intent = new Intent(mActivity, PostActivity.class);
        intent.putExtra("status", text);
        if (selection > 0) {
            intent.putExtra("selection", selection);
        }
        if (inReplyToStatusId > 0L) {
            intent.putExtra("inReplyToStatusId", inReplyToStatusId);
        }
        mActivity.startActivity(intent);
    }
}
