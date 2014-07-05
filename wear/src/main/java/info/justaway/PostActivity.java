package info.justaway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;


public class PostActivity extends Activity implements
        DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private DelayedConfirmationView mDelayedConfirmationView;
    private String mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mStatusText = getIntent().getStringExtra("text");
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(mStatusText);
        mDelayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.timer);
        startConfirmationTimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (!mResolvingError) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void startConfirmationTimer() {
        mDelayedConfirmationView.setTotalTimeMs(3 * 1000);
        mDelayedConfirmationView.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
            @Override
            public void onTimerFinished(View view) {
                // テキストをモバイルの PostActivity に投げる
                PutDataMapRequest dataMap = PutDataMapRequest.create("/text");
                dataMap.getDataMap().putString("text", mStatusText);
                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, dataMap.asPutDataRequest());

                // TODO: ツイートができたかできてないか、電話から時計に結果を伝えたい
                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(final DataApi.DataItemResult result) {
                        if (result.getStatus().isSuccess()) {
                            startConfirmationActivity(ConfirmationActivity.SUCCESS_ANIMATION, "ok");
                        } else {
                            startConfirmationActivity(ConfirmationActivity.FAILURE_ANIMATION, "fail");
                        }
                        finish();
                    }
                });
            }

            @Override
            public void onTimerSelected(View view) {
                // キャンセル
                finish();
            }
        });
        mDelayedConfirmationView.start();
    }

    private void startConfirmationActivity(int animationType, String message) {
        Intent confirmationActivity = new Intent(this, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        startActivity(confirmationActivity);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
