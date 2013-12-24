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

    private File mImgPath;
    private Uri mUri;

    public UpdateProfileImageFragment(File imgPath, Uri uri) {
        this.mImgPath = imgPath;
        this.mUri = uri;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());

        builder.setTitle(R.string.confirm_update_profile_image);

        // LinearLayoutを被せないとサイズを調整できない
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setGravity(Gravity.CENTER);
        ImageView image = new ImageView(getActivity());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setLayoutParams(new LinearLayout.LayoutParams(
                340,
                340));
        JustawayApplication.getApplication().displayImage(mUri.toString(), image);
        layout.addView(image);
        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.button_apply),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new UpdateProfileImageTask().execute();
                        dismiss();
                    }
                });
        builder.setNegativeButton(getString(R.string.button_cancel),
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
                User user = JustawayApplication.getApplication().getTwitter().updateProfileImage(mImgPath);
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            // dismissProgressDialog();
            if (user != null) {
                JustawayApplication.showToast(R.string.toast_update_profile_image_success);
            } else {
                JustawayApplication.showToast(R.string.toast_update_profile_image_failure);
            }
        }
    }
}