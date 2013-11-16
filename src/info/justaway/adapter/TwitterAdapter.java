package info.justaway.adapter;

import info.justaway.JustawayApplication;
import info.justaway.PostActivity;
import info.justaway.ScaleImageActivity;
import info.justaway.MainActivity;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.model.Row;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TwitterAdapter extends ArrayAdapter<Row> {
    private JustawayApplication mApplication;
    private Context mContext;
    private ArrayList<Row> statuses = new ArrayList<Row>();
    private LayoutInflater mInflater;
    private int mLayout;
    private Boolean isMain;
    private static final int LIMIT = 200;

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayout = textViewResourceId;
        this.mApplication = (JustawayApplication) context.getApplicationContext();
        this.isMain = mContext.getClass().getName().equals("info.justaway.MainActivity");
    }

    @Override
    public void add(Row row) {
        super.add(row);
        this.filter(row);
        this.statuses.add(row);
        this.limitation();
    }

    @Override
    public void insert(Row row, int index) {
        super.insert(row, index);
        this.filter(row);
        this.statuses.add(index, row);
        this.limitation();
    }

    @Override
    public void remove(Row row) {
        super.remove(row);
        this.statuses.remove(row);
    }

    private void filter(Row row) {
        Status status = row.getStatus();
        if (status != null && status.isRetweeted()) {
            Status retweet = status.getRetweetedStatus();
            User user = mApplication.getUser();
            if (retweet != null && user != null && status.getUser().getId() == user.getId()) {
                Log.d("Justaway", "[filter]" + retweet.getId() + " => " + status.getId());
                mApplication.setRtId(retweet.getId(), status.getId());
            }
        }
    }

    public void replaceStatus(Status status) {
        for (Row row : statuses) {
            if (row.isDirectMessage() != true && row.getStatus().getId() == status.getId()) {
                row.setStatus(status);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void removeStatus(long statusId) {
        for (Row row : statuses) {
            if (row.isDirectMessage() != true && row.getStatus().getId() == statusId) {
                remove(row);
                break;
            }
        }
    }

    public void removeDirectMessage(long directMessageId) {
        for (Row row : statuses) {
            if (row.isDirectMessage() && row.getMessage().getId() == directMessageId) {
                remove(row);
                break;
            }
        }
    }

    public void limitation() {
        int size = this.statuses.size();
        if (size > LIMIT) {
            int count = size - LIMIT;
            for (int i = 0; i < count; i++) {
                super.remove(this.statuses.remove(size - i - 1));
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.statuses.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
        }

        // 表示すべきデータの取得
        Row row = (Row) statuses.get(position);

        if (row.isDirectMessage()) {
            DirectMessage message = row.getMessage();
            if (message == null) {
                return view;
            }
            renderMessage(view, message);
        } else {
            Status status = row.getStatus();
            if (status == null) {
                return view;
            }

            Status retweet = status.getRetweetedStatus();
            if (row.isFavorite()) {
                renderStatus(view, row, status, null, row.getSource());
            } else if (retweet == null) {
                renderStatus(view, row, status, null, null);
            } else {
                renderStatus(view, row, retweet, status, null);
            }
        }

        if (isMain && position == 0) {
            ((MainActivity) mContext).showTopView();
        }

        return view;
    }

    private void renderMessage(View view, final DirectMessage message) {

        Typeface fontello = Typeface.createFromAsset(mContext.getAssets(), "fontello.ttf");
        User user = JustawayApplication.getApplication().getUser();

        TextView do_reply = (TextView) view.findViewById(R.id.do_reply);
        view.findViewById(R.id.do_retweet).setVisibility(View.GONE);
        view.findViewById(R.id.do_fav).setVisibility(View.GONE);
        view.findViewById(R.id.retweet_count).setVisibility(View.GONE);
        view.findViewById(R.id.fav_count).setVisibility(View.GONE);
        view.findViewById(R.id.menu_and_via).setVisibility(View.VISIBLE);

        if (message.getSender().getId() == user.getId()) {
            do_reply.setVisibility(View.GONE);
        } else {
            do_reply.setVisibility(View.VISIBLE);
            do_reply.setTypeface(fontello);
            do_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostActivity.class);
                    String text = "D " + message.getSender().getScreenName() + " ";
                    intent.putExtra("status", text);
                    intent.putExtra("selection", text.length());
                    mContext.startActivity(intent);
                }
            });
        }

        ((TextView) view.findViewById(R.id.display_name)).setText(message.getSender().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + message.getSender().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText("D " + message.getRecipientScreenName()
                + " " + message.getText());
        SimpleDateFormat date_format = new SimpleDateFormat("MM'/'dd' 'hh':'mm':'ss",
                Locale.ENGLISH);
        ((TextView) view.findViewById(R.id.datetime)).setText(date_format.format(message
                .getCreatedAt()));
        view.findViewById(R.id.via).setVisibility(View.GONE);
        view.findViewById(R.id.retweet).setVisibility(View.GONE);
        view.findViewById(R.id.images).setVisibility(View.GONE);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        mApplication.displayRoundedImage(message.getSender().getBiggerProfileImageURL(), icon);
        view.findViewById(R.id.action).setVisibility(View.GONE);
        // view.findViewById(R.id.is_favorited).setVisibility(View.GONE);
    }

    private void renderStatus(View view, final Row row, final Status status, Status retweet,
                              User favorite) {

        final Status soruce = retweet != null ? retweet : status;
        User user = JustawayApplication.getApplication().getUser();

        Typeface fontello = Typeface.createFromAsset(mContext.getAssets(), "fontello.ttf");

        final TextView do_reply = (TextView) view.findViewById(R.id.do_reply);
        final TextView do_retweet = (TextView) view.findViewById(R.id.do_retweet);
        final TextView do_fav = (TextView) view.findViewById(R.id.do_fav);
        TextView retweet_count = (TextView) view.findViewById(R.id.retweet_count);
        TextView fav_count = (TextView) view.findViewById(R.id.fav_count);

        if (status.getFavoriteCount() > 0) {
            fav_count.setText(String.valueOf(status.getFavoriteCount()));
            fav_count.setVisibility(View.VISIBLE);
        } else {
            fav_count.setText("0");
            fav_count.setVisibility(View.INVISIBLE);
        }

        if (status.getRetweetCount() > 0) {
            retweet_count.setText(String.valueOf(status.getRetweetCount()));
            retweet_count.setVisibility(View.VISIBLE);
        } else {
            retweet_count.setText("0");
            retweet_count.setVisibility(View.INVISIBLE);
        }

        do_reply.setTypeface(fontello);
        do_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PostActivity.class);
                String text = "@" + soruce.getUser().getScreenName() + " ";
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                mContext.startActivity(intent);
            }
        });

        do_retweet.setTypeface(fontello);
        do_retweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = mApplication.getRtId(status);
                if (id != null) {
                    mApplication.doDestroyRetweet(status.getId());
                    do_retweet.setTextColor(Color.parseColor("#666666"));
                } else {
                    mApplication.doRetweet(status.getId());
                    do_retweet.setTextColor(mContext.getResources()
                            .getColor(color.holo_green_light));
                }
            }
        });

        do_fav.setTypeface(fontello);
        do_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApplication.isFav(status)) {
                    mApplication.doDestroyFavorite(status.getId());
                    do_retweet.setTextColor(Color.parseColor("#666666"));
                } else {
                    mApplication.doFavorite(status.getId());
                    do_fav.setTextColor(mContext.getResources().getColor(color.holo_orange_light));
                }
            }
        });

        if (mApplication.getRtId(status) != null) {
            do_retweet.setTextColor(mContext.getResources().getColor(color.holo_green_light));
        } else {
            do_retweet.setTextColor(Color.parseColor("#666666"));
        }

        if (mApplication.isFav(status)) {
            do_fav.setTextColor(mContext.getResources().getColor(color.holo_orange_light));
        } else {
            do_fav.setTextColor(Color.parseColor("#666666"));
        }

        ((TextView) view.findViewById(R.id.display_name)).setText(status.getUser().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + status.getUser().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText(status.getText());
        ((TextView) view.findViewById(R.id.datetime))
                .setText(getRelativeTime(status.getCreatedAt()));
        ((TextView) view.findViewById(R.id.via))
                .setText("via " + getClientName(status.getSource()));
        view.findViewById(R.id.via).setVisibility(View.VISIBLE);

        TextView actionIcon = (TextView) view.findViewById(R.id.action_icon);
        actionIcon.setTypeface(fontello);
        TextView actionByName = (TextView) view.findViewById(R.id.action_by_display_name);
        TextView actionByScreenName = (TextView) view.findViewById(R.id.action_by_screen_name);

        // favの場合
        if (favorite != null) {
            actionIcon.setText(R.string.fontello_star);
            actionIcon.setTextColor(mContext.getResources().getColor(color.holo_orange_light));
            actionByName.setText(favorite.getName());
            actionByScreenName.setText("@" + favorite.getScreenName());
            view.findViewById(R.id.retweet).setVisibility(View.GONE);
            view.findViewById(R.id.menu_and_via).setVisibility(View.VISIBLE);
            view.findViewById(R.id.action).setVisibility(View.VISIBLE);
        }
        // RTの場合
        else if (retweet != null) {
            // 自分のツイート
            if (user.getId() == status.getUser().getId()) {
                actionIcon.setText(R.string.fontello_retweet);
                actionIcon.setTextColor(mContext.getResources().getColor(color.holo_green_light));
                actionByName.setText(retweet.getUser().getName());
                actionByScreenName.setText("@" + retweet.getUser().getScreenName());
                view.findViewById(R.id.retweet).setVisibility(View.GONE);
                view.findViewById(R.id.menu_and_via).setVisibility(View.VISIBLE);
                view.findViewById(R.id.action).setVisibility(View.VISIBLE);
            } else {
                ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
                mApplication.displayRoundedImage(retweet.getUser().getProfileImageURL(), icon);
                TextView retweet_by = (TextView) view.findViewById(R.id.retweet_by);
                retweet_by.setText("RT by "
                        + retweet.getUser().getName() + " @" + retweet.getUser().getScreenName());
                view.findViewById(R.id.action).setVisibility(View.GONE);
                view.findViewById(R.id.menu_and_via).setVisibility(View.VISIBLE);
                view.findViewById(R.id.retweet).setVisibility(View.VISIBLE);
            }
        } else {
            // 自分へのリプ
            if (user.getId() == status.getInReplyToUserId()) {
                actionIcon.setText(R.string.fontello_at);
                actionIcon.setTextColor(mContext.getResources().getColor(color.holo_red_light));
                actionByName.setText(status.getUser().getName());
                actionByScreenName.setText("@" + status.getUser().getScreenName());
                view.findViewById(R.id.action).setVisibility(View.VISIBLE);
                view.findViewById(R.id.retweet).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.action).setVisibility(View.GONE);
                view.findViewById(R.id.retweet).setVisibility(View.GONE);
            }
            view.findViewById(R.id.menu_and_via).setVisibility(View.VISIBLE);
        }

        if (status.getUser().isProtected()) {
            ((TextView) view.findViewById(R.id.fontello_lock)).setTypeface(fontello);
            view.findViewById(R.id.fontello_lock).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.fontello_lock).setVisibility(View.GONE);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        mApplication.displayRoundedImage(status.getUser().getBiggerProfileImageURL(), icon);
        icon.setOnClickListener(new View.OnClickListener() {
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
        for (URLEntity url : urls) {
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
        }
        for (MediaEntity media : medias) {
            imageUrls.add(media.getMediaURL());
        }
        LinearLayout images = (LinearLayout) view.findViewById(R.id.images);
        images.removeAllViews();
        if (imageUrls.size() > 0) {
            for (final String url : imageUrls) {
                ImageView image = new ImageView(mContext);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                images.addView(image, new LinearLayout.LayoutParams(
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
            images.setVisibility(View.VISIBLE);
        } else {
            images.setVisibility(View.GONE);
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
}
