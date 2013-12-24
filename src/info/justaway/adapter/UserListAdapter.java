package info.justaway.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.ProfileActivity;
import info.justaway.R;
import twitter4j.UserList;

public class UserListAdapter extends ArrayAdapter<UserList> {

    private JustawayApplication mApplication;
    private ArrayList<UserList> mUserLists = new ArrayList<UserList>();
    private Context mContext;
    private LayoutInflater mInflater;
    private int mLayout;

    public UserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mLayout = textViewResourceId;
        this.mApplication = (JustawayApplication) context.getApplicationContext();
    }

    @Override
    public void add(UserList userList) {
        super.add(userList);
        mUserLists.add(userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
        }

        final UserList userList = mUserLists.get(position);

        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        String iconUrl = userList.getUser().getBiggerProfileImageURL();
        mApplication.displayRoundedImage(iconUrl, icon);

        ((TextView) view.findViewById(R.id.list_name)).setText(userList.getName());
        ((TextView) view.findViewById(R.id.screen_name)).setText(userList.getUser().getScreenName() + "さんが作成");
        ((TextView) view.findViewById(R.id.description)).setText(userList.getDescription());
        ((TextView) view.findViewById(R.id.member_count)).setText(userList.getMemberCount() + "人のメンバー");

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("screenName", userList.getUser().getScreenName());
                mContext.startActivity(intent);
            }
        });

        return view;
    }
}
