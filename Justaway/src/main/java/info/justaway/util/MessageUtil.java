package info.justaway.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import info.justaway.JustawayApplication;

public class MessageUtil {
    private static ProgressDialog sProgressDialog;

    public static void showToast(String text) {
        JustawayApplication application = JustawayApplication.getApplication();
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int id) {
        JustawayApplication application = JustawayApplication.getApplication();
        String text = application.getString(id);
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int id, String description) {
        JustawayApplication application = JustawayApplication.getApplication();
        String text = application.getString(id) + "\n" + description;
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show();
    }

    public static void showProgressDialog(Context context, String message) {
        sProgressDialog = new ProgressDialog(context);
        sProgressDialog.setMessage(message);
        sProgressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (sProgressDialog != null)
            try {
                sProgressDialog.dismiss();
            } finally {
                sProgressDialog = null;
            }
    }
}
