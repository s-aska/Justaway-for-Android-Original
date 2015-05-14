package info.justaway.util;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.model.AccessTokenManager;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class StatusUtil {
    private static final Pattern TWITPIC_PATTERN = Pattern.compile("^http://twitpic\\.com/(\\w+)$");
    private static final Pattern TWIPPLE_PATTERN = Pattern.compile("^http://p\\.twipple\\.jp/(\\w+)$");
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("^https?://instagram\\.com/p/([^/]+)/$");
    private static final Pattern PHOTOZOU_PATTERN = Pattern.compile("^http://photozou\\.jp/photo/show/\\d+/(\\d+)$");
    private static final Pattern IMAGES_PATTERN = Pattern.compile("^https?://.*\\.(png|gif|jpeg|jpg)$");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("^https?://(?:www\\.youtube\\.com/watch\\?.*v=|youtu\\.be/)([\\w-]+)");
    private static final Pattern NICONICO_PATTERN = Pattern.compile("^http://(?:www\\.nicovideo\\.jp/watch|nico\\.ms)/sm(\\d+)$");
    private static final Pattern PIXIV_PATTERN = Pattern.compile("^http://www\\.pixiv\\.net/member_illust\\.php");

    private static final Pattern URL_PATTERN = Pattern.compile("(http://|https://)[\\w\\.\\-/:#\\?=&;%~\\+]+");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@[a-zA-Z0-9_]+");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#\\S+");

    /**
     * source(via)からクライアント名を抜き出す
     *
     * @param source <a href="クライアントURL">クライアント名</a>という文字列
     * @return クライアント名
     */
    public static String getClientName(String source) {
        String[] tokens = source.split("[<>]");
        if (tokens.length > 1) {
            return tokens[2];
        } else {
            return tokens[0];
        }
    }

    /**
     * 自分宛てのメンションかどうかを判定する
     *
     * @param status ツイート
     * @return true ... 自分宛てのメンション
     */
    public static boolean isMentionForMe(Status status) {
        long userId = AccessTokenManager.getUserId();
        if (status.getInReplyToUserId() == userId) {
            return true;
        }
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        for (UserMentionEntity mention : mentions) {
            if (mention.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 短縮URLを表示用URLに置換する
     *
     * @param status ツイート
     * @return 短縮URLを展開したツイート本文
     */
    public static String getExpandedText(Status status) {
        String text = status.getText();
        for (URLEntity url : status.getURLEntities()) {
            Pattern p = Pattern.compile(url.getURL());
            Matcher m = p.matcher(text);
            text = m.replaceAll(url.getExpandedURL());
        }

        for (MediaEntity media : status.getMediaEntities()) {
            Pattern p = Pattern.compile(media.getURL());
            Matcher m = p.matcher(text);
            text = m.replaceAll(media.getExpandedURL());
        }
        return text;
    }

    /**
     * ツイートに含まれる画像のURLをすべて取得する
     *
     * @param status ツイート
     * @return 画像のURL
     */
    public static ArrayList<String> getImageUrls(Status status) {
        ArrayList<String> imageUrls = new ArrayList<String>();
        for (URLEntity url : status.getURLEntities()) {
            Matcher twitpic_matcher = TWITPIC_PATTERN.matcher(url.getExpandedURL());
            if (twitpic_matcher.find()) {
                imageUrls.add("http://twitpic.com/show/full/" + twitpic_matcher.group(1));
                continue;
            }
            Matcher twipple_matcher = TWIPPLE_PATTERN.matcher(url.getExpandedURL());
            if (twipple_matcher.find()) {
                imageUrls.add("http://p.twpl.jp/show/orig/" + twipple_matcher.group(1));
                continue;
            }
            Matcher instagram_matcher = INSTAGRAM_PATTERN.matcher(url.getExpandedURL());
            if (instagram_matcher.find()) {
                imageUrls.add(url.getExpandedURL() + "media?size=l");
                continue;
            }
            Matcher photozou_matcher = PHOTOZOU_PATTERN.matcher(url.getExpandedURL());
            if (photozou_matcher.find()) {
                imageUrls.add("http://photozou.jp/p/img/" + photozou_matcher.group(1));
                continue;
            }
            Matcher youtube_matcher = YOUTUBE_PATTERN.matcher(url.getExpandedURL());
            if (youtube_matcher.find()) {
                imageUrls.add("http://i.ytimg.com/vi/" + youtube_matcher.group(1) + "/hqdefault.jpg");
                continue;
            }
            Matcher niconico_matcher = NICONICO_PATTERN.matcher(url.getExpandedURL());
            if (niconico_matcher.find()) {
                int id = Integer.valueOf(niconico_matcher.group(1));
                int host = id % 4 + 1;
                imageUrls.add("http://tn-skr" + host + ".smilevideo.jp/smile?i=" + id + ".L");
                continue;
            }
            Matcher pixiv_matcher = PIXIV_PATTERN.matcher(url.getExpandedURL());
            if (pixiv_matcher.find()) {
                imageUrls.add(url.getExpandedURL());
            }
            Matcher images_matcher = IMAGES_PATTERN.matcher(url.getExpandedURL());
            if (images_matcher.find()) {
                imageUrls.add(url.getExpandedURL());
            }
        }

        if (status.getExtendedMediaEntities().length > 0) {
            for (MediaEntity media : status.getExtendedMediaEntities()) {
                imageUrls.add(media.getMediaURL());
            }
        } else {
            for (MediaEntity media : status.getMediaEntities()) {
                imageUrls.add(media.getMediaURL());
            }
        }

        return imageUrls;
    }

    public static SpannableStringBuilder generateUnderline(String str) {
        // URL、メンション、ハッシュタグ が含まれていたら下線を付ける
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(str);
        UnderlineSpan us;

        Matcher urlMatcher = URL_PATTERN.matcher(str);
        while (urlMatcher.find()) {
            us = new UnderlineSpan();
            sb.setSpan(us, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        Matcher mentionMatcher = MENTION_PATTERN.matcher(str);
        while (mentionMatcher.find()) {
            us = new UnderlineSpan();
            sb.setSpan(us, mentionMatcher.start(), mentionMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher hashtagMatcher = HASHTAG_PATTERN.matcher(str);
        while (hashtagMatcher.find()) {
            us = new UnderlineSpan();
            sb.setSpan(us, hashtagMatcher.start(), hashtagMatcher.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return sb;
    }
}
