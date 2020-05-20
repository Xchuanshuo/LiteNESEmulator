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

    public static String getSaveName(String path) {
        return MD5Util.encrypt(path);
    }
}
