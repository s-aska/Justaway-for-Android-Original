package info.justaway.adapter;

import info.justaway.JustawayApplication;
import info.justaway.ScaleImageActivity;
import info.justaway.MainActivity;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.model.Row;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TwitterAdapter extends ArrayAdapter<Row> {
    private JustawayApplication app;
    private Context context;
    private ArrayList<Row> statuses = new ArrayList<Row>();
    private LayoutInflater inflater;
    private int layout;
    private static int limit = 500;

    public TwitterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layout = textViewResourceId;
        this.app = (JustawayApplication) context.getApplicationContext();
    }

    @Override
    public void add(Row row) {
        super.add(row);
        this.statuses.add(row);
        this.limitation();
    }

    @Override
    public void insert(Row row, int index) {
        super.insert(row, index);
        this.statuses.add(index, row);
        this.limitation();
    }

    @Override
    public void remove(Row row) {
        super.remove(row);
        this.statuses.remove(row);
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
        if (size > limit) {
            int count = size - limit;
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
            view = inflater.inflate(this.layout, null);
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
                renderStatus(view, status, null, row.getSource());
            } else if (retweet == null) {
                renderStatus(view, status, null, null);
            } else {
                renderStatus(view, retweet, status, null);
            }
        }

        if (position == 0) {
            ((MainActivity) context).showTopView();
        }

        return view;
    }

    private void renderMessage(View view, DirectMessage message) {
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
        app.displayRoundedImage(message.getSender().getBiggerProfileImageURL(), icon);
        view.findViewById(R.id.action).setVisibility(View.GONE);
        view.findViewById(R.id.is_favorited).setVisibility(View.GONE);
    }

    private void renderStatus(View view, final Status status, Status retweet, User favorite) {
        ((TextView) view.findViewById(R.id.display_name)).setText(status.getUser().getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@"
                + status.getUser().getScreenName());
        ((TextView) view.findViewById(R.id.status)).setText(status.getText());
        SimpleDateFormat date_format = new SimpleDateFormat("MM'/'dd' 'hh':'mm':'ss",
                Locale.ENGLISH);
        ((TextView) view.findViewById(R.id.datetime)).setText(date_format.format(status
                .getCreatedAt()));
        ((TextView) view.findViewById(R.id.via))
                .setText("via " + getClientName(status.getSource()));
        view.findViewById(R.id.via).setVisibility(View.VISIBLE);

        TextView actionIcon = (TextView) view.findViewById(R.id.action_icon);
        actionIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        actionIcon.setText("");
        TextView actionBy = (TextView) view.findViewById(R.id.action_by);
        TextView actionName = (TextView) view.findViewById(R.id.action_name);
        User user = JustawayApplication.getApplication().getUser();

        if (status.isFavorited() && user.getId() != status.getUser().getId()) {
            TextView isFavorited = (TextView) view.findViewById(R.id.is_favorited);
            isFavorited.setTextColor(context.getResources().getColor(color.holo_orange_light));
            isFavorited.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
            isFavorited.setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.is_favorited).setVisibility(View.GONE);
        }

        // favの場合
        if (favorite != null) {
            actionIcon.setText(R.string.fontello_star);
            actionIcon.setTextColor(context.getResources().getColor(color.holo_orange_light));
            actionBy.setText(favorite.getName());
            actionName.setText("favorited");
            view.findViewById(R.id.action).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.retweet_by)).setText("favorited by "
                    + favorite.getScreenName() + "(" + favorite.getName() + ") and "
                    + String.valueOf(status.getFavoriteCount()) + " others");
            ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
            app.displayRoundedImage(favorite.getMiniProfileImageURL(), icon);
            view.findViewById(R.id.retweet).setVisibility(View.VISIBLE);
        }
        // RTの場合
        else if (retweet != null) {
            // 自分のツイート
            if (user.getId() == status.getUser().getId()) {
                actionIcon.setText(R.string.fontello_retweet);
                actionIcon.setTextColor(context.getResources().getColor(color.holo_green_light));
                actionBy.setText(retweet.getUser().getName());
                actionName.setText("retweeted");
                view.findViewById(R.id.action).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.action).setVisibility(View.GONE);
            }
            ((TextView) view.findViewById(R.id.retweet_by)).setText("retweeted by "
                    + retweet.getUser().getScreenName() + "(" + retweet.getUser().getName()
                    + ") and " + String.valueOf(status.getRetweetCount()) + " others");
            ImageView icon = (ImageView) view.findViewById(R.id.retweet_icon);
            app.displayRoundedImage(retweet.getUser().getMiniProfileImageURL(), icon);
            view.findViewById(R.id.retweet).setVisibility(View.VISIBLE);
        } else {
            // 自分へのリプ
            if (user.getId() == status.getInReplyToUserId()) {
                actionIcon.setText(R.string.fontello_at);
                actionIcon.setTextColor(context.getResources().getColor(color.holo_red_light));
                actionBy.setText(status.getUser().getName());
                actionName.setText("mentioned you");
                view.findViewById(R.id.action).setVisibility(View.VISIBLE);
                view.findViewById(R.id.retweet).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.action).setVisibility(View.GONE);
                view.findViewById(R.id.retweet).setVisibility(View.GONE);
            }
        }

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        app.displayRoundedImage(status.getUser().getBiggerProfileImageURL(), icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", status.getUser().getScreenName());
                context.startActivity(intent);
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
                ImageView image = new ImageView(context);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                images.addView(image, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, 120));
                app.displayRoundedImage(url, image);
                // 画像タップで拡大表示（ピンチイン・ピンチアウトいつかちゃんとやる）
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ScaleImageActivity.class);
                        intent.putExtra("url", url);
                        context.startActivity(intent);
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
}
