package info.justaway;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import twitter4j.Twitter;

public class SearchActivity extends Activity {

    private Context context;
    private Twitter twitter;
    private EditText searchWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = this;

        JustawayApplication application = JustawayApplication.getApplication();
        twitter = application.getTwitter();

        Button search = (Button) findViewById(R.id.search);
        search.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));

        searchWords = (EditText) findViewById(R.id.searchWords);

        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
}