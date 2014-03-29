package info.justaway.event;

import android.support.v4.app.DialogFragment;

public class AlertDialogEvent {
    private final DialogFragment mDialogFragment;

    public AlertDialogEvent(final DialogFragment dialogFragment) {
        mDialogFragment = dialogFragment;
    }

    public DialogFragment getDialogFragment() {
        return mDialogFragment;
    }
}
