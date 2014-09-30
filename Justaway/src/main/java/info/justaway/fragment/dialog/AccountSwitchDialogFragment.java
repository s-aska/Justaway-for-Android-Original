package info.justaway.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import info.justaway.R;
import info.justaway.listener.RemoveAccountListener;
import twitter4j.auth.AccessToken;

public final class AccountSwitchDialogFragment extends DialogFragment {

    public static AccountSwitchDialogFragment newInstance(AccessToken accessToken) {
        final Bundle args = new Bundle(1);
        args.putSerializable("accessToken", accessToken);

        final AccountSwitchDialogFragment f = new AccountSwitchDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AccessToken accessToken = (AccessToken) getArguments().getSerializable("accessToken");

        assert accessToken != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getString(R.string.confirm_remove_account), accessToken.getScreenName()));
        builder.setPositiveButton(
                R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RemoveAccountListener listener = (RemoveAccountListener) getActivity();
                        listener.removeAccount(accessToken);
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