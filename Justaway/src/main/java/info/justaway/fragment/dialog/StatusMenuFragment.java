package info.justaway.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.PostActivity;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.SearchActivity;
import info.justaway.fragment.AroundFragment;
import info.justaway.fragment.RetweetersFragment;
import info.justaway.fragment.TalkFragment;
import info.justaway.listener.StatusActionListener;
import info.justaway.model.Row;
import info.justaway.plugin.TwiccaPlugin;
import info.justaway.settings.MuteSettings;
import info.justaway.task.DestroyStatusTask;
import twitter4j.DirectMessage;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class StatusMenuFragment extends DialogFragment {

    private FragmentActivity mActivity;
    private JustawayApplication mApplication;

    static final int CLOSED_MENU_DELAY = 800;

    private List<ResolveInfo> mTwiccaPlugins;
    private StatusActionListener mStatusActionListener;

    public static StatusMenuFragment newInstance(Row row) {
        Bundle args = new Bundle();
        if (row.isDirectMessage()) {
            args.putSerializable("directMessage", row.getMessage());
        } else {
            args.putSerializable("status", row.getStatus());
        }
        if (row.isFavorite()) {
            args.putSerializable("favoriteSourceUser", row.getSource());
        }
        final StatusMenuFragment f = new StatusMenuFragment();
        f.setArguments(args);
        return f;
    }

    public StatusMenuFragment() {
    }

    public StatusMenuFragment setStatusActionListener(StatusActionListener statusActionListener) {
        mStatusActionListener = statusActionListener;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mActivity = getActivity();
        mApplication = JustawayApplication.getApplication();

        final MenuAdapter adapter = new MenuAdapter(getActivity(), R.layout.row_menu);
        ListView listView = new ListView(mActivity);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Menu menu = adapter.getItem(i);
                menu.callback.run();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setView(listView);

        /**
         * DM
         */
        final DirectMessage directMessage = (DirectMessage) getArguments().getSerializable("directMessage");
        if (directMessage != null) {
            builder.setTitle(directMessage.getSenderScreenName());

            /**
             * 返信(DM)
             */
            adapter.add(new Menu(R.string.context_menu_reply_direct_message, new Runnable() {
                @Override
                public void run() {
                    String text = "D " + directMessage.getSenderScreenName() + " ";
                    tweet(text, text.length(), null);
                    dismiss();
                }
            }));

            /**
             * ツイ消し(DM)
             */
            adapter.add(new Menu(R.string.context_menu_destroy_direct_message, new Runnable() {
                @Override
                public void run() {
                    MainActivity mainActivity = (MainActivity) mActivity;
                    mainActivity.doDestroyDirectMessage(directMessage.getId());
                    dismiss();
                }
            }));

            /**
             * ツイート内のメンション
             */
            for (final UserMentionEntity mention : directMessage.getUserMentionEntities()) {
                adapter.add(new Menu("@" + mention.getScreenName(), new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mActivity, ProfileActivity.class);
                        intent.putExtra("screenName", mention.getScreenName());
                        mActivity.startActivity(intent);
                    }
                }));
            }

            /**
             * ツイート内のURL
             */
            URLEntity[] urls = directMessage.getURLEntities();
            for (final URLEntity url : urls) {
                adapter.add(new Menu(url.getExpandedURL(), new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.getExpandedURL()));
                        mActivity.startActivity(intent);
                    }
                }));
            }

            return builder.create();
        }

        /**
         * ツイート
         */
        final Status status = (Status) getArguments().getSerializable("status");
        if (status == null) {
            return null;
        }
        final Status retweet = status.getRetweetedStatus();
        final Status source = retweet != null ? retweet : status;
        final UserMentionEntity[] mentions = source.getUserMentionEntities();
        Boolean isPublic = !source.getUser().isProtected();

        builder.setTitle(status.getText());

        /**
         * リプ
         */
        adapter.add(new Menu(R.string.context_menu_reply, new Runnable() {
            @Override
            public void run() {
                String text;
                if (source.getUser().getId() == mApplication.getUserId() && mentions.length == 1) {
                    text = "@" + mentions[0].getScreenName() + " ";
                } else {
                    text = "@" + source.getUser().getScreenName() + " ";
                }
                tweet(text, text.length(), status);
                dismiss();
            }
        }));

        /**
         * 全員にリプ
         */
        if (mentions.length > 1 || (mentions.length == 1 && !mentions[0].getScreenName().equals(mApplication.getScreenName()))) {
            adapter.add(new Menu(R.string.context_menu_reply_all, new Runnable() {
                @Override
                public void run() {
                    String text = "";
                    if (source.getUser().getId() != mApplication.getUserId()) {
                        text = "@" + source.getUser().getScreenName() + " ";
                    }
                    for (UserMentionEntity mention : mentions) {
                        if (source.getUser().getScreenName().equals(mention.getScreenName())) {
                            continue;
                        }
                        if (mApplication.getScreenName().equals(mention.getScreenName())) {
                            continue;
                        }
                        text = text.concat("@" + mention.getScreenName() + " ");
                    }
                    tweet(text, text.length(), status);
                    dismiss();
                }
            }));
        }

        /**
         * 引用
         */
        if (isPublic) {
            adapter.add(new Menu(R.string.context_menu_qt, new Runnable() {
                @Override
                public void run() {
                    String text = " https://twitter.com/" + source.getUser().getScreenName()
                            + "/status/" + String.valueOf(source.getId());
                    tweet(text, 0, source);
                    dismiss();
                }
            }));
        }

        /**
         * ふぁぼ / あんふぁぼ
         */
        if (mApplication.isFav(status)) {
            adapter.add(new Menu(R.string.context_menu_destroy_favorite, new Runnable() {
                @Override
                public void run() {
                    mApplication.doDestroyFavorite(status.getId());
                    mStatusActionListener.notifyDataSetChanged();
                    dismiss();
                }
            }));
        } else {
            adapter.add(new Menu(R.string.context_menu_create_favorite, new Runnable() {
                @Override
                public void run() {
                    mApplication.doFavorite(status.getId());
                    mStatusActionListener.notifyDataSetChanged();
                    dismiss();
                }
            }));
        }

        /**
         * 自分のツイートまたはRT
         */
        if (status.getUser().getId() == mApplication.getUserId()) {

            /**
             * 自分のRT
             */
            if (retweet != null) {

                /**
                 * RT解除
                 */
                adapter.add(new Menu(R.string.context_menu_destroy_retweet, new Runnable() {
                    @Override
                    public void run() {
                        mApplication.doDestroyRetweet(status);
                        mStatusActionListener.notifyDataSetChanged();
                        dismiss();
                    }
                }));
            }

            /**
             * 自分のツイート
             */
            else {

                /**
                 * ツイ消し
                 */
                adapter.add(new Menu(R.string.context_menu_destroy_status, new Runnable() {
                    @Override
                    public void run() {
                        new DestroyStatusTask(status.getId())
                                .setStatusActionListener(mStatusActionListener)
                                .execute();
                        dismiss();
                    }
                }));
            }
        }

        /**
         * 自分がRTした事があるツイート
         */
        else if (mApplication.getRtId(status) != null) {

            /**
             * RT解除
             */
            adapter.add(new Menu(R.string.context_menu_destroy_retweet, new Runnable() {
                @Override
                public void run() {
                    mApplication.doDestroyRetweet(status);
                    mStatusActionListener.notifyDataSetChanged();
                    dismiss();
                }
            }));
        } else {

            /**
             * 非鍵垢
             */
            if (isPublic) {

                /**
                 * 未ふぁぼ
                 */
                if (!mApplication.isFav(status)) {

                    /**
                     * ふぁぼ＆RT
                     */
                    adapter.add(new Menu(R.string.context_menu_favorite_and_retweet, new Runnable() {
                        @Override
                        public void run() {
                            mApplication.doFavorite(status.getId());
                            mApplication.doRetweet(status.getId());
                            mStatusActionListener.notifyDataSetChanged();
                            dismiss();
                        }
                    }));
                }

                /**
                 * RT
                 */
                adapter.add(new Menu(R.string.context_menu_retweet, new Runnable() {
                    @Override
                    public void run() {
                        mApplication.doRetweet(status.getId());
                        mStatusActionListener.notifyDataSetChanged();
                        dismiss();
                    }
                }));
            }
        }

        /**
         * RTある時
         */
        if (source.getRetweetCount() > 0) {

            /**
             * RTした人を表示
             */
            adapter.add(new Menu(R.string.context_menu_show_retweeters, new Runnable() {
                @Override
                public void run() {
                    RetweetersFragment retweetersFragment = new RetweetersFragment();
                    Bundle retweetsArgs = new Bundle();
                    retweetsArgs.putLong("statusId", source.getId());
                    retweetersFragment.setArguments(retweetsArgs);
                    retweetersFragment.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }));
        }

        /**
         * リプの時
         */
        if (source.getInReplyToStatusId() > 0) {

            /**
             * 会話を表示
             */
            adapter.add(new Menu(R.string.context_menu_talk, new Runnable() {
                @Override
                public void run() {
                    TalkFragment dialog = new TalkFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("status", source);
                    dialog.setArguments(args);
                    dialog.show(mActivity.getSupportFragmentManager(), "dialog");
                }
            }));
        }

        /**
         * 前後のツイートを表示
         */
        adapter.add(new Menu(R.string.context_menu_show_around, new Runnable() {
            @Override
            public void run() {
                AroundFragment aroundFragment = new AroundFragment();
                Bundle aroundArgs = new Bundle();
                aroundArgs.putSerializable("status", source);
                aroundFragment.setArguments(aroundArgs);
                aroundFragment.show(mActivity.getSupportFragmentManager(), "dialog");
            }
        }));

        /**
         * ツイート内のURL
         */
        URLEntity[] urls = source.getURLEntities();
        for (final URLEntity url : urls) {
            adapter.add(new Menu(url.getExpandedURL(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.getExpandedURL()));
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * ツイート内のURL(画像)
         */
        URLEntity[] medias = source.getMediaEntities();
        for (final URLEntity url : medias) {
            adapter.add(new Menu(url.getExpandedURL(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.getExpandedURL()));
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * ツイート内のハッシュタグ
         */
        HashtagEntity[] hashtags = source.getHashtagEntities();
        for (final HashtagEntity hashtag : hashtags) {
            adapter.add(new Menu("#" + hashtag.getText(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mActivity, SearchActivity.class);
                    intent.putExtra("query", "#" + hashtag.getText());
                    mActivity.startActivity(intent);
                }
            }));
        }

        LongSparseArray<Boolean> users = new LongSparseArray<Boolean>();

        /**
         * ふぁぼした人
         */
        final User favoriteSourceUser = (User) getArguments().getSerializable("favoriteSourceUser");
        if (favoriteSourceUser != null) {
            users.put(favoriteSourceUser.getId(), true);
            adapter.add(new Menu("@" + favoriteSourceUser.getScreenName(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra("screenName", favoriteSourceUser.getScreenName());
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * RTした人
         */
        if (retweet != null && users.get(status.getUser().getId()) == null) {
            users.put(status.getUser().getId(), true);
            adapter.add(new Menu("@" + status.getUser().getScreenName(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra("screenName", status.getUser().getScreenName());
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * ツイート内のメンション
         */
        for (final UserMentionEntity mention : mentions) {
            if (users.get(mention.getId()) != null) {
                continue;
            }
            users.put(mention.getId(), true);
            adapter.add(new Menu("@" + mention.getScreenName(), new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra("screenName", mention.getScreenName());
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * 非鍵垢
         */
        if (isPublic) {

            /**
             * TwiccaPlugin
             */
            if (mTwiccaPlugins == null) {
                mTwiccaPlugins = TwiccaPlugin.getResolveInfo(mActivity.getPackageManager(),
                        TwiccaPlugin.TWICCA_ACTION_SHOW_TWEET);
            }
            if (!mTwiccaPlugins.isEmpty()) {
                PackageManager pm = mActivity.getPackageManager();
                for (final ResolveInfo resolveInfo : mTwiccaPlugins) {
                    if (pm == null || resolveInfo.activityInfo == null) {
                        continue;
                    }
                    String label = (String) resolveInfo.activityInfo.loadLabel(pm);
                    if (label == null) {
                        continue;
                    }
                    adapter.add(new Menu(label, new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = TwiccaPlugin.createIntentShowTweet(status,
                                    resolveInfo.activityInfo.packageName,
                                    resolveInfo.activityInfo.name);
                            mActivity.startActivity(intent);
                        }
                    }));
                }
            }

            /**
             * ツイートを共有
             */
            adapter.add(new Menu(R.string.context_menu_share_url, new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" + source.getUser().getScreenName()
                            + "/status/" + String.valueOf(source.getId()));
                    mActivity.startActivity(intent);
                }
            }));
        }

        /**
         * viaをミュート
         */
        adapter.add(new Menu(String.format(mActivity.getString(R.string.context_menu_mute), mApplication.getClientName(source.getSource())), new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(String.format(getString(R.string.context_create_mute), mApplication.getClientName(source.getSource())))
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MuteSettings muteSettings = mApplication.getMuteSettings();
                                muteSettings.addSource(mApplication.getClientName(source.getSource()));
                                muteSettings.saveMuteSettings();
                                JustawayApplication.showToast(R.string.toast_create_mute);
                                dismiss();
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        }));

        /**
         * ハッシュタグをミュート
         */
        for (final HashtagEntity hashtag : hashtags) {
            adapter.add(new Menu(String.format(mActivity.getString(R.string.context_menu_mute), "#".concat(hashtag.getText())), new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(String.format(getString(R.string.context_create_mute), "#".concat(hashtag.getText())))
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MuteSettings muteSettings = mApplication.getMuteSettings();
                                    muteSettings.addWord("#" + hashtag.getText());
                                    muteSettings.saveMuteSettings();
                                    JustawayApplication.showToast(R.string.toast_create_mute);
                                    dismiss();
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }));
        }

        /**
         * ユーザーをミュート
         */
        adapter.add(new Menu(String.format(mActivity.getString(R.string.context_menu_mute), "@".concat(source.getUser().getScreenName())), new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(String.format(getString(R.string.context_create_mute), "@".concat(source.getUser().getScreenName())))
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MuteSettings muteSettings = mApplication.getMuteSettings();
                                muteSettings.addUser(source.getUser().getId(), source.getUser().getScreenName());
                                muteSettings.saveMuteSettings();
                                JustawayApplication.showToast(R.string.toast_create_mute);
                                dismiss();
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        }));

        return builder.create();
    }

    private EditText getQuickTweetEdit() {
        View singleLineTweet = mActivity.findViewById(R.id.quick_tweet_layout);
        if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
            return (EditText) mActivity.findViewById(R.id.quick_tweet_edit);
        }
        return null;
    }

    private void tweet(String text, int selection, Status inReplyToStatus) {
        EditText editStatus = getQuickTweetEdit();
        if (editStatus != null) {
            editStatus.requestFocus();
            editStatus.setText(text);
            if (selection > 0) {
                editStatus.setSelection(selection);
            }
            if (inReplyToStatus != null) {
                ((MainActivity) mActivity).setInReplyToStatus(inReplyToStatus);
            }
            mApplication.showKeyboard(editStatus, CLOSED_MENU_DELAY);
        } else {
            Intent intent = new Intent(mActivity, PostActivity.class);
            intent.putExtra("status", text);
            if (selection > 0) {
                intent.putExtra("selection", selection);
            }
            if (inReplyToStatus != null) {
                intent.putExtra("inReplyToStatus", inReplyToStatus);
            }
            mActivity.startActivity(intent);
        }
    }

    public class Menu {
        public Runnable callback;
        public String label;

        public Menu(String label, Runnable callback) {
            this.label = label;
            this.callback = callback;
        }

        public Menu(int resId, Runnable callback) {
            this.label = getString(resId);
            this.callback = callback;
        }
    }

    public class MenuAdapter extends ArrayAdapter<Menu> {

        private ArrayList<Menu> mMenuList = new ArrayList<Menu>();
        private LayoutInflater mInflater;
        private int mLayout;

        public MenuAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(Menu menu) {
            super.add(menu);
            mMenuList.add(menu);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
                if (view == null) {
                    return null;
                }
            }

            Menu menu = mMenuList.get(position);

            ((TextView) view).setText(menu.label);

            return view;
        }
    }
}
