package info.justaway.fragment.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.R;
import twitter4j.URLEntity;
import twitter4j.User;

public class DescriptionFragment extends Fragment {

    private static SimpleDateFormat mSimpleDateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_description, container, false);
        if (v == null) {
            return null;
        }

        User user = (User) getArguments().getSerializable("user");
        if (user == null) {
            return null;
        }

        TextView description = (TextView) v.findViewById(R.id.description);
        TextView location = (TextView) v.findViewById(R.id.location);
        TextView url = (TextView) v.findViewById(R.id.url);
        TextView start = (TextView) v.findViewById(R.id.start);

        /**
         * プロフィール
         */
        if (user.getDescription() != null && user.getDescription().length() > 0) {
            String descriptionString = user.getDescription();
            /**
             * 短縮URLの展開
             */
            if (user.getDescriptionURLEntities() != null) {
                URLEntity[] urls = user.getDescriptionURLEntities();
                for (URLEntity descriptionUrl : urls) {
                    Pattern p = Pattern.compile(descriptionUrl.getURL());
                    Matcher m = p.matcher(descriptionString);
                    descriptionString = m.replaceAll(descriptionUrl.getExpandedURL());
                }
            }
            description.setText(descriptionString);
            description.setVisibility(View.VISIBLE);
        } else {
            description.setVisibility(View.GONE);
        }

        /**
         * 現在地
         */
        if (user.getLocation() != null && user.getLocation().length() > 0) {
            location.setText(user.getLocation());
            location.setVisibility(View.VISIBLE);
        } else {
            location.setVisibility(View.GONE);
        }

        /**
         * WebSite
         */
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

        /**
         * Twitter開始日
         */
        if (mSimpleDateFormat == null) {
            mSimpleDateFormat = new SimpleDateFormat(getString(R.string.format_user_created_at), Locale.ENGLISH);
        }

        start.setText(mSimpleDateFormat.format(user.getCreatedAt()));

        return v;
    }
}
