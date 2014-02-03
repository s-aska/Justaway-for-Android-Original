package info.justaway.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import info.justaway.BuildConfig;
import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.PostActivity;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.ScaleImageActivity;
import info.justaway.model.Row;
import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class TwitterAdapter extends ArrayAdapter<Row> {

    static class ViewHolder {
        LinearLayout action;
        TextView action_icon;
        TextView action_by_display_name;
        TextView action_by_screen_name;

        ImageView icon;

        TextView display_name;
        TextView screen_name;
        TextView fontello_lock;
        TextView datetime_relative;

        TextView status;

        LinearLayout images;

        TableLayout menu_and_via;

        TextView do_reply;
        TextView do_retweet;
        TextView retweet_count;
        TextView do_fav;
        TextView fav_count;

        TextView via;
        TextView datetime;

        LinearLayout retweet;
        ImageView retweet_icon;
        TextView retweet_by;
    }

    private JustawayApplication mApplication;
    private Context mContext;
    private ArrayList<Row> mStatuses = new ArrayList<Row>();
    private LayoutInflater mInflater;
    private int mLayout;
    private Boolean isMain;
    private static final int LIMIT = 100;
    private int mLimit = LIMIT;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM'/'dd' 'HH':'mm':'ss",
            Locale.ENGLISH);

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayout = textViewResourceId;
        this.mApplication = (JustawayApplication) context.getApplicationContext();
        this.isMain = mContext.getClass().getName().equals("info.justaway.MainActivity");
    }

    public void extensionAdd(Row row) {
        super.add(row);
        this.filter(row);
        this.mStatuses.add(row);
        mLimit++;
    }

    @Override
    public void add(Row row) {
        super.add(row);
        this.filter(row);
        this.mStatuses.add(row);
        this.limitation();
    }

    @Override
    public void insert(Row row, int index) {
        super.insert(row, index);
        this.filter(row);
        this.mStatuses.add(index, row);
        this.limitation();
    }

    @Override
    public void remove(Row row) {
        super.remove(row);
        this.mStatuses.remove(row);
    }

    private void filter(Row row) {
        Status status = row.getStatus();
        if (status != null && status.isRetweeted()) {
            Status retweet = status.getRetweetedStatus();
            long userId = mApplication.getUserId();
            if (retweet != null && status.getUser().getId() == userId) {
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

    public void removeStatus(long statusId) {
        for (Row row : mStatuses) {
            if (!row.isDirectMessage() && row.getStatus().getId() == statusId) {
                remove(row);
                break;
            }
        }
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
        int size = this.mStatuses.size();
        if (size > mLimit) {
            int count = size - mLimit;
            for (int i = 0; i < count; i++) {
                super.remove(this.mStatuses.remove(size - i - 1));
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.mStatuses.clear();
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

            holder = new ViewHolder();
            holder.action = (LinearLayout) view.findViewById(R.id.action);
            holder.action_icon = (TextView) view.findViewById(R.id.action_icon);
            holder.action_by_display_name = (TextView) view.findViewById(R.id.action_by_display_name);
            holder.action_by_screen_name = (TextView) view.findViewById(R.id.action_by_screen_name);
            holder.icon = (ImageView) view.findViewById(R.id.icon);
            holder.display_name = (TextView) view.findViewById(R.id.display_name);
            holder.screen_name = (TextView) view.findViewById(R.id.screen_name);
            holder.fontello_lock = (TextView) view.findViewById(R.id.fontello_lock);
            holder.datetime_relative = (TextView) view.findViewById(R.id.datetime_relative);
            holder.status = (TextView) view.findViewById(R.id.status);
            holder.images = (LinearLayout) view.findViewById(R.id.images);
            holder.menu_and_via = (TableLayout) view.findViewById(R.id.menu_and_via);
            holder.do_reply = (TextView) view.findViewById(R.id.do_reply);
            holder.do_retweet = (TextView) view.findViewById(R.id.do_retweet);
            holder.retweet_count = (TextView) view.findViewById(R.id.retweet_count);
            holder.do_fav = (TextView) view.findViewById(R.id.do_fav);
            holder.fav_count = (TextView) view.findViewById(R.id.fav_count);
            holder.via = (TextView) view.findViewById(R.id.via);
            holder.datetime = (TextView) view.findViewById(R.id.datetime);
            holder.retweet = (LinearLayout) view.findViewById(R.id.retweet);
            holder.retweet_icon = (ImageView) view.findViewById(R.id.retweet_icon);
            holder.retweet_by = (TextView) view.findViewById(R.id.retweet_by);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
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

        if (isMain && position == 0) {
            ((MainActivity) mContext).showTopView();
        }

        return view;
    }

    private void renderMessage(ViewHolder holder, final DirectMessage message) {

        Typeface fontello = JustawayApplication.getFontello();
        long userId = JustawayApplication.getApplication().getUserId();

        holder.do_retweet.setVisibility(View.GONE);
        holder.do_fav.setVisibility(View.GONE);
        holder.retweet_count.setVisibility(View.GONE);
        holder.fav_count.setVisibility(View.GONE);
        holder.menu_and_via.setVisibility(View.VISIBLE);

        if (message.getSender().getId() == userId) {
            holder.do_reply.setVisibility(View.GONE);
        } else {
            holder.do_reply.setVisibility(View.VISIBLE);
            holder.do_reply.setTypeface(fontello);
            holder.do_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = "D " + message.getSender().getScreenName() + " ";
                    if (mContext.getClass().getName().equals("info.justaway.MainActivity")) {
                        MainActivity activity = (MainActivity) mContext;
                        View singleLineTweet = activity.findViewById(R.id.quick_tweet_layout);
                        if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
                            EditText editStatus = (EditText) activity.findViewById(R.id.quick_tweet_edit);
                            editStatus.setText(text);
                            editStatus.setSelection(text.length());
                            editStatus.requestFocus();
                            mApplication.showKeyboard(editStatus);
                            activity.setInReplyToStatusId((long) 0);
                            return;
                        }
                    }
                    Intent intent = new Intent(mContext, PostActivity.class);
                    intent.putExtra("status", text);
                    intent.putExtra("selection", text.length());
                    mContext.startActivity(intent);
                }
            });
        }

        holder.display_name.setText(message.getSender().getName());
        holder.screen_name.setText("@"
                + message.getSender().getScreenName());
        holder.status.setText("D " + message.getRecipientScreenName()
                + " " + message.getText());
        holder.datetime
                .setText(getAbsoluteTime(message.getCreatedAt()));
        holder.datetime_relative.setText(getRelativeTime(message.getCreatedAt()));
        holder.via.setVisibility(View.GONE);
        holder.retweet.setVisibility(View.GONE);
        holder.images.setVisibility(View.GONE);
        mApplication.displayRoundedImage(message.getSender().getBiggerProfileImageURL(), holder.icon);
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", message.getSender().getScreenName());
                mContext.startActivity(intent);
            }
        });
        holder.action.setVisibility(View.GONE);
        holder.fontello_lock.setVisibility(View.INVISIBLE);
    }

    private void renderStatus(final ViewHolder holder, final Status status, Status retweet,
                              User favorite) {

        long userId = JustawayApplication.getApplication().getUserId();

        Typeface fontello = JustawayApplication.getFontello();

        if (status.getFavoriteCount() > 0) {
            holder.fav_count.setText(String.valueOf(status.getFavoriteCount()));
            holder.fav_count.setVisibility(View.VISIBLE);
        } else {
            holder.fav_count.setText("0");
            holder.fav_count.setVisibility(View.INVISIBLE);
        }

        if (status.getRetweetCount() > 0) {
            holder.retweet_count.setText(String.valueOf(status.getRetweetCount()));
            holder.retweet_count.setVisibility(View.VISIBLE);
        } else {
            holder.retweet_count.setText("0");
            holder.retweet_count.setVisibility(View.INVISIBLE);
        }

        holder.do_reply.setTypeface(fontello);
        holder.do_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserMentionEntity[] mentions = status.getUserMentionEntities();
                Intent intent = new Intent(mContext, PostActivity.class);
                String text;
                if (status.getUser().getId() == mApplication.getUserId() && mentions.length == 1) {
                    text = "@" + mentions[0].getScreenName() + " ";
                } else {
                    text = "@" + status.getUser().getScreenName() + " ";
                }

                if (mContext.getClass().getName().equals("info.justaway.MainActivity")) {
                    MainActivity activity = (MainActivity) mContext;
                    View singleLineTweet = activity.findViewById(R.id.quick_tweet_layout);
                    if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
                        EditText editStatus = (EditText) activity.findViewById(R.id.quick_tweet_edit);
                        editStatus.setText(text);
                        editStatus.setSelection(text.length());
                        editStatus.requestFocus();
                        mApplication.showKeyboard(editStatus);
                        activity.setInReplyToStatusId(status.getId());
                        return;
                    }
                }
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                mContext.startActivity(intent);
            }
        });

        holder.do_retweet.setTypeface(fontello);
        holder.do_retweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.getUser().isProtected()) {
                    JustawayApplication.showToast(R.string.toast_protected_tweet_can_not_share);
                    return;
                }
                Long id = mApplication.getRtId(status);
                if (id != null) {
                    DialogFragment dialog = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.confirm_destroy_retweet);
                            builder.setMessage(status.getText());
                            builder.setPositiveButton(getString(R.string.button_destroy_retweet),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mApplication.doDestroyRetweet(status.getId());
                                            holder.do_retweet.setTextColor(Color.parseColor("#666666"));
                                            dismiss();
                                        }
                                    });
                            builder.setNegativeButton(getString(R.string.button_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dismiss();
                                        }
                                    });

                            return builder.create();
                        }
                    };
                    FragmentActivity activity = (FragmentActivity) mContext;
                    dialog.show(activity.getSupportFragmentManager(), "dialog");
                } else {
                    DialogFragment dialog = new DialogFragment() {

                        /**
                         * ダイアログ閉じたあとの処理を定義できるようにしておく
                         */
                        private Runnable mOnDismiss;

                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(R.string.confirm_retweet);
                            builder.setMessage(status.getText());
                            builder.setNeutralButton(getString(R.string.button_quote),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FragmentActivity activity = (FragmentActivity) mContext;
                                            EditText editStatus = null;
                                            View singleLineTweet = activity.findViewById(R.id.quick_tweet_layout);
                                            if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
                                                editStatus = (EditText) activity.findViewById(R.id.quick_tweet_edit);
                                            }
                                            String text = " https://twitter.com/"
                                                    + status.getUser().getScreenName()
                                                    + "/status/" + String.valueOf(status.getId());
                                            if (editStatus != null) {
                                                editStatus.requestFocus();
                                                editStatus.setText(text);
                                                /**
                                                 * ダイアログ閉じた後じゃないとキーボードを出せない
                                                 */
                                                final View view = editStatus;
                                                mOnDismiss = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mApplication.showKeyboard(view);
                                                    }
                                                };
                                                return;
                                            }
                                            Intent intent = new Intent(activity, PostActivity.class);
                                            intent.putExtra("status", text);
                                            intent.putExtra("inReplyToStatusId", status.getId());
                                            startActivity(intent);
                                            dismiss();
                                        }
                                    });
                            builder.setPositiveButton(getString(R.string.button_retweet),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mApplication.doRetweet(status.getId());
                                            holder.do_retweet.setTextColor(mContext.getResources()
                                                    .getColor(R.color.holo_green_light));
                                            dismiss();
                                        }
                                    });
                            builder.setNegativeButton(getString(R.string.button_cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dismiss();
                                        }
                                    });

                            return builder.create();
                        }

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            super.onDismiss(dialog);
                            if (mOnDismiss != null) {
                                mOnDismiss.run();
                            }
                        }
                    };
                    FragmentActivity activity = (FragmentActivity) mContext;
                    dialog.show(activity.getSupportFragmentManager(), "dialog");
                }
            }
        });

        holder.do_fav.setTypeface(fontello);
        holder.do_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.do_fav.getTag().equals("is_fav")) {
                    mApplication.doDestroyFavorite(status.getId());
                    holder.do_fav.setTag("no_fav");
                    holder.do_fav.setTextColor(Color.parseColor("#666666"));
                } else {
                    mApplication.doFavorite(status.getId());
                    holder.do_fav.setTag("is_fav");
                    holder.do_fav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
                }
            }
        });

        if (mApplication.getRtId(status) != null) {
            holder.do_retweet.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
        } else {
            holder.do_retweet.setTextColor(Color.parseColor("#666666"));
        }

        if (mApplication.isFav(status)) {
            holder.do_fav.setTag("is_fav");
            holder.do_fav.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
        } else {
            holder.do_fav.setTag("no_fav");
            holder.do_fav.setTextColor(Color.parseColor("#666666"));
        }

        holder.display_name.setText(status.getUser().getName());
        holder.screen_name.setText("@" + status.getUser().getScreenName());
        holder.datetime_relative.setText(getRelativeTime(status.getCreatedAt()));
        holder.datetime.setText(getAbsoluteTime(status.getCreatedAt()));

        String via = getClientName(status.getSource());
        holder.via.setText("via " + via);
        holder.via.setVisibility(View.VISIBLE);

        /**
         * デバッグモードの時だけ Justaway for Android をハイライト
         */
        if (BuildConfig.DEBUG) {
            if (via.equals("Justaway for Android")) {
                holder.via.setTextColor(mContext.getResources().getColor(R.color.holo_blue_light));
            } else {
                holder.via.setTextColor(Color.parseColor("#666666"));
            }
        }

        holder.action_icon.setTypeface(fontello);

        // favの場合
        if (favorite != null) {
            holder.action_icon.setText(R.string.fontello_star);
            holder.action_icon.setTextColor(mContext.getResources().getColor(R.color.holo_orange_light));
            holder.action_by_display_name.setText(favorite.getName());
            holder.action_by_screen_name.setText("@" + favorite.getScreenName());
            holder.retweet.setVisibility(View.GONE);
            holder.menu_and_via.setVisibility(View.VISIBLE);
            holder.action.setVisibility(View.VISIBLE);
        }
        // RTの場合
        else if (retweet != null) {
            // 自分のツイート
            if (userId == status.getUser().getId()) {
                holder.action_icon.setText(R.string.fontello_retweet);
                holder.action_icon.setTextColor(mContext.getResources().getColor(R.color.holo_green_light));
                holder.action_by_display_name.setText(retweet.getUser().getName());
                holder.action_by_screen_name.setText("@" + retweet.getUser().getScreenName());
                holder.retweet.setVisibility(View.GONE);
                holder.menu_and_via.setVisibility(View.VISIBLE);
                holder.action.setVisibility(View.VISIBLE);
            } else {
                mApplication.displayRoundedImage(retweet.getUser().getProfileImageURL(), holder.retweet_icon);
                holder.retweet_by.setText("RT by " + retweet.getUser().getName() + " @" + retweet.getUser().getScreenName());
                holder.action.setVisibility(View.GONE);
                holder.menu_and_via.setVisibility(View.VISIBLE);
                holder.retweet.setVisibility(View.VISIBLE);
            }
        } else {
            // 自分へのリプ
            if (userId == status.getInReplyToUserId()) {
                holder.action_icon.setText(R.string.fontello_at);
                holder.action_icon.setTextColor(mContext.getResources().getColor(R.color.holo_red_light));
                holder.action_by_display_name.setText(status.getUser().getName());
                holder.action_by_screen_name.setText("@" + status.getUser().getScreenName());
                holder.action.setVisibility(View.VISIBLE);
                holder.retweet.setVisibility(View.GONE);
            } else {
                holder.action.setVisibility(View.GONE);
                holder.retweet.setVisibility(View.GONE);
            }
            holder.menu_and_via.setVisibility(View.VISIBLE);
        }

        if (status.getUser().isProtected()) {
            holder.fontello_lock.setTypeface(fontello);
            holder.fontello_lock.setVisibility(View.VISIBLE);
        } else {
            holder.fontello_lock.setVisibility(View.INVISIBLE);
        }
        mApplication.displayRoundedImage(status.getUser().getBiggerProfileImageURL(), holder.icon);
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", status.getUser().getScreenName());
                mContext.startActivity(intent);
            }
        });

        MediaEntity[] medias = retweet != null ? retweet.getMediaEntities() : status
                .getMediaEntities();
        URLEntity[] urls = retweet != null ? retweet.getURLEntities() : status.getURLEntities();
        ArrayList<String> imageUrls = new ArrayList<String>();
        Pattern twitpic_pattern = Pattern.compile("^http://twitpic\\.com/(\\w+)$");
        Pattern twipple_pattern = Pattern.compile("^http://p\\.twipple\\.jp/(\\w+)$");
        Pattern instagram_pattern = Pattern.compile("^http://instagram\\.com/p/([^/]+)/$");
        Pattern images_pattern = Pattern.compile("^https?://.*\\.(png|gif|jpeg|jpg)$");
        Pattern youtube_pattern = Pattern.compile("^https?://(?:www\\.youtube\\.com/watch\\?.*v=|youtu\\.be/)([\\w-]+)");
        Pattern niconico_pattern = Pattern.compile("^http://(?:www\\.nicovideo\\.jp/watch|nico\\.ms)/[a-z][a-z](\\d+)$");
        String statusString = status.getText();
        for (URLEntity url : urls) {
            Pattern p = Pattern.compile(url.getURL());
            Matcher m = p.matcher(statusString);
            statusString = m.replaceAll(url.getExpandedURL());

            Matcher twitpic_matcher = twitpic_pattern.matcher(url.getExpandedURL());
            if (twitpic_matcher.find()) {
                imageUrls.add("http://twitpic.com/show/full/" + twitpic_matcher.group(1));
                continue;
            }
            Matcher twipple_matcher = twipple_pattern.matcher(url.getExpandedURL());
            if (twipple_matcher.find()) {
                imageUrls.add("http://p.twpl.jp/show/orig/" + twipple_matcher.group(1));
                continue;
            }
            Matcher instagram_matcher = instagram_pattern.matcher(url.getExpandedURL());
            if (instagram_matcher.find()) {
                imageUrls.add(url.getExpandedURL() + "media?size=l");
                continue;
            }
            Matcher youtube_matcher = youtube_pattern.matcher(url.getExpandedURL());
            if (youtube_matcher.find()) {
                imageUrls.add("http://i.ytimg.com/vi/" + youtube_matcher.group(1) + "/hqdefault.jpg");
                continue;
            }
            Matcher niconico_matcher = niconico_pattern.matcher(url.getExpandedURL());
            if (niconico_matcher.find()) {
                int id = Integer.valueOf(niconico_matcher.group(1));
                int host = id % 4 + 1;
                imageUrls.add("http://tn-skr" + host + ".smilevideo.jp/smile?i=" + id + ".L");
                continue;
            }
            Matcher images_matcher = images_pattern.matcher(url.getExpandedURL());
            if (images_matcher.find()) {
                imageUrls.add(url.getExpandedURL());
            }
        }
        holder.status.setText(statusString);

        for (MediaEntity media : medias) {
            imageUrls.add(media.getMediaURL());
        }
        holder.images.removeAllViews();
        if (imageUrls.size() > 0) {
            for (final String url : imageUrls) {
                ImageView image = new ImageView(mContext);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.images.addView(image, new LinearLayout.LayoutParams(
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
            holder.images.setVisibility(View.VISIBLE);
        } else {
            holder.images.setVisibility(View.GONE);
        }
    }

    private String getClientName(String source) {
        String[] tokens = source.split("[<>]");
        if (tokens.length > 1) {
            return tokens[2];
        } else {
            return tokens[0];
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
}
