package info.justaway.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * バックキーでフォーカスが外れるEditTextの拡張
 */
public class ClearEditText extends EditText {

    @SuppressWarnings("unused")
    public ClearEditText(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
