package info.justaway;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

import twitter4j.User;

import static android.app.AlertDialog.Builder;

public class UpdateProfileImageFragment extends DialogFragment {

    private File imgPath;
    private Uri uri;

    public UpdateProfileImageFragment(File imgPath, Uri uri) {
        this.imgPath = imgPath;
        this.uri = uri;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());

        builder.setTitle("プロフィール画像を変更しますか？");

        // LinearLayoutを被せないとサイズを調整できない
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);
        ImageView image = new ImageView(getActivity());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setLayoutParams(new LinearLayout.LayoutParams(
                340,
                340));
        JustawayApplication.getApplication().displayImage(uri.toString(), image);
        layout.addView(image);
        builder.setView(layout);

        builder.setPositiveButton("適用",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new UpdateProfileImageTask().execute();
                        dismiss();
                    }
                });
        builder.setNegativeButton("キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    private class UpdateProfileImageTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... params) {
            try {
                User user = JustawayApplication.getApplication().getTwitter().updateProfileImage(imgPath);
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            // dismissProgressDialog();
            JustawayApplication application = JustawayApplication.getApplication();
            if (user != null) {
                application.showToast("プロフィール画像が公開されました");
            } else {
                application.showToast("失敗しました");
            }
        }
    }
}