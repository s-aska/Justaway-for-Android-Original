package info.justaway.adapter;

import info.justaway.JustawayApplication;

import java.util.ArrayList;

import twitter4j.UserList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

public class SubscribeUserListAdapter extends ArrayAdapter<UserList> {

    private ArrayList<UserList> userLists = new ArrayList<UserList>();
    private Context context;
    private LayoutInflater inflater;
    private int layout;

    public SubscribeUserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layout = textViewResourceId;
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

        UserList userList = (UserList) userLists.get(position);

        CheckBox checkbox = (CheckBox) view;
        checkbox.setText(userList.getName());
        checkbox.setChecked(JustawayApplication.getApplication().existsTab(userList.getId()));
        checkbox.setTag(userList.getId());

        return view;
    }

}
