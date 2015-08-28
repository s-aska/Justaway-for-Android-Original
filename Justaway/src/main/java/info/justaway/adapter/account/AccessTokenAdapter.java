package info.justaway.adapter.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import info.justaway.R;
import info.justaway.listener.OnTrashListener;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.UserIconManager;
import info.justaway.util.ThemeUtil;
import info.justaway.widget.FontelloButton;
import twitter4j.auth.AccessToken;

public class AccessTokenAdapter extends ArrayAdapter<AccessToken> {

    private LayoutInflater mInflater;
    private int mLayout;
    private OnTrashListener mOnTrashListener;
    private int mColorBlue;

    class ViewHolder {
        @Bind(R.id.icon) ImageView mIcon;
        @Bind(R.id.screen_name) TextView mScreenName;
        @Bind(R.id.trash) FontelloButton mTrash;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setOnTrashListener(OnTrashListener onTrashListener) {
        mOnTrashListener = onTrashListener;
    }

    public AccessTokenAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = textViewResourceId;
        mColorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue);
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

        final AccessToken accessToken = getItem(position);

        UserIconManager.displayUserIcon(accessToken.getUserId(), viewHolder.mIcon);

        viewHolder.mScreenName.setText(accessToken.getScreenName());

        if (AccessTokenManager.getUserId() == accessToken.getUserId()) {
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
