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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.settings.MuteSettings;

public class SourceFragment extends Fragment {

    private MuteSettings mMuteSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);
        if (v == null) {
            return null;
        }

        SourceAdapter adapter = new SourceAdapter(getActivity(), R.layout.row_word);

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(adapter);

        JustawayApplication application = JustawayApplication.getApplication();
        mMuteSettings = application.getMuteSettings();
        for (String source : mMuteSettings.getSources()) {
            adapter.add(source);
        }

        return v;
    }

    public class SourceAdapter extends ArrayAdapter<String> {

        private ArrayList<String> mSourceList = new ArrayList<String>();
        private LayoutInflater mInflater;
        private int mLayout;

        public SourceAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
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

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
                assert view != null;
            }

            final String source = mSourceList.get(position);

            ((TextView) view.findViewById(R.id.word)).setText(source);

            TextView trash = (TextView) view.findViewById(R.id.trash);
            trash.setTypeface(JustawayApplication.getFontello());

            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(String.format(getString(R.string.confirm_destroy_mute), source))
                            .setPositiveButton(
                                    R.string.button_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            remove(source);
                                            mMuteSettings.removeSource(source);
                                            mMuteSettings.saveMuteSettings();
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
