package com.legend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author legend
 */
public class MD5Util {

    private static final char hexDigits[] = { '0', '1', '2', '3'
                                            , '4', '5', '6', '7'
                                            , '8', '9', 'A', 'B'
                                            , 'C', 'D', 'E', 'F' };
    private static final String salt = "1a2b3cqsacs.*4d";

    public static String encryptAddSalt(String s) {
        StringBuilder builder = new StringBuilder();
        for (int i=0;i<s.length();i++) {
            builder.append(s.charAt(i)+salt.charAt(i/salt.length()));
        }
        return encrypt(builder.toString());
    }

    public static String encrypt(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("The params is invalid!");
        }
        byte[] bytes = new byte[1024];
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            int read = 0;
            while ((read = inputStream.read(bytes)) != -1) {
                mdInst.update(bytes, 0, read);
            }
            return getMd5StrByCipher(mdInst.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String encrypt(String s) {
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            return getMd5StrByCipher(md);
        } catch (Exception e) {
            return null;
        }
    }

    // 把密文转换成十六进制的字符串形式
    private static String getMd5StrByCipher(byte[] md) {
        int j = md.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

    public static void main(String[] args) {
        String string = encrypt("123456");
        System.out.println(string);
    }

}
