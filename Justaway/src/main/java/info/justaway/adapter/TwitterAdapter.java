package info.justaway.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import info.justaway.BuildConfig;
import info.justaway.JustawayApplication;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.ScaleImageActivity;
import info.justaway.event.AlertDialogEvent;
import info.justaway.event.action.SeenTopEvent;
import info.justaway.model.Row;
import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

public class TwitterAdapter extends ArrayAdapter<Row> {

    static class ViewHolder {

        @InjectView(R.id.action_container)
        LinearLayout mActionContainer;
        @InjectView(R.id.action_icon)
        TextView mActionIcon;
        @InjectView(R.id.action_by_display_name)
        TextView mActionByDisplayName;
        @InjectView(R.id.action_by_screen_name)
        TextView mActionByScreenName;
        @InjectView(R.id.icon)
        ImageView mIcon;
        @InjectView(R.id.display_name)
        TextView mDisplayName;
        @InjectView(R.id.screen_name)
        TextView mScreenName;
        @InjectView(R.id.lock)
        TextView mLock;
        @InjectView(R.id.datetime_relative)
        TextView mDatetimeRelative;
        @InjectView(R.id.status)
        TextView mStatus;
        @InjectView(R.id.images_container)
        LinearLayout mImagesContainer;
        @InjectView(R.id.menu_and_via_container)
        TableLayout mMenuAndViaContainer;
        @InjectView(R.id.do_reply)
        TextView mDoReply;
        @InjectView(R.id.do_retweet)
        TextView mDoRetweet;
        @InjectView(R.id.retweet_count)
        TextView mRetweetCount;
        @InjectView(R.id.do_fav)
        TextView mDoFav;
        @InjectView(R.id.fav_count)
        TextView mFavCount;
        @InjectView(R.id.via)
        TextView mVia;
        @InjectView(R.id.datetime)
        TextView mDatetime;
        @InjectView(R.id.retweet_container)
        LinearLayout mRetweetContainer;
        @InjectView(R.id.retweet_icon)
        ImageView mRetweetIcon;
        @InjectView(R.id.retweet_by)
        TextView mRetweetBy;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private JustawayApplication mApplication;
    private Context mContext;
    private ArrayList<Row> mStatuses = new ArrayList<Row>();
    private LayoutInflater mInflater;
    private int mLayout;
    private int mColorBlue = 0;
    private static final int LIMIT = 100;
    private int mLimit = LIMIT;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM'/'dd' 'HH':'mm':'ss",
            Locale.ENGLISH);
    private static final Pattern TWITPIC_PATTERN = Pattern.compile("^http://twitpic\\.com/(\\w+)$");
    private static final Pattern TWIPPLE_PATTERN = Pattern.compile("^http://p\\.twipple\\.jp/(\\w+)$");
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^http://instagram\\.com/p/([^/]+)/$");
    private static final Pattern IMAGES_PATTERN = Pattern.compile("^https?://.*\\.(png|gif|jpeg|jpg)$");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("^https?://(?:www\\.youtube\\.com/watch\\?.*v=|youtu\\.be/)([\\w-]+)");
    private static final Pattern NICONICO_PATTERN = Pattern.compile("^http://(?:www\\.nicovideo\\.jp/watch|nico\\.ms)/sm(\\d+)$");

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayout = textViewResourceId;
        this.mApplication = JustawayApplication.getApplication();
    }

    public void extensionAdd(Row row) {
        if (JustawayApplication.isMute(row)) {
            return;
        }
        super.add(row);
        if (exists(row)) {
            return;
        }
        mStatuses.add(row);
        filter(row);
        mLimit++;
    }

    @Override
    public void add(Row row) {
        if (JustawayApplication.isMute(row)) {
            return;
        }
        if (exists(row)) {
            return;
        }
        super.add(row);
        mStatuses.add(row);
        filter(row);
        limitation();
    }

    @Override
    public void insert(Row row, int index) {
        if (JustawayApplication.isMute(row)) {
            return;
        }
        if (exists(row)) {
            return;
        }
        super.insert(row, index);
        mStatuses.add(index, row);
        filter(row);
        limitation();
    }

    public boolean exists(Row row) {
        // 先頭の3つくらい見れば十分
        int max = 3;
        if (row.isStatus()) {
            for (Row status : mStatuses) {
                if (status.isStatus() && status.getStatus().getId() == row.getStatus().getId()) {
                    return true;
                }
                max--;
                if (max < 1) {
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public void remove(Row row) {
        super.remove(row);
        mStatuses.remove(row);
    }

    private void filter(Row row) {
        Status status = row.getStatus();
        if (status != null && status.isRetweeted()) {
            Status retweet = status.getRetweetedStatus();
            if (retweet != null && status.getUser().getId() == mApplication.getUserId()) {
                mApplication.setRtId(retweet.getId(), status.getId());
            }
        }
    }

    @SuppressWarnings("unused")
    public void replaceStatus(Status status) {
        for (Row row : mStatuses) {
            if (!row.isDirectMessage() && row.getStatus().getId() == status.getId()) {
                row.setStatus(status);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public int removeStatus(long statusId) {
        int position = 0;
        for (Row row : mStatuses) {
            if (!row.isDirectMessage() && row.getStatus().getId() == statusId) {
                remove(row);
                return position;
            }
            position++;
        }
        return -1;
    }

    public void removeDirectMessage(long directMessageId) {
        for (Row row : mStatuses) {
            if (row.isDirectMessage() && row.getMessage().getId() == directMessageId) {
                remove(row);
                break;
            }
        }
    }

    public void limitation() {
        int size = mStatuses.size();
        if (size > mLimit) {
            int count = size - mLimit;
            for (int i = 0; i < count; i++) {
                super.remove(mStatuses.remove(size - i - 1));
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        mStatuses.clear();
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

        if (mApplication.getFontSize() != (Integer) holder.mStatus.getTag()) {
            holder.mStatus.setTag(mApplication.getFontSize());
            holder.mStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, mApplication.getFontSize());
            holder.mDisplayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mApplication.getFontSize());
            holder.mScreenName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mApplication.getFontSize() - 2);
            holder.mDatetimeRelative.setTextSize(TypedValue.COMPLEX_UNIT_SP, mApplication.getFontSize() - 2);
        }

        // 表示すべきデータの取得
        Row row = mStatuses.get(position);

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

        if (position == 0) {
            EventBus.getDefault().post(new SeenTopEvent());
        }

        return view;
    }

    private void renderMessage(ViewHolder holder, final DirectMessage message) {

        Typeface fontello = JustawayApplication.getFontello();
        long userId = JustawayApplication.getApplication().getUserId();

        holder.mDoRetweet.setVisibility(View.GONE);
        holder.mDoFav.setVisibility(View.GONE);
        holder.mRetweetCount.setVisibility(View.GONE);
        holder.mFavCount.setVisibility(View.GONE);
        holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);

        if (message.getSender().getId() == userId) {
            holder.mDoReply.setVisibility(View.GONE);
        } else {
            holder.mDoReply.setVisibility(View.VISIBLE);
            holder.mDoReply.setTypeface(fontello);
            holder.mDoReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mApplication.doReplyDirectMessage(message, mContext);
                }
            });
        }

        holder.mDisplayName.setText(message.getSender().getName());
        holder.mScreenName.setText("@"
                + message.getSender().getScreenName());
        holder.mStatus.setText("D " + message.getRecipientScreenName()
                + " " + message.getText());
        holder.mDatetime
                .setText(getAbsoluteTime(message.getCreatedAt()));
        holder.mDatetimeRelative.setText(getRelativeTime(message.getCreatedAt()));
        holder.mVia.setVisibility(View.GONE);
        holder.mRetweetContainer.setVisibility(View.GONE);
        holder.mImagesContainer.setVisibility(View.GONE);
        mApplication.displayUserIcon(message.getSender(), holder.mIcon);
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

        long userId = JustawayApplication.getApplication().getUserId();

        Typeface fontello = JustawayApplication.getFontello();

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

        holder.mDoReply.setTypeface(fontello);
        holder.mDoReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApplication.doReplyAll(status, mContext);
            }
        });

        holder.mDoRetweet.setTypeface(fontello);
        holder.mDoRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.getUser().isProtected()) {
                    JustawayApplication.showToast(R.string.toast_protected_tweet_can_not_share);
                    return;
                }
                Long id = mApplication.getRtId(status);
                if (id != null) {
                    if (id == 0) {
                        JustawayApplication.showToast(R.string.toast_destroy_retweet_progress);
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

        holder.mDoFav.setTypeface(fontello);
        holder.mDoFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mDoFav.getTag().equals("is_fav")) {
                    holder.mDoFav.setTag("no_fav");
                    holder.mDoFav.setTextColor(Color.parseColor("#666666"));
                    mApplication.doDestroyFavorite(status.getId());
                } else {
                    holder.mDoFav.setTag("is_fav");
                    holder.mDoFav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                    mApplication.doFavorite(status.getId());
                }
            }
        });

        if (mApplication.getRtId(status) != null) {
            holder.mDoRetweet.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
        } else {
            holder.mDoRetweet.setTextColor(Color.parseColor("#666666"));
        }

        if (mApplication.isFav(status)) {
            holder.mDoFav.setTag("is_fav");
            holder.mDoFav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
        } else {
            holder.mDoFav.setTag("no_fav");
            holder.mDoFav.setTextColor(Color.parseColor("#666666"));
        }

        holder.mDisplayName.setText(status.getUser().getName());
        holder.mScreenName.setText("@" + status.getUser().getScreenName());
        holder.mDatetimeRelative.setText(getRelativeTime(status.getCreatedAt()));
        holder.mDatetime.setText(getAbsoluteTime(status.getCreatedAt()));

        String via = mApplication.getClientName(status.getSource());
        holder.mVia.setText("via " + via);
        holder.mVia.setVisibility(View.VISIBLE);

        /**
         * デバッグモードの時だけ Justaway for Android をハイライト
         */
        if (BuildConfig.DEBUG) {
            if (via.equals("Justaway for Android")) {
                if (mColorBlue == 0) {
                    mColorBlue = mApplication.getThemeTextColor((Activity) mContext, R.attr.holo_blue);
                }
                holder.mVia.setTextColor(mColorBlue);
            } else {
                holder.mVia.setTextColor(Color.parseColor("#666666"));
            }
        }

        holder.mActionIcon.setTypeface(fontello);

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
                if (mApplication.getUserIconSize().equals("none")) {
                    holder.mRetweetIcon.setVisibility(View.GONE);
                } else {
                    holder.mRetweetIcon.setVisibility(View.VISIBLE);
                    mApplication.displayRoundedImage(retweet.getUser().getProfileImageURL(), holder.mRetweetIcon);
                }
                holder.mRetweetBy.setText("RT by " + retweet.getUser().getName() + " @" + retweet.getUser().getScreenName());
                holder.mActionContainer.setVisibility(View.GONE);
                holder.mMenuAndViaContainer.setVisibility(View.VISIBLE);
                holder.mRetweetContainer.setVisibility(View.VISIBLE);
            }
        } else {

            // 自分へのリプ
            if (mApplication.isMentionForMe(status)) {
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
            holder.mLock.setTypeface(fontello);
            holder.mLock.setVisibility(View.VISIBLE);
        } else {
            holder.mLock.setVisibility(View.INVISIBLE);
        }
        mApplication.displayUserIcon(status.getUser(), holder.mIcon);
        holder.mIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", status.getUser().getScreenName());
                mContext.startActivity(intent);
            }
        });

        boolean displayThumbnail = mApplication.getDisplayThumbnailOn();
        URLEntity[] urls = retweet != null ? retweet.getURLEntities() : status.getURLEntities();
        ArrayList<String> imageUrls = new ArrayList<String>();
        String statusString = status.getText();
        for (URLEntity url : urls) {
            Pattern p = Pattern.compile(url.getURL());
            Matcher m = p.matcher(statusString);
            statusString = m.replaceAll(url.getExpandedURL());
            if (!displayThumbnail) {
                continue;
            }
            Matcher twitpic_matcher = TWITPIC_PATTERN.matcher(url.getExpandedURL());
            if (twitpic_matcher.find()) {
                imageUrls.add("http://twitpic.com/show/full/" + twitpic_matcher.group(1));
                continue;
            }
            Matcher twipple_matcher = TWIPPLE_PATTERN.matcher(url.getExpandedURL());
            if (twipple_matcher.find()) {
                imageUrls.add("http://p.twpl.jp/show/orig/" + twipple_matcher.group(1));
                continue;
            }
            Matcher instagram_matcher = INSTAGRAM_PATTERN.matcher(url.getExpandedURL());
            if (instagram_matcher.find()) {
                imageUrls.add(url.getExpandedURL() + "media?size=l");
                continue;
            }
            Matcher youtube_matcher = YOUTUBE_PATTERN.matcher(url.getExpandedURL());
            if (youtube_matcher.find()) {
                imageUrls.add("http://i.ytimg.com/vi/" + youtube_matcher.group(1) + "/hqdefault.jpg");
                continue;
            }
            Matcher niconico_matcher = NICONICO_PATTERN.matcher(url.getExpandedURL());
            if (niconico_matcher.find()) {
                int id = Integer.valueOf(niconico_matcher.group(1));
                int host = id % 4 + 1;
                imageUrls.add("http://tn-skr" + host + ".smilevideo.jp/smile?i=" + id + ".L");
                continue;
            }
            Matcher images_matcher = IMAGES_PATTERN.matcher(url.getExpandedURL());
            if (images_matcher.find()) {
                imageUrls.add(url.getExpandedURL());
            }
        }
        holder.mStatus.setText(statusString);

        if (!displayThumbnail) {
            holder.mImagesContainer.setVisibility(View.GONE);
            return;
        }
        MediaEntity[] medias = retweet != null ? retweet.getMediaEntities() : status.getMediaEntities();
        for (MediaEntity media : medias) {
            imageUrls.add(media.getMediaURL());
        }
        holder.mImagesContainer.removeAllViews();
        if (imageUrls.size() > 0) {
            for (final String url : imageUrls) {
                ImageView image = new ImageView(mContext);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.mImagesContainer.addView(image, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 120));
                mApplication.displayRoundedImage(url, image);
                // 画像タップで拡大表示（ピンチイン・ピンチアウトいつかちゃんとやる）
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ScaleImageActivity.class);
                        intent.putExtra("url", url);
                        mContext.startActivity(intent);
                    }
                });
            }
            holder.mImagesContainer.setVisibility(View.VISIBLE);
        } else {
            holder.mImagesContainer.setVisibility(View.GONE);
        }
    }


    private String getRelativeTime(Date date) {
        int diff = (int) (((new Date()).getTime() - date.getTime()) / 1000);
        if (diff < 1) {
            return "now";
        } else if (diff < 60) {
            return diff + "s";
        } else if (diff < 3600) {
            return (diff / 60) + "m";
        } else if (diff < 86400) {
            return (diff / 3600) + "h";
        } else {
            return (diff / 86400) + "d";
        }
    }

    private String getAbsoluteTime(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static final class RetweetDialogFragment extends DialogFragment {
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
                            JustawayApplication.getApplication().doQuote(status, getActivity());
                            dismiss();
                        }
                    }
            );
            builder.setPositiveButton(getString(R.string.button_retweet),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JustawayApplication.getApplication().doRetweet(status.getId());
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
                            JustawayApplication.getApplication().doDestroyRetweet(status);
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
