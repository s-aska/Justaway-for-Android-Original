package info.justaway.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ScaleImageViewPager extends ViewPager {

    private boolean enabled = true;

    public ScaleImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (ScaleImageView.sBounds) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (ScaleImageView.sBounds) {
            try {
                enabled = super.onInterceptTouchEvent(event);
            } catch (Exception e) {
                //
            }
            return enabled;
        }
        return false;
    }
}
