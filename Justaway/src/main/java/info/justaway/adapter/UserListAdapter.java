package info.justaway.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.UserListActivity;
import info.justaway.util.ImageUtil;
import twitter4j.UserList;

public class UserListAdapter extends ArrayAdapter<UserList> {

    static class ViewHolder {
        TextView list_name;
        TextView screen_name;
        TextView description;
        TextView member_count;
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout;

    public UserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mLayout = textViewResourceId;
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
            holder = new ViewHolder();
            holder.list_name = ((TextView) view.findViewById(R.id.list_name));
            holder.screen_name = ((TextView) view.findViewById(R.id.screen_name));
            holder.description = ((TextView) view.findViewById(R.id.description));
            holder.member_count = ((TextView) view.findViewById(R.id.member_count));
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final UserList userList = getItem(position);

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        String iconUrl = userList.getUser().getBiggerProfileImageURL();
        ImageUtil.displayRoundedImage(iconUrl, icon);

        holder.list_name.setText(userList.getName());
        holder.screen_name.setText(userList.getUser().getScreenName()
                .concat(mContext.getString(R.string.label_created_by)));
        holder.description.setText(userList.getDescription());
        holder.member_count.setText(String.valueOf(userList.getMemberCount())
                .concat(mContext.getString(R.string.label_members)));

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", userList.getUser().getScreenName());
                mContext.startActivity(intent);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserListActivity.class);
                intent.putExtra("userList", userList);
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
