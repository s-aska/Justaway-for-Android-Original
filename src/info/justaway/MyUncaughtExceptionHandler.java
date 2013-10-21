package info.justaway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static File BUG_REPORT_FILE = null;
    static {
        String sdcard = Environment.getExternalStorageDirectory().getPath();
        String path = sdcard + File.separator + "bug.txt";
        BUG_REPORT_FILE = new File(path);
    }

    private static PackageInfo sPackInfo;
    private UncaughtExceptionHandler mDefaultUEH;

    public MyUncaughtExceptionHandler(Context context) {
        try {
            // パッケージ情報
            sPackInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread th, Throwable t) {
        try {
            saveState(t);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mDefaultUEH.uncaughtException(th, t);
    }

    private void saveState(Throwable e) throws FileNotFoundException {
        StackTraceElement[] stacks = e.getStackTrace();
        File file = BUG_REPORT_FILE;
        PrintWriter pw = null;
        pw = new PrintWriter(new FileOutputStream(file));
        StringBuilder sb = new StringBuilder();
        int len = stacks.length;
        for (int i = 0; i < len; i++) {
            StackTraceElement stack = stacks[i];
            sb.setLength(0);
            sb.append(stack.getClassName()).append("#");
            sb.append(stack.getMethodName()).append(":");
            sb.append(stack.getLineNumber());
            pw.println(sb.toString());
        }
        pw.close();
    }

    public static final void showBugReportDialogIfExist(Context context) {
        File file = BUG_REPORT_FILE;
        if (file != null & file.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("バグレポート");
            builder.setMessage("バグ発生状況を開発者に送信しますか？");
            builder.setNegativeButton("キャンセル", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish(dialog);
                }
            });
            builder.setPositiveButton("送信", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    postBugReportInBackground();// バグ報告
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private static void postBugReportInBackground() {
        new Thread(new Runnable() {
            public void run() {
                postBugReport();
                File file = BUG_REPORT_FILE;
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
        }).start();
    }

    private static void postBugReport() {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        String bug = getFileBody(BUG_REPORT_FILE);
        nvps.add(new BasicNameValuePair("dev", Build.DEVICE));
        nvps.add(new BasicNameValuePair("mod", Build.MODEL));
        nvps.add(new BasicNameValuePair("sdk", String.valueOf(Build.VERSION.SDK_INT)));
        nvps.add(new BasicNameValuePair("ver", sPackInfo.versionName));
        nvps.add(new BasicNameValuePair("bug", bug));
        try {
            HttpPost httpPost = new HttpPost("http://justaway.info/bug");
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileBody(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static void finish(DialogInterface dialog) {
        File file = BUG_REPORT_FILE;
        if (file.exists()) {
            file.delete();
        }
        dialog.dismiss();
    }
}
