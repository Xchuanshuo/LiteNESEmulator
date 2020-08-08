package com.legend.utils;

import java.util.regex.Pattern;

/**
 * @author Legend
 * @data by on 20-5-4.
 * @description
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isHexNumeric(String str){
        if (isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("^[A-Fa-f0-9]+$");
        return pattern.matcher(str).matches();
    }

    public static boolean isValidIP(String text) {
        if (text != null && !text.isEmpty()) {
            if ("localhost".equals(text)) return true;
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }

    public static String getSaveName(String path) {
        return MD5Util.encrypt(path);
    }
}
