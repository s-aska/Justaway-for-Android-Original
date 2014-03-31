package info.justaway.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.AlertDialogEvent;
import info.justaway.model.UserListWithRegistered;
import info.justaway.task.DestroyUserListSubscriptionTask;
import info.justaway.task.DestroyUserListTask;
import twitter4j.UserList;

public class SubscribeUserListAdapter extends ArrayAdapter<UserListWithRegistered> {

    private ArrayList<UserListWithRegistered> mUserLists = new ArrayList<UserListWithRegistered>();
    private LayoutInflater mInflater;
    private JustawayApplication mApplication;
    private int mLayout;

    public SubscribeUserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
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

    public UserListWithRegistered findByUserListId(Long userListId) {
        for (UserListWithRegistered userListWithRegistered : mUserLists) {
            if (userListWithRegistered.getUserList().getId() == userListId) {
                return userListWithRegistered;
            }
        }
        return null;
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

                if (mApplication.getUserId() == userList.getUser().getId()) {
                    // 自分のリストの場合はリスト削除
                    DialogFragment dialog = new DestroyUserListDialogFragment();
                    Bundle args = new Bundle(1);
                    args.putSerializable("userList", userList);
                    dialog.setArguments(args);
                    EventBus.getDefault().post(new AlertDialogEvent(dialog));
                } else {
                    // 他人のリストの場合はリストの購読解除
                    DialogFragment dialog = new DestroyUserListSubscriptionDialogFragment();
                    Bundle args = new Bundle(1);
                    args.putSerializable("userList", userList);
                    dialog.setArguments(args);
                    EventBus.getDefault().post(new AlertDialogEvent(dialog));
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
            checkbox.setOnCheckedChangeListener(null);
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

    public static final class DestroyUserListDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final UserList userList = (UserList) getArguments().getSerializable("userList");
            if (userList == null) {
                return null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_destroy_user_list);
            builder.setMessage(userList.getName());
            builder.setPositiveButton(getString(R.string.button_yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DestroyUserListTask(userList).execute();
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }

    public static final class DestroyUserListSubscriptionDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final UserList userList = (UserList) getArguments().getSerializable("userList");
            if (userList == null) {
                return null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_destroy_user_list_subscribe);
            builder.setMessage(userList.getName());
            builder.setPositiveButton(getString(R.string.button_yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DestroyUserListSubscriptionTask(userList).execute();
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
        }
    }
}
