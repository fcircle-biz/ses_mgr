package jp.co.example.sesapp.util;

import java.util.UUID;

/**
 * 文字列操作のユーティリティクラス.
 */
public final class StringUtils {

    private StringUtils() {
        // ユーティリティクラスなのでインスタンス化を防止
    }

    /**
     * 文字列がnullまたは空文字かどうかを判定します.
     *
     * @param str チェックする文字列
     * @return nullまたは空文字の場合はtrue、それ以外はfalse
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 文字列が空白文字のみかどうかを判定します.
     *
     * @param str チェックする文字列
     * @return nullまたは空文字または空白文字のみの場合はtrue、それ以外はfalse
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 文字列がnullでない、かつ空文字でないかどうかを判定します.
     *
     * @param str チェックする文字列
     * @return null以外かつ空文字以外の場合はtrue、それ以外はfalse
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 文字列がnullでない、かつ空白文字のみでないかどうかを判定します.
     *
     * @param str チェックする文字列
     * @return null以外かつ空白文字のみでない場合はtrue、それ以外はfalse
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 文字列がnullの場合は空文字を返し、それ以外は元の文字列を返します.
     *
     * @param str 変換する文字列
     * @return nullの場合は空文字、それ以外は元の文字列
     */
    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 文字列が指定された最大長を超える場合は、切り詰めて末尾に省略記号を付けます.
     *
     * @param str 対象の文字列
     * @param maxLength 最大長
     * @param suffix 省略記号（デフォルトは「...」）
     * @return 切り詰められた文字列
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (str == null) {
            return null;
        }
        
        String actualSuffix = suffix != null ? suffix : "...";
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        int truncateLength = maxLength - actualSuffix.length();
        if (truncateLength < 0) {
            truncateLength = 0;
        }
        
        return str.substring(0, truncateLength) + actualSuffix;
    }

    /**
     * 文字列が指定された最大長を超える場合は、切り詰めて末尾に「...」を付けます.
     *
     * @param str 対象の文字列
     * @param maxLength 最大長
     * @return 切り詰められた文字列
     */
    public static String truncate(String str, int maxLength) {
        return truncate(str, maxLength, "...");
    }

    /**
     * ランダムなUUID文字列を生成します.
     *
     * @return ランダムなUUID文字列
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * ハイフンなしのUUID文字列を生成します.
     *
     * @return ハイフンなしのランダムなUUID文字列
     */
    public static String generateCompactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 指定された文字が全角カタカナかどうかを判定します.
     *
     * @param c 判定する文字
     * @return 全角カタカナの場合はtrue、それ以外はfalse
     */
    public static boolean isFullWidthKatakana(char c) {
        return c >= 'ァ' && c <= 'ヶ';
    }

    /**
     * 指定された文字列が全角カタカナのみで構成されているかどうかを判定します.
     *
     * @param str 判定する文字列
     * @return 全角カタカナのみで構成されている場合はtrue、それ以外はfalse
     */
    public static boolean isFullWidthKatakana(String str) {
        if (isBlank(str)) {
            return false;
        }
        
        for (int i = 0; i < str.length(); i++) {
            if (!isFullWidthKatakana(str.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
}