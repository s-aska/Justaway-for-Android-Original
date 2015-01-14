package info.justaway.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.LongSparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import info.justaway.BuildConfig;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.event.AlertDialogEvent;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.FavRetweetManager;
import info.justaway.model.Row;
import info.justaway.model.UserIconManager;
import info.justaway.settings.BasicSettings;
import info.justaway.settings.MuteSettings;
import info.justaway.util.ActionUtil;
import info.justaway.util.ImageUtil;
import info.justaway.util.MessageUtil;
import info.justaway.util.StatusUtil;
import info.justaway.util.ThemeUtil;
import info.justaway.util.TimeUtil;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;

public class TwitterAdapter extends ArrayAdapter<Row> {

    static class ViewHolder {
        @InjectView(R.id.action_container) ViewGroup mActionContainer;
        @InjectView(R.id.action_icon) TextView mActionIcon;
        @InjectView(R.id.action_by_display_name) TextView mActionByDisplayName;
        @InjectView(R.id.action_by_screen_name) TextView mActionByScreenName;
        @InjectView(R.id.icon) ImageView mIcon;
        @InjectView(R.id.display_name) TextView mDisplayName;
        @InjectView(R.id.screen_name) TextView mScreenName;
        @InjectView(R.id.lock) TextView mLock;
        @InjectView(R.id.datetime_relative) TextView mDatetimeRelative;
        @InjectView(R.id.status) TextView mStatus;
        @InjectView(R.id.images_container) ViewGroup mImagesContainer;
        @InjectView(R.id.menu_and_via_container) ViewGroup mMenuAndViaContainer;
        @InjectView(R.id.do_reply) TextView mDoReply;
        @InjectView(R.id.do_retweet) TextView mDoRetweet;
        @InjectView(R.id.retweet_count) TextView mRetweetCount;
        @InjectView(R.id.do_fav) TextView mDoFav;
        @InjectView(R.id.fav_count) TextView mFavCount;
        @InjectView(R.id.via) TextView mVia;
        @InjectView(R.id.datetime) TextView mDatetime;
        @InjectView(R.id.retweet_container) View mRetweetContainer;
        @InjectView(R.id.retweet_icon) ImageView mRetweetIcon;
        @InjectView(R.id.retweet_by) TextView mRetweetBy;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout;
    private int mColorBlue = 0;
    private static final int LIMIT = 100;
    private int mLimit = LIMIT;
    private final LongSparseArray<Boolean> mIdMap = new LongSparseArray<>();

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mLayout = textViewResourceId;
    }

    public Context getContext() {
        return mContext;
    }

    public void extensionAdd(Row row) {
        if (MuteSettings.isMute(row)) {
            return;
        }
        if (exists(row)) {
            return;
        }
        super.add(row);
        if (row.isStatus()) {
            mIdMap.put(row.getStatus().getId(), true);
        }
        filter(row);
        mLimit++;
    }

    @Override
    public void add(Row row) {
        if (MuteSettings.isMute(row)) {
            return;
        }
        if (exists(row)) {
            return;
        }
        super.add(row);
        if (row.isStatus()) {
            mIdMap.put(row.getStatus().getId(), true);
        }
        filter(row);
        limitation();
    }

    @Override
    public void insert(Row row, int index) {
        if (MuteSettings.isMute(row)) {
            return;
        }
        if (exists(row)) {
            return;
        }
        super.insert(row, index);
        if (row.isStatus()) {
            mIdMap.put(row.getStatus().getId(), true);
        }
        filter(row);
        limitation();
    }

    @Override
    public void remove(Row row) {
        super.remove(row);
        if (row.isStatus()) {
            mIdMap.remove(row.getStatus().getId());
        }
    }

    public boolean exists(Row row) {
        return row.isStatus() && mIdMap.get(row.getStatus().getId(), false);
    }

    private void filter(Row row) {
        Status status = row.getStatus();
        if (status != null && status.isRetweeted()) {
            Status retweet = status.getRetweetedStatus();
            if (retweet != null && status.getUser().getId() == AccessTokenManager.getUserId()) {
                FavRetweetManager.setRtId(retweet.getId(), status.getId());
            }
        }
    }

    @SuppressWarnings("unused")
    public void replaceStatus(Status status) {
        for (int i = 0; i < getCount(); i++) {
            Row row = getItem(i);
            if (!row.isDirectMessage() && row.getStatus().getId() == status.getId()) {
                row.setStatus(status);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public int removeStatus(long statusId) {
        int position = 0;
        for (int i = 0; i < getCount(); i++) {
            Row row = getItem(i);
            if (!row.isDirectMessage() && row.getStatus().getId() == statusId) {
                remove(row);
                return position;
            }
            position++;
        }
        return -1;
    }

    public void removeDirectMessage(long directMessageId) {
        for (int i = 0; i < getCount(); i++) {
            Row row = getItem(i);
            if (row.isDirectMessage() && row.getMessage().getId() == directMessageId) {
                remove(row);
                break;
            }
        }
    }

    public void limitation() {
        int size = getCount();
        if (size > mLimit) {
            int count = size - mLimit;
            for (int i = 0; i < count; i++) {
                super.remove(getItem(size - i - 1));
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        mIdMap.clear();
        mLimit = LIMIT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // ビューを受け取る
        View view = convertView;

        if (view == null) {

            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
            if (view == null) {
                return null;
            }
            holder = new ViewHolder(view);
            holder.mStatus.setTag(12);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Integer fontSize = BasicSettings.getFontSize();
        if (!fontSize.equals(holder.mStatus.getTag())) {
            holder.mStatus.setTag(fontSize);
            holder.mStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            holder.mDisplayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            holder.mScreenName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize - 2);
            holder.mDatetimeRelative.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize - 2);
        }

        // 表示すべきデータの取得
        Row row = getItem(position);

        if (row.isDirectMessage()) {
            DirectMessage message = row.getMessage();
            if (message == null) {
                return view;
            }
            renderMessage(holder, message);
        } else {
            Status status = row.getStatus();
            if (status == null) {
                return view;
            }

            Status retweet = status.getRetweetedStatus();
            if (row.isFavorite()) {
                renderStatus(holder, status, null, row.getSource());
            } else if (retweet == null) {
                renderStatus(holder, status, null, null);
            } else {
                renderStatus(holder, retweet, status, null);
            }
        }

        return view;
    }

    private void renderMessage(ViewHolder holder, final DirectMessage message) {

        long userId = AccessTokenManager.getUserId();

        holder.mDoRetweet.setVisibility(View.GONE);
        holder.mDoFav.setVisibility(View.GONE);
        holder.mRetweetCount.setVisibility(View.GONE);
        holder.mFavCount.setVisibility(View.GONE);
        holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);

        if (message.getSender().getId() == userId) {
            holder.mDoReply.setVisibility(View.GONE);
        } else {
            holder.mDoReply.setVisibility(View.VISIBLE);
            holder.mDoReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionUtil.doReplyDirectMessage(message, mContext);
                }
            });
        }

        holder.mDisplayName.setText(message.getSender().getName());
        holder.mScreenName.setText("@"
                + message.getSender().getScreenName());
        holder.mStatus.setText("D " + message.getRecipientScreenName()
                + " " + message.getText());
        holder.mDatetime
                .setText(TimeUtil.getAbsoluteTime(message.getCreatedAt()));
        holder.mDatetimeRelative.setText(TimeUtil.getRelativeTime(message.getCreatedAt()));
        holder.mVia.setVisibility(View.GONE);
        holder.mRetweetContainer.setVisibility(View.GONE);
        holder.mImagesContainer.setVisibility(View.GONE);
        UserIconManager.displayUserIcon(message.getSender(), holder.mIcon);
        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", message.getSender().getScreenName());
                mContext.startActivity(intent);
            }
        });
        holder.mActionContainer.setVisibility(View.GONE);
        holder.mLock.setVisibility(View.INVISIBLE);
    }

    private void renderStatus(final ViewHolder holder, final Status status, Status retweet,
                              User favorite) {

        long userId = AccessTokenManager.getUserId();

        if (status.getFavoriteCount() > 0) {
            holder.mFavCount.setText(String.valueOf(status.getFavoriteCount()));
            holder.mFavCount.setVisibility(View.VISIBLE);
        } else {
            holder.mFavCount.setText("0");
            holder.mFavCount.setVisibility(View.INVISIBLE);
        }

        if (status.getRetweetCount() > 0) {
            holder.mRetweetCount.setText(String.valueOf(status.getRetweetCount()));
            holder.mRetweetCount.setVisibility(View.VISIBLE);
        } else {
            holder.mRetweetCount.setText("0");
            holder.mRetweetCount.setVisibility(View.INVISIBLE);
        }

        holder.mDoReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionUtil.doReplyAll(status, mContext);
            }
        });

        holder.mDoRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.getUser().isProtected()) {
                    MessageUtil.showToast(R.string.toast_protected_tweet_can_not_share);
                    return;
                }
                Long id = FavRetweetManager.getRtId(status);
                if (id != null) {
                    if (id == 0) {
                        MessageUtil.showToast(R.string.toast_destroy_retweet_progress);
                    } else {
                        DialogFragment dialog = new DestroyRetweetDialogFragment();
                        Bundle args = new Bundle(1);
                        args.putSerializable("status", status);
                        dialog.setArguments(args);
                        EventBus.getDefault().post(new AlertDialogEvent(dialog));
                    }
                } else {
                    DialogFragment dialog = new RetweetDialogFragment();
                    Bundle args = new Bundle(1);
                    args.putSerializable("status", status);
                    dialog.setArguments(args);
                    EventBus.getDefault().post(new AlertDialogEvent(dialog));
                }
            }
        });

        holder.mDoFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mDoFav.getTag().equals("is_fav")) {
                    holder.mDoFav.setTag("no_fav");
                    holder.mDoFav.setTextColor(Color.parseColor("#666666"));
                    ActionUtil.doDestroyFavorite(status.getId());
                } else {
                    holder.mDoFav.setTag("is_fav");
                    holder.mDoFav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    ActionUtil.doFavorite(status.getId());
                }
            }
        });

        if (FavRetweetManager.getRtId(status) != null) {
            holder.mDoRetweet.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
        } else {
            holder.mDoRetweet.setTextColor(Color.parseColor("#666666"));
        }

        if (FavRetweetManager.isFav(status)) {
            holder.mDoFav.setTag("is_fav");
            holder.mDoFav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
        } else {
            holder.mDoFav.setTag("no_fav");
            holder.mDoFav.setTextColor(Color.parseColor("#666666"));
        }

        holder.mDisplayName.setText(status.getUser().getName());
        holder.mScreenName.setText("@" + status.getUser().getScreenName());
        holder.mDatetimeRelative.setText(TimeUtil.getRelativeTime(status.getCreatedAt()));
        holder.mDatetime.setText(TimeUtil.getAbsoluteTime(status.getCreatedAt()));

        String via = StatusUtil.getClientName(status.getSource());
        holder.mVia.setText("via " + via);
        holder.mVia.setVisibility(View.VISIBLE);

        /**
         * デバッグモードの時だけ Justaway for Android をハイライト
         */
        if (BuildConfig.DEBUG) {
            if (via.equals("Justaway for Android")) {
                if (mColorBlue == 0) {
                    mColorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue);
                }
                holder.mVia.setTextColor(mColorBlue);
            } else {
                holder.mVia.setTextColor(Color.parseColor("#666666"));
            }
        }

        // favの場合
        if (favorite != null) {
            holder.mActionIcon.setText(R.string.fontello_star);
            holder.mActionIcon.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
            holder.mActionByDisplayName.setText(favorite.getName());
            holder.mActionByScreenName.setText("@" + favorite.getScreenName());
            holder.mRetweetContainer.setVisibility(View.GONE);
            holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);
            holder.mActionContainer.setVisibility(View.VISIBLE);
        }

        // RTの場合
        else if (retweet != null) {

            // 自分のツイート
            if (userId == status.getUser().getId()) {
                holder.mActionIcon.setText(R.string.fontello_retweet);
                holder.mActionIcon.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
                holder.mActionByDisplayName.setText(retweet.getUser().getName());
                holder.mActionByScreenName.setText("@" + retweet.getUser().getScreenName());
                holder.mRetweetContainer.setVisibility(View.GONE);
                holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);
                holder.mActionContainer.setVisibility(View.VISIBLE);
            } else {
                if (BasicSettings.getUserIconSize().equals("none")) {
                    holder.mRetweetIcon.setVisibility(View.GONE);
                } else {
                    holder.mRetweetIcon.setVisibility(View.VISIBLE);
                    ImageUtil.displayRoundedImage(retweet.getUser().getProfileImageURL(), holder.mRetweetIcon);
                }
                holder.mRetweetBy.setText("RT by " + retweet.getUser().getName() + " @" + retweet.getUser().getScreenName());
                holder.mActionContainer.setVisibility(View.GONE);
                holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);
                holder.mRetweetContainer.setVisibility(View.VISIBLE);
            }
        } else {

            // 自分へのリプ
            if (StatusUtil.isMentionForMe(status)) {
                holder.mActionIcon.setText(R.string.fontello_at);
                holder.mActionIcon.setTextColor(mContext.getResources().getColor(R.color.holo_red_light));
                holder.mActionByDisplayName.setText(status.getUser().getName());
                holder.mActionByScreenName.setText("@" + status.getUser().getScreenName());
                holder.mActionContainer.setVisibility(View.VISIBLE);
                holder.mRetweetContainer.setVisibility(View.GONE);
            } else {
                holder.mActionContainer.setVisibility(View.GONE);
                holder.mRetweetContainer.setVisibility(View.GONE);
            }
            holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);
        }

        if (status.getUser().isProtected()) {
            holder.mLock.setVisibility(View.VISIBLE);
        } else {
            holder.mLock.setVisibility(View.INVISIBLE);
        }
        UserIconManager.displayUserIcon(status.getUser(), holder.mIcon);
        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", status.getUser().getScreenName());
                mContext.startActivity(intent);
            }
        });

        // RTの場合はRT元
        String statusString = StatusUtil.getExpandedText(status);
        holder.mStatus.setText(StatusUtil.generateUnderline(statusString));

        // プレビュー表示On
        if (BasicSettings.getDisplayThumbnailOn()) {
            ImageUtil.displayThumbnailImages(mContext, holder.mImagesContainer, status);
        } else {
            holder.mImagesContainer.setVisibility(View.GONE);
        }
    }

    public static final class RetweetDialogFragment extends DialogFragment {
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Status status = (Status) getArguments().getSerializable("status");
            if (status == null) {
                return null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_retweet);
            builder.setMessage(status.getText());
            builder.setNeutralButton(getString(R.string.button_quote),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActionUtil.doQuote(status, getActivity());
                            dismiss();
                        }
                    }
            );
            builder.setPositiveButton(getString(R.string.button_retweet),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActionUtil.doRetweet(status.getId());
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }

    public static final class DestroyRetweetDialogFragment extends DialogFragment {
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Status status = (Status) getArguments().getSerializable("status");
            if (status == null) {
                return null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_destroy_retweet);
            builder.setMessage(status.getText());
            builder.setPositiveButton(getString(R.string.button_destroy_retweet),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActionUtil.doDestroyRetweet(status);
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }
}
