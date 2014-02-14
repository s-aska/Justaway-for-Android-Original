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

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.settings.MuteSettings;

public class WordFragment extends Fragment {

    private MuteSettings mMuteSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mute_word, container, false);
        if (v == null) {
            return null;
        }

        final WordAdapter adapter = new WordAdapter(getActivity(), R.layout.row_word);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(adapter);

        final JustawayApplication application = JustawayApplication.getApplication();
        mMuteSettings = application.getMuteSettings();
        for (String word : mMuteSettings.getWords()) {
            adapter.add(word);
        }

        v.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(getActivity());
                application.showKeyboard(editText);
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
                                        mMuteSettings.addWord(word);
                                        mMuteSettings.saveMuteSettings();
                                        JustawayApplication.showToast(R.string.toast_create_mute);
                                    }
                                })
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();
            }
        });

        return v;
    }

    public class WordAdapter extends ArrayAdapter<String> {

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

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
                assert view != null;
            }

            final String word = mWordList.get(position);

            ((TextView) view.findViewById(R.id.word)).setText(word);

            TextView trash = (TextView) view.findViewById(R.id.trash);
            trash.setTypeface(JustawayApplication.getFontello());

            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(word.concat(getString(R.string.confirm_destroy_some)))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove(word);
                                            mMuteSettings.removeWord(word);
                                            mMuteSettings.saveMuteSettings();
                                        }
                                    })
                            .setNegativeButton(
                                    R.string.button_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                            .show();
                }
            });

            return view;
        }
    }
}
