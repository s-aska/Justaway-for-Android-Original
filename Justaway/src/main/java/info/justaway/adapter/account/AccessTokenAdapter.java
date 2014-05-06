package info.justaway.adapter.account;

import android.app.Activity;
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
import info.justaway.listener.OnTrashListener;
import info.justaway.widget.FontelloButton;
import twitter4j.auth.AccessToken;

public class AccessTokenAdapter extends ArrayAdapter<AccessToken> {

    private ArrayList<AccessToken> mAccountLists = new ArrayList<AccessToken>();
    private LayoutInflater mInflater;
    private int mLayout;
    private OnTrashListener mOnTrashListener;
    private JustawayApplication mApplication;
    private int mColorBlue;

    class ViewHolder {
        @InjectView(R.id.icon) ImageView mIcon;
        @InjectView(R.id.screen_name) TextView mScreenName;
        @InjectView(R.id.trash) FontelloButton mTrash;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public void setOnTrashListener(OnTrashListener onTrashListener) {
        mOnTrashListener = onTrashListener;
    }

    public AccessTokenAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = textViewResourceId;
        mApplication = JustawayApplication.getApplication();
        mColorBlue = mApplication.getThemeTextColor((Activity) context, R.attr.holo_blue);
    }

    @Override
    public void add(AccessToken account) {
        super.add(account);
        mAccountLists.add(account);
    }

    @Override
    public void remove(AccessToken accessToken) {
        super.remove(accessToken);
        mAccountLists.remove(accessToken);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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

        final AccessToken accessToken = mAccountLists.get(position);

        mApplication.displayUserIcon(accessToken.getUserId(), viewHolder.mIcon);

        viewHolder.mScreenName.setText(accessToken.getScreenName());
        viewHolder.mTrash.setTypeface(JustawayApplication.getFontello());

        if (mApplication.getUserId() == accessToken.getUserId()) {
            viewHolder.mScreenName.setTextColor(mColorBlue);
            viewHolder.mTrash.setVisibility(View.GONE);
        }

        viewHolder.mTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnTrashListener != null) {
                    mOnTrashListener.onTrash(position);
                }
            }
        });

        return view;
    }
}
