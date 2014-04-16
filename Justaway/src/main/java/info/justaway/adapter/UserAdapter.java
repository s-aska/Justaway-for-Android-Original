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

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.justaway.JustawayApplication;
import info.justaway.ProfileActivity;
import info.justaway.R;
import twitter4j.URLEntity;
import twitter4j.User;

public class UserAdapter extends ArrayAdapter<User> {

    static class ViewHolder {

        @InjectView(R.id.icon)
        ImageView mIcon;
        @InjectView(R.id.display_name)
        TextView mDisplayName;
        @InjectView(R.id.screen_name)
        TextView mScreenName;
        @InjectView(R.id.lock)
        TextView mFontelloLock;
        @InjectView(R.id.description)
        TextView mDescription;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private ArrayList<User> mUsers = new ArrayList<User>();
    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout;

    public UserAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayout = textViewResourceId;
    }

    @Override
    public void add(User user) {
        super.add(user);
        mUsers.add(user);
    }

    @Override
    public void clear() {
        super.clear();
        mUsers.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        ViewHolder holder;
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
            if (view == null) {
                return null;
            }
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final User user = mUsers.get(position);

        String iconUrl = user.getBiggerProfileImageURL();
        JustawayApplication.getApplication().displayRoundedImage(iconUrl, holder.mIcon);

        holder.mDisplayName.setText(user.getName());
        holder.mScreenName.setText("@" + user.getScreenName());

        String descriptionString = user.getDescription();
        if (descriptionString != null && descriptionString.length() > 0) {
            URLEntity[] urls = user.getDescriptionURLEntities();
            for (URLEntity descriptionUrl : urls) {
                descriptionString = descriptionString.replaceAll(descriptionUrl.getURL(),
                        descriptionUrl.getExpandedURL());
            }
            holder.mDescription.setText(descriptionString);
            holder.mDescription.setVisibility(View.VISIBLE);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }

        Typeface fontello = JustawayApplication.getFontello();
        if (user.isProtected()) {
            holder.mFontelloLock.setTypeface(fontello);
            holder.mFontelloLock.setVisibility(View.VISIBLE);
        } else {
            holder.mFontelloLock.setVisibility(View.INVISIBLE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", user.getScreenName());
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
