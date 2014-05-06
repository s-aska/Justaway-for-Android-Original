package info.justaway.adapter.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.UserIconManager;
import twitter4j.auth.AccessToken;

public class AccessTokenAdapter extends ArrayAdapter<AccessToken> {

    static class ViewHolder {
        @InjectView(R.id.icon) ImageView mIcon;
        @InjectView(R.id.screen_name) TextView mScreenName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    private ArrayList<AccessToken> mAccessTokenList = new ArrayList<AccessToken>();
    private LayoutInflater mInflater;
    private int mLayout;
    private int mHighlightColor;
    private int mDefaultColor;

    public AccessTokenAdapter(Context context, int textViewResourceId, int highlightColor, int defaultColor) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = textViewResourceId;
        mHighlightColor = highlightColor;
        mDefaultColor = defaultColor;

        ArrayList<AccessToken> accessTokens = AccessTokenManager.getAccessTokens();
        if (accessTokens != null) {
            for (AccessToken accessToken : accessTokens) {
                add(accessToken);
            }
        }
    }

    @Override
    public void add(AccessToken accessToken) {
        super.add(accessToken);
        mAccessTokenList.add(accessToken);
    }

    @Override
    public void clear() {
        super.clear();
        mAccessTokenList.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // ビューを受け取る
        View view = convertView;
        if (view == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            view = mInflater.inflate(this.mLayout, null);
            if (view == null) {
                return null;
            }
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        AccessToken accessToken = mAccessTokenList.get(position);

        UserIconManager.displayUserIcon(accessToken.getUserId(), holder.mIcon);

        holder.mScreenName.setText(accessToken.getScreenName());

        /**
         * 使用中のアカウントはハイライト表示
         */
        if (AccessTokenManager.getUserId() == accessToken.getUserId()) {
            holder.mScreenName.setTextColor(mHighlightColor);
        } else {
            holder.mScreenName.setTextColor(mDefaultColor);
        }

        return view;
    }
}
