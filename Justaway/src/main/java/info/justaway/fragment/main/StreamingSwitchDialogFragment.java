package info.justaway.fragment.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.model.TwitterManager;
import info.justaway.settings.BasicSettings;
import info.justaway.util.MessageUtil;

public class StreamingSwitchDialogFragment extends DialogFragment {

    public static StreamingSwitchDialogFragment newInstance(boolean turnOn) {
        final Bundle args = new Bundle(1);
        args.putBoolean("turnOn", turnOn);

        final StreamingSwitchDialogFragment f = new StreamingSwitchDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final boolean turnOn = getArguments().getBoolean("turnOn");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(turnOn ? R.string.confirm_create_streaming : R.string.confirm_destroy_streaming);
        builder.setPositiveButton(getString(R.string.button_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BasicSettings.setStreamingMode(turnOn);
                        if (turnOn) {
                            TwitterManager.startStreaming();
                            MessageUtil.showToast(R.string.toast_create_streaming);
                        } else {
                            TwitterManager.stopStreaming();
                            MessageUtil.showToast(R.string.toast_destroy_streaming);
                        }
                        dismiss();
                    }
                }
        );
        builder.setNegativeButton(getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }
        );
        return builder.create();
    }
}
