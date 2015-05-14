package info.justaway.downloader;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomImageDownaloder extends BaseImageDownloader {
    private static final Pattern PIXIV_PATTERN = Pattern.compile("^http://\\w+.pixiv\\.net");

    public CustomImageDownaloder(Context context) {
        super(context);
    }

    public CustomImageDownaloder(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = super.createConnection(url, extra);
        Matcher pixiv_matcher = PIXIV_PATTERN.matcher(url);
        if (pixiv_matcher.find()) {
            conn.setRequestProperty("Referer", "http://www.pixiv.com/");
        }
        return conn;
    }
}
