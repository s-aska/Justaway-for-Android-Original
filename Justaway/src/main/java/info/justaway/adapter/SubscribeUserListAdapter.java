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

import butterknife.ButterKnife;
import butterknife.Bind;
import de.greenrobot.event.EventBus;
import info.justaway.R;
import info.justaway.event.AlertDialogEvent;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.UserListWithRegistered;
import info.justaway.task.DestroyUserListSubscriptionTask;
import info.justaway.task.DestroyUserListTask;
import info.justaway.widget.FontelloTextView;
import twitter4j.UserList;

public class SubscribeUserListAdapter extends ArrayAdapter<UserListWithRegistered> {

    private LayoutInflater mInflater;
    private int mLayout;

    static class ViewHolder {
        @Bind(R.id.checkbox) CheckBox mCheckBox;
        @Bind(R.id.trash) FontelloTextView mTrash;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public SubscribeUserListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = textViewResourceId;
    }

    public UserListWithRegistered findByUserListId(Long userListId) {
        for (int i = 0; i < getCount(); i++) {
            UserListWithRegistered userListWithRegistered = getItem(i);
            if (userListWithRegistered.getUserList().getId() == userListId) {
                return userListWithRegistered;
            }
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
            if (view == null) {
                return null;
            }
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final UserListWithRegistered userListWithRegistered = getItem(position);
        final UserList userList = userListWithRegistered.getUserList();

        viewHolder.mTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccessTokenManager.getUserId() == userList.getUser().getId()) {
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

        if (AccessTokenManager.getUserId() == userList.getUser().getId()) {
            viewHolder.mCheckBox.setText(userList.getName());
        } else {
            viewHolder.mCheckBox.setText(userList.getFullName());
        }
        viewHolder.mCheckBox.setOnCheckedChangeListener(null);
        viewHolder.mCheckBox.setChecked(userListWithRegistered.isRegistered());
        viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userListWithRegistered.setRegistered(b);
            }
        });

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
