package info.justaway.fragment.mute;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.justaway.R;
import info.justaway.settings.MuteSettings;
import info.justaway.widget.FontelloTextView;

public class SourceFragment extends Fragment {

    private SourceAdapter mSourceAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);
        if (v == null) {
            return null;
        }

        mSourceAdapter = new SourceAdapter(getActivity(), R.layout.row_word);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mSourceAdapter);

        for (String source : MuteSettings.getSources()) {
            mSourceAdapter.add(source);
        }

        return v;
    }

    public void removeSource(String source) {
        mSourceAdapter.remove(source);
        MuteSettings.removeSource(source);
        MuteSettings.saveMuteSettings();
    }

    public class SourceAdapter extends ArrayAdapter<String> {

        private ArrayList<String> mSourceList = new ArrayList<String>();
        private LayoutInflater mInflater;
        private int mLayout;

        class ViewHolder {
            @InjectView(R.id.word) TextView mWord;
            @InjectView(R.id.trash) FontelloTextView mTrash;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }

        public SourceAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLayout = textViewResourceId;
        }

        @Override
        public void add(String source) {
            super.add(source);
            mSourceList.add(source);
        }

        public void remove(String source) {
            super.remove(source);
            mSourceList.remove(source);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
                if (view == null) {
                    return null;
                }
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final String source = mSourceList.get(position);

            viewHolder.mWord.setText(source);
            viewHolder.mTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Bundle args = new Bundle(1);
                    args.putString("source", source);

                    final ConfirmDialogFragment fragment = new ConfirmDialogFragment();
                    fragment.setArguments(args);
                    fragment.show(getFragmentManager(), "dialog");
                }
            });

            return view;
        }
    }

    public final class ConfirmDialogFragment extends DialogFragment {
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
                            removeSource(source);
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
}
