package info.justaway.fragment.mute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.settings.MuteSettings;

public class UserFragment extends Fragment {

    private MuteSettings mMuteSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);
        if (v == null) {
            return null;
        }

        UserAdapter adapter = new UserAdapter(getActivity(), R.layout.row_word);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(adapter);

        JustawayApplication application = JustawayApplication.getApplication();
        mMuteSettings = application.getMuteSettings();
        HashMap<Long, String> userMap = mMuteSettings.getUserMap();
        for (Long userId : userMap.keySet()) {
            User user = new User();
            user.userId = userId;
            user.screenName = userMap.get(userId);
            adapter.add(user);
        }

        return v;
    }

    public static class User {
        Long userId;
        String screenName;
    }

    public class UserAdapter extends ArrayAdapter<User> {

        private ArrayList<User> mUserList = new ArrayList<User>();
        private LayoutInflater mInflater;
        private int mLayout;

        public UserAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(User user) {
            super.add(user);
            mUserList.add(user);
        }

        public void remove(User user) {
            super.remove(user);
            mUserList.remove(user);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
                assert view != null;
            }

            final User user = mUserList.get(position);

            TextView screenName = (TextView) view.findViewById(R.id.word);
            screenName.setText("@".concat(user.screenName));

            TextView trash = (TextView) view.findViewById(R.id.trash);
            trash.setTypeface(JustawayApplication.getFontello());

            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("@".concat(user.screenName).concat(getString(R.string.confirm_destroy_some)))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove(user);
                                            mMuteSettings.removeUser(user.userId);
                                            mMuteSettings.saveMuteSettings();
                                        }
                                    })
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                            .show();
                }
            });

            return view;
        }
    }
}
