package info.justaway.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.model.UserListWithRegistered;
import twitter4j.UserList;

public class SubscribeUserListAdapter extends ArrayAdapter<UserListWithRegistered> {

    private ArrayList<UserListWithRegistered> mUserLists = new ArrayList<UserListWithRegistered>();
    private Context mContext;
    private LayoutInflater mInflater;
    private JustawayApplication mApplication;
    private int mLayout;

    public SubscribeUserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mLayout = textViewResourceId;
        this.mApplication = JustawayApplication.getApplication();
    }

    @Override
    public void add(UserListWithRegistered userListWithRegistered) {
        super.add(userListWithRegistered);
        mUserLists.add(userListWithRegistered);
    }

    @Override
    public void remove(UserListWithRegistered userListWithRegistered) {
        super.remove(userListWithRegistered);
        mUserLists.remove(userListWithRegistered);
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

        final UserListWithRegistered userListWithRegistered = mUserLists.get(position);
        final UserList userList = userListWithRegistered.getUserList();

        TextView trash = (TextView) view.findViewById(R.id.trash);
        trash.setTypeface(JustawayApplication.getFontello());
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 自分のリストの場合はリスト削除
                if (mApplication.getUserId() == userList.getUser().getId()) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.confirm_destroy_user_list)
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AsyncTask<Void, Void, Boolean>() {
                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    try {
                                                        mApplication.getTwitter().destroyUserList(userList.getId());
                                                        return true;
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        return false;
                                                    }
                                                }

                                                @Override
                                                protected void onPostExecute(Boolean success) {
                                                    if (success) {
                                                        JustawayApplication.showToast(R.string.toast_destroy_user_list_success);
                                                        remove(userListWithRegistered);
                                                        mApplication.getUserLists().remove(userList);
                                                    } else {
                                                        JustawayApplication.showToast(R.string.toast_destroy_user_list_failure);
                                                    }
                                                }
                                            }.execute();
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }

                // 他人のリストの場合はリストの購読解除
                else {
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.confirm_destroy_user_list_subscribe)
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AsyncTask<Void, Void, Boolean>() {
                                                @Override
                                                protected Boolean doInBackground(Void... params) {
                                                    try {
                                                        mApplication.getTwitter().destroyUserListSubscription(userList.getId());
                                                        return true;
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        return false;
                                                    }
                                                }

                                                @Override
                                                protected void onPostExecute(Boolean success) {
                                                    if (success) {
                                                        JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_success);
                                                        remove(userListWithRegistered);
                                                        mApplication.getUserLists().remove(userList);
                                                    } else {
                                                        JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_failure);
                                                    }
                                                }
                                            }.execute();
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }

            }
        });

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        if (checkbox != null) {
            if (mApplication.getUserId() == userList.getUser().getId()) {
                checkbox.setText(userList.getName());
            } else {
                checkbox.setText(userList.getFullName());
            }
            checkbox.setChecked(userListWithRegistered.isRegistered());
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    userListWithRegistered.setRegistered(b);
                }
            });
        }

        return view;
    }
}
