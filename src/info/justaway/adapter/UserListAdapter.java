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

/**
 * Created by teshi on 2013/12/21.
 */
public class UserListAdapter extends ArrayAdapter<UserList> {

    private JustawayApplication mApplication;
    private ArrayList<UserList> userLists = new ArrayList<UserList>();
    private Context context;
    private LayoutInflater inflater;
    private int layout;


    public UserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layout = textViewResourceId;
        this.mApplication = (JustawayApplication) context.getApplicationContext();
    }

    @Override
    public void add(UserList userList) {
        super.add(userList);
        userLists.add(userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = inflater.inflate(this.layout, null);
        }

        final UserList userList = userLists.get(position);

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
                context.startActivity(intent);
            }
        });

        return view;
    }
}
