package info.justaway.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import info.justaway.JustawayApplication;

public class KeyboardUtil {
    public static void showKeyboard(final View view) {
        showKeyboard(view, 200);
    }

    public static InputMethodManager getInputMethodManager() {
        return (InputMethodManager) JustawayApplication.getApplication()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void showKeyboard(final View view, int delay) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * 表示されてないEditViewを表示と同時にキーボード出したい場合
                 * フォーカスが当たってないとキーボードは出てこないのリスナーを使う
                 * 元々設定されているリスナーを引っ張りだし、キーボード出したら戻しておく（行儀良い）
                 */
                final View.OnFocusChangeListener listener = view.getOnFocusChangeListener();
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean has_focus) {
                        if (!has_focus) {
                            return;
                        }
                        getInputMethodManager().showSoftInput(v, InputMethodManager.SHOW_FORCED);
                        v.setOnFocusChangeListener(listener);
                    }
                });
                view.clearFocus();
                view.requestFocus();
            }
        }, delay);
    }

    public static void hideKeyboard(View view) {
        getInputMethodManager().hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
