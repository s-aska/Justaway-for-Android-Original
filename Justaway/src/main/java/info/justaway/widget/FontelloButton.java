package info.justaway.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import info.justaway.JustawayApplication;

public class FontelloButton extends Button {

    public FontelloButton(Context context) {
        super(context);
        init();
    }

    public FontelloButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public FontelloButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    private void init() {
        setTypeface(JustawayApplication.getFontello());
    }
}
