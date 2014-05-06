package info.justaway.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.model.AccessTokenManager;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class TwitterUtil {

    private static final Pattern URL_PATTERN = Pattern.compile("(http://|https://)[\\w\\.\\-/:#\\?=&;%~\\+]+");

    /**
     * ツイートの文字数を数えます
     * @param str ツイート文字列
     * @return 文字数
     */
    public static int count(String str) {
        int length = str.codePointCount(0, str.length());

        // 短縮URLを考慮
        Matcher matcher = URL_PATTERN.matcher(str);
        while (matcher.find()) {
            length = length - matcher.group().length() + 22;
            if (matcher.group().contains("https://")) ++length;
        }

        return 140 - length;
    }

    /**
     * source(via)からクライアント名を抜き出す
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
}
