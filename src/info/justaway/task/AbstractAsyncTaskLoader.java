package info.justaway.task;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * 一般的な AsyncTaskLoader の処理を全てに書くのは冗長なのでこれを継承して使う
 * 
 * @author aska
 * @param <D>
 */
public abstract class AbstractAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D data;

    public AbstractAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        this.data = data;

        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (data != null) {
            deliverResult(data);
        }

        if (takeContentChanged() || data == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        data = null;
    }
}
