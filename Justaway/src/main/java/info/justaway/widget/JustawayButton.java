package info.justaway.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import info.justaway.JustawayApplication;
import info.justaway.R;

public class JustawayButton extends Button {

    private static final String XMLLS = "http://schemas.android.com/apk/res/android";

    public JustawayButton(Context context) {
        super(context);
        init(context, null);
    }

    public JustawayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        init(context, attrs);
    }

    public JustawayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setTypeface(JustawayApplication.getFontello());

        if (attrs == null || attrs.getAttributeValue(XMLLS, "textSize") == null) {
            setTextSize(22);
        }

        // テーマによってボタンの色を変える
        if (attrs == null || attrs.getAttributeResourceValue(XMLLS, "background", -1) == -1) {
            TypedValue outValueBackground = new TypedValue();
            Resources.Theme theme = context.getTheme();
            if (theme != null) {
                theme.resolveAttribute(R.attr.button_stateful, outValueBackground, true);
            }
            setBackgroundResource(outValueBackground.resourceId);
        }
    }
}
