package info.justaway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import info.justaway.task.FavoriteTask;

public class FavoriteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        long statusId = data.getLongExtra("statusId", -1L);
        if (statusId > 0) {
            new FavoriteTask(statusId).execute();
        }
        finish();
    }
}
