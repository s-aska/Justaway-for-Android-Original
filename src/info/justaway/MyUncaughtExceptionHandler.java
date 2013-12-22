package info.justaway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final String BUG_FILE = "BUG";
    private static final String MAIL_TO = "mailto:s.aska.org@gmail.com";

    private static Context sContext;
    private static PackageInfo sPackageInfo;
    private static ActivityManager.MemoryInfo sMemoryInfo = new ActivityManager.MemoryInfo();
    private UncaughtExceptionHandler mDefaultUEH;

    public MyUncaughtExceptionHandler(Context context) {
        sContext = context;
        try {
            // パッケージ情報
            sPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread th, Throwable t) {
        saveState(t);
        mDefaultUEH.uncaughtException(th, t);
    }

    private void saveState(Throwable error) {
        try {
            PrintWriter writer = new PrintWriter(sContext.openFileOutput(BUG_FILE,
                    Context.MODE_PRIVATE));
            if (sPackageInfo != null) {
                writer.printf("[BUG][%s] versionName:%s, versionCode:%d\n",
                        sPackageInfo.packageName, sPackageInfo.versionName, sPackageInfo.versionCode);
            } else {
                writer.printf("[BUG][Unknown]\n");
            }
            try {
                writer
                        .printf("Runtime Memory: total: %dKB, free: %dKB, used: %dKB\n", Runtime
                                .getRuntime().totalMemory() / 1024,
                                Runtime.getRuntime().freeMemory() / 1024, (Runtime.getRuntime()
                                .totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ((ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE))
                        .getMemoryInfo(sMemoryInfo);
                writer.printf("availMem: %dKB, lowMemory: %b\n", sMemoryInfo.availMem / 1024,
                        sMemoryInfo.lowMemory);
            } catch (Exception e) {
                e.printStackTrace();
            }
            writer.printf("DEVICE: %s\n", Build.DEVICE);
            writer.printf("MODEL: %s\n", Build.MODEL);
            writer.printf("VERSION.SDK: %s\n", Build.VERSION.SDK_INT);
            writer.println("");
            error.printStackTrace(writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void showBugReportDialogIfExist(final Activity activity) {
        File bugFile = activity.getFileStreamPath(BUG_FILE);
        if (!bugFile.exists())
            return;

        File writeFile = activity.getFileStreamPath(BUG_FILE + ".txt");
        bugFile.renameTo(writeFile);

        final StringBuilder body = new StringBuilder();
        String firstLine = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(writeFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (firstLine == null) {
                    firstLine = line;
                } else {
                    body.append(line).append("\n");
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String subject = firstLine;
        new AlertDialog.Builder(activity).setTitle("バグレポート").setMessage("バグ発生状況を開発者に送信しますか？")
                .setPositiveButton("送信", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(MAIL_TO))
                                .putExtra(Intent.EXTRA_SUBJECT, subject).putExtra(Intent.EXTRA_TEXT,
                                        body.toString()));
                    }
                }).setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }
}
