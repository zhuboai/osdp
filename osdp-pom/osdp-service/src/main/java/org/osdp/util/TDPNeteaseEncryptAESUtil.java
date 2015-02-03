package org.osdp.util;

import javax.crypto.Cipher;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

import com.tuniu.operation.platform.tsg.base.core.utils.JsonUtil;

public class TDPNeteaseEncryptAESUtil {
	private static final String encoding = "UTF-8";

	/**
	 * 加密算法
	 */
	private static final String CRYPTOGRAPHIC_ALGORITHM = "AES";

	/**
	 * 加密字符编码
	 */
	private static final String SECURERANDOM_ALGORITHM = "SHA1PRNG";

	/**
	 * aes密钥
	 */
	private static String AES_KEY = "8B5A387393D1CAAC576CC07A3F0B61E3";

	/**
	 * 加密
	 */
	public static String encryptByKey(Object inputContent) {
		String content = JsonUtil.toString(inputContent);
		if (StringUtils.isBlank(content)) {
			return null;
		}
		content = content.trim();// 去除前后空格
		try {
			SecureRandom secureRandom = SecureRandom.getInstance(SECURERANDOM_ALGORITHM);
			secureRandom.setSeed(AES_KEY.getBytes(encoding));
			KeyGenerator kgen = KeyGenerator.getInstance(CRYPTOGRAPHIC_ALGORITHM);
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] keyb = secretKey.getEncoded();
			SecretKeySpec sks = new SecretKeySpec(keyb, CRYPTOGRAPHIC_ALGORITHM);
			Cipher ecipher = Cipher.getInstance(CRYPTOGRAPHIC_ALGORITHM);// 创建加密密码器
			ecipher.init(Cipher.ENCRYPT_MODE, sks);
			byte[] byteContent = content.getBytes(encoding);
			byte[] result = null;
			result = ecipher.doFinal(byteContent);
			return parseByte2HexStr(result);// 16进制的转换
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解密
	 */
	public static String decryptByKey(String content) {
		if (StringUtils.isBlank(content)) {
			return null;
		}
		try {
			SecureRandom secureRandom = SecureRandom.getInstance(SECURERANDOM_ALGORITHM);
			secureRandom.setSeed(AES_KEY.getBytes(encoding));
			KeyGenerator kgen = KeyGenerator.getInstance(CRYPTOGRAPHIC_ALGORITHM);
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] keyb = secretKey.getEncoded();
			SecretKeySpec sks = new SecretKeySpec(keyb, CRYPTOGRAPHIC_ALGORITHM);
			Cipher dcipher = Cipher.getInstance(CRYPTOGRAPHIC_ALGORITHM);// 创建加密密码器
			dcipher.init(Cipher.DECRYPT_MODE, sks);
			byte[] contentTemp = parseHexStr2Byte(content); // 将16进制字符串变为加密字符串
			byte[] result = null;
			result = dcipher.doFinal(contentTemp);
			return new String(result, encoding);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 转化字符串为十六进制编码
	 * 
	 * @param s
	 * @return
	 */
	public static String StringToHex(String s) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			str.append(Integer.toHexString((int) s.charAt(i)));
		}
		return str.toString();
	}

	/**
	 * 转化十六进制编码为字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String HexToString(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, encoding);// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static void main(String args[]) {
		String a = "xupeng";
		String encryStr = encryptByKey(a);
		System.out.println(encryStr);
		String deStr = decryptByKey(encryStr);
		System.out.println(deStr);
	}

}
