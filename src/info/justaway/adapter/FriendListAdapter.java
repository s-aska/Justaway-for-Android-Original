package info.justaway.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.JustawayApplication;
import info.justaway.ProfileActivity;
import info.justaway.R;
import twitter4j.URLEntity;
import twitter4j.User;

public class FriendListAdapter extends ArrayAdapter<User> {
    private JustawayApplication mApplication;
    private ArrayList<User> users = new ArrayList<User>();
    private Context context;
    private LayoutInflater inflater;
    private int layout;

    public FriendListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layout = textViewResourceId;
        this.mApplication = (JustawayApplication) context.getApplicationContext();
    }

    @Override
    public void add(User user) {
        super.add(user);
        users.add(user);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = inflater.inflate(this.layout, null);
        }

        final User user = users.get(position);

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        String iconUrl = user.getBiggerProfileImageURL();
        mApplication.displayRoundedImage(iconUrl, icon);

        ((TextView) view.findViewById(R.id.display_name)).setText(user.getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText("@" + user.getScreenName());

        TextView description = (TextView) view.findViewById(R.id.description);

        if (user.getDescription() != null && user.getDescription().length() > 0) {
            String descriptionString = user.getDescription();
            if (user.getDescriptionURLEntities() != null) {
                URLEntity[] urls = user.getDescriptionURLEntities();
                for (URLEntity descriptionUrl : urls) {
                    Pattern p = Pattern.compile(descriptionUrl.getURL());
                    Matcher m = p.matcher(descriptionString);
                    descriptionString = m.replaceAll(descriptionUrl.getExpandedURL());
                }
            }
            description.setText(descriptionString);
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }

        Typeface fontello = Typeface.createFromAsset(context.getAssets(), "fontello.ttf");

        if (user.isProtected()) {
            ((TextView) view.findViewById(R.id.fontello_lock)).setTypeface(fontello);
            view.findViewById(R.id.fontello_lock).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.fontello_lock).setVisibility(View.INVISIBLE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", user.getScreenName());
                context.startActivity(intent);
            }
        });

        return view;
    }
}
