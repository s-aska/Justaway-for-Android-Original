package info.justaway.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.util.ImageUtil;
import info.justaway.widget.FontelloTextView;
import twitter4j.URLEntity;
import twitter4j.User;

public class UserAdapter extends ArrayAdapter<User> {

    static class ViewHolder {
        @Bind(R.id.icon) ImageView mIcon;
        @Bind(R.id.display_name) TextView mDisplayName;
        @Bind(R.id.screen_name) TextView mScreenName;
        @Bind(R.id.lock) FontelloTextView mFontelloLock;
        @Bind(R.id.description) TextView mDescription;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout;

    public UserAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mLayout = textViewResourceId;
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
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final User user = getItem(position);

        String iconUrl = user.getBiggerProfileImageURL();
        ImageUtil.displayRoundedImage(iconUrl, holder.mIcon);

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

        if (user.isProtected()) {
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
