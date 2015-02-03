package org.osdp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 提供 16 位与 32 位 MD5 加密
 * 
 */
public class MD5Util {

    /**
     * 32位标准 MD5Util 加密
     * 
     * @param plainText
     *            明文
     * @return 密文<br>
     *         返回 Null 值则出现异常
     */
    public static String cell32(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();// 32位的加密

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 16 位标准 MD5Util 加密
     * 
     * @param plainText
     *            明文
     * @return 密文<br>
     *         返回 Null 值则出现异常
     */
    public String cell16(String plainText) {
        String result = cell32(plainText);
        if (result == null)
            return null;
        return result.toString().substring(8, 24);// 16位的加密
    }
}