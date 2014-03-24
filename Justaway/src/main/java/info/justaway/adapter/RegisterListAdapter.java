package info.justaway.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

import info.justaway.R;
import info.justaway.model.UserListWithRegistered;

public class RegisterListAdapter extends ArrayAdapter<UserListWithRegistered> {

    private ArrayList<UserListWithRegistered> mUserListWithRegisteredList = new ArrayList<UserListWithRegistered>();
    private LayoutInflater mInflater;
    private int mLayout;

    public RegisterListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mLayout = textViewResourceId;
    }

    @Override
    public void add(UserListWithRegistered userListWithRegistered) {
        super.add(userListWithRegistered);
        mUserListWithRegisteredList.add(userListWithRegistered);
    }

    @Override
    public void remove(UserListWithRegistered userList) {
        super.remove(userList);
        mUserListWithRegisteredList.remove(userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
            if (view == null) {
                return null;
            }
        }

        UserListWithRegistered userListWithRegistered = mUserListWithRegisteredList.get(position);
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        if (checkbox != null) {
            checkbox.setText(userListWithRegistered.getUserList().getName());
            checkbox.setChecked(userListWithRegistered.isRegistered());
            checkbox.setTag(userListWithRegistered.getUserList().getId());
        }

        return view;
    }

}
