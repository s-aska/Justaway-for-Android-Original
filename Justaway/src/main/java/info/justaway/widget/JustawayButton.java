package info.justaway.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import info.justaway.JustawayApplication;
import info.justaway.R;

public class JustawayButton extends Button {

    public JustawayButton(Context context) {
        super(context);
        init(context);
    }

    public JustawayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JustawayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setTypeface(JustawayApplication.getFontello());
        setTextSize(22);

        // テーマによってボタンの色を変える
        TypedValue outValueBackground = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            theme.resolveAttribute(R.attr.button_stateful, outValueBackground, true);
        }
        setBackgroundResource(outValueBackground.resourceId);
    }
}
