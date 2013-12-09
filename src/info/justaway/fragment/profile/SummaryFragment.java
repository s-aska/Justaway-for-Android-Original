package info.justaway.fragment.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import info.justaway.EditProfileActivity;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.ScaleImageActivity;
import info.justaway.task.FollowTask;
import info.justaway.task.UnfollowTask;
import twitter4j.Relationship;
import twitter4j.User;

/**
 * Created by aska on 2013/12/04.
 */
public class SummaryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_summary, container, false);

        JustawayApplication application = JustawayApplication.getApplication();

        final User user = (User) getArguments().getSerializable("user");
        Relationship relationship = (Relationship) getArguments().getSerializable("relationship");

        ImageView icon = (ImageView) v.findViewById(R.id.icon);
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView screenName = (TextView) v.findViewById(R.id.screenName);
        TextView followedBy = (TextView) v.findViewById(R.id.followedBy);
        TextView follow = (TextView) v.findViewById(R.id.follow);

        final String iconUrl = user.getBiggerProfileImageURL();
        application.displayRoundedImage(iconUrl, icon);

        // アイコンタップで拡大
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ScaleImageActivity.class);
                intent.putExtra("url", iconUrl);
                startActivity(intent);
            }
        });

        name.setText(user.getName());
        screenName.setText("@" + user.getScreenName());

        if (relationship.isSourceFollowedByTarget()) {
            followedBy.setText("フォローされています");
        } else {
            followedBy.setText("");
        }

        User me = application.getUser();
        if (me == null) {
            follow.setVisibility(View.GONE);
            return v;
        }

        follow.setVisibility(View.VISIBLE);
        if (user.getId() == me.getId()) {
            follow.setText("プロフィールを編集する");
            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(intent);
                }
            });
        } else if (relationship.isSourceFollowingTarget()) {
            follow.setText("フォローを解除");
            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new UnfollowTask().execute(user.getId());
                }
            });
        } else {
            follow.setText("フォローする");
            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FollowTask().execute(user.getId());
                }
            });
        }

        return v;
    }
}
