package info.justaway;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ImageView;

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

        builder.setTitle("プロフィール画像を変更する");
        builder.setMessage("");

        ImageView image = new ImageView(getActivity());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        JustawayApplication.getApplication().displayRoundedImage(uri.toString(), image);
        builder.setView(image);

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
                application.setUser(user);
            } else {
                application.showToast("失敗しました");
            }
        }
    }
}