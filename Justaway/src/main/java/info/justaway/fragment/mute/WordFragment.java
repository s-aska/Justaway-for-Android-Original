package info.justaway.fragment.mute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.justaway.R;
import info.justaway.settings.MuteSettings;
import info.justaway.util.KeyboardUtil;
import info.justaway.util.MessageUtil;
import info.justaway.widget.FontelloTextView;

public class WordFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mute_word, container, false);
        if (v == null) {
            return null;
        }

        final WordAdapter adapter = new WordAdapter(getActivity(), R.layout.row_word);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(adapter);

        for (String word : MuteSettings.getWords()) {
            adapter.add(word);
        }

        v.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(getActivity());
                KeyboardUtil.showKeyboard(editText);
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_create_mute_word)
                        .setView(editText)
                        .setPositiveButton(
                                R.string.button_save,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (editText.getText() == null) {
                                            return;
                                        }
                                        String word = editText.getText().toString();
                                        if (word.isEmpty()) {
                                            return;
                                        }
                                        adapter.add(word);
                                        MuteSettings.addWord(word);
                                        MuteSettings.saveMuteSettings();
                                        MessageUtil.showToast(R.string.toast_create_mute);
                                    }
                                }
                        )
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }
                        )
                        .show();
            }
        });

        return v;
    }

    public class WordAdapter extends ArrayAdapter<String> {

        class ViewHolder {
            @InjectView(R.id.word) TextView mWord;
            @InjectView(R.id.trash) FontelloTextView mTrash;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }

        private ArrayList<String> mWordList = new ArrayList<String>();
        private LayoutInflater mInflater;
        private int mLayout;

        public WordAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(String source) {
            super.add(source);
            mWordList.add(source);
        }

        public void remove(String source) {
            super.remove(source);
            mWordList.remove(source);
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

            final String word = mWordList.get(position);

            viewHolder.mWord.setText(word);

            viewHolder.mTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setMessage(String.format(getString(R.string.confirm_destroy_mute), word))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove(word);
                                            MuteSettings.removeWord(word);
                                            MuteSettings.saveMuteSettings();
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }
                            )
                            .show();
                }
            });

            return view;
        }
    }
}
