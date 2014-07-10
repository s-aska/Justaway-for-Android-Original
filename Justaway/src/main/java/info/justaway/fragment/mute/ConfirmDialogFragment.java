package info.justaway.fragment.mute;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import info.justaway.R;

public final class ConfirmDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        public void onPositiveClick(String source);
    }

    public static ConfirmDialogFragment newInstance(Fragment fragment) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        dialog.setTargetFragment(fragment, 0);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String source = getArguments().getString("source");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getString(R.string.confirm_destroy_mute), source));
        builder.setPositiveButton(
                R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                        listener.onPositiveClick(source);
                    }
                }
        );
        builder.setNegativeButton(
                R.string.button_no,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );
        return builder.create();
    }
}
