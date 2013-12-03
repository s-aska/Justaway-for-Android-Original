package info.justaway.fragment.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import info.justaway.R;
import twitter4j.User;

public class DescriptionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_description, container, false);

        User user = (User) getArguments().getSerializable("user");

        TextView description = (TextView) v.findViewById(R.id.description);
        TextView location = (TextView) v.findViewById(R.id.location);
        TextView url = (TextView) v.findViewById(R.id.url);
        TextView start = (TextView) v.findViewById(R.id.start);

        if (user.getDescription() != null && user.getDescription().length() > 0) {
            description.setText(user.getDescription());
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }

        if (user.getLocation() != null && user.getLocation().length() > 0) {
            location.setText(user.getLocation());
            location.setVisibility(View.VISIBLE);
        } else {
            location.setVisibility(View.GONE);
        }

        if (user.getURL() != null && user.getURL().length() > 0) {
            if (user.getURLEntity() != null) {
                url.setText(user.getURLEntity().getExpandedURL());
            } else {
                url.setText(user.getURL());
            }
            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }

        SimpleDateFormat date_format = new SimpleDateFormat("yyyy年MM月dd日", Locale.ENGLISH);
        start.setText("Twitter開始日：" + date_format.format(user.getCreatedAt()));

        return v;
    }
}
