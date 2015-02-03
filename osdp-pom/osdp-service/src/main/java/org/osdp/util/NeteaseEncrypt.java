package org.osdp.util;

import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

//import org.apache.commons.codec.binary.Base64;

/**
 * RSA签名工具类
 * @author wangran
 *
 */
@Repository
public class NeteaseEncrypt {

	private static final Logger logger = LoggerFactory.getLogger(NeteaseEncrypt.class);
	
	public static final String KEY_ALGORITHM = "RSA";

	private static final String encoding = "UTF-8";
	
    @Value(value = "@{htf.private.key}")
	private String PRIVATEKEY;
    
    @Value(value = "@{htf.public.key}")
	private String PUBLICKEY;

    /**
     * 对业务数据进行添加签名操作
     * @param dataStr base64编码过的业务json数据
     * @return 签名结果
     */
	public String envolopData(String dataStr) {
		String plainText = "";
		try {
			plainText = rsaSign(PRIVATEKEY, dataStr);
		} catch (Exception e) {
			logger.error("签名失败：", e);
		}
		return plainText;
	}

   /**
	 * JDK base64 编码
	 * @param msg 编码源数据
	 * @return 编码结果
	 * @throws Exception 编码失败错误
	 */
	public String base64Encoder(String msg) throws Exception {
		BASE64Encoder base64encoder = new BASE64Encoder();
		return base64encoder.encodeBuffer(msg.getBytes(encoding)).replaceAll(
				"\r|\n", "");
	}

	/**
	 * JDK base64 解码
	 * @param msg 解码源数据
	 * @return 解码结果
	 * @throws Exception 解码失败错误
	 */
	public String base64Decoder(String msg) throws Exception {
		BASE64Decoder base64decoder = new BASE64Decoder();
		return new String(base64decoder.decodeBuffer(msg), encoding);
	}
	/*public String base64Decoder(String msg) throws Exception {
//		BASE64Decoder base64decoder = new BASE64Decoder();
		return new String(Base64.decodeBase64(msg), encoding);
	}*/
	
	/*public String base64Encoder(String msg) throws Exception {
//		BASE64Encoder base64encoder = new BASE64Encoder();
//		return base64encoder.encodeBuffer(msg.getBytes(encoding)).replaceAll(
//				"\r|\n", "");
		// return new String(Base64.encodeBase64(msg.getBytes(encoding)));
		return Base64.encodeBase64URLSafeString(msg.getBytes(encoding));
	}*/
	

	/**
	 * 解码RSA签名
	 * @param sign 签名串
	 * @param msg 业务数据
	 * @return 解密结果
	 */
	public String DecryptData(String sign, String msg) {
		String plainText = "";
		try {
			boolean verifyFlag = false;
			verifyFlag = rsaVerify(PUBLICKEY, sign, msg, encoding);

			if (verifyFlag) {
				plainText = base64Decoder(msg);
			}
		} catch (Exception e) {
			logger.error("解密错误", e);
		}
		return plainText;
	}

	/**
	 * RSA签名加密算法
	 * @param priKey RSA私钥
	 * @param src 待加密数据
	 * @return 加密后的字符串
	 */
	public String rsaSign(String priKey, String src) {
		String sign = "";
		try {
			BASE64Decoder base64decoder = new BASE64Decoder();
			BASE64Encoder base64encoder = new BASE64Encoder();
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
					base64decoder.decodeBuffer(priKey));
			KeyFactory fac = KeyFactory.getInstance("RSA");

			RSAPrivateKey privateKey = (RSAPrivateKey) fac
					.generatePrivate(keySpec);
			Signature sigEng = Signature.getInstance("SHA1withRSA");
			sigEng.initSign(privateKey);
			sigEng.update(src.getBytes(encoding));
			byte[] signature = sigEng.sign();
			sign = base64encoder.encodeBuffer(signature);
			sign = sign.replaceAll("\r|\n", "");
			sign = URLEncoder.encode(sign);
		} catch (Exception e) {
			logger.error("加签error", e);
		}
		return sign;
	}

	/**
	 * RSA签名验证，用于对请求消息的签名做校验
	 * @param pubKey 公钥
	 * @param sign RSA签名字符串
	 * @param src 源数据
	 * @param encoding 编码 默认为UTF-8
	 * @return 返回验证结果
	 */
	public boolean rsaVerify(String pubKey, String sign, String src,
			String encoding) {
		boolean rs = false;
		try {
			BASE64Decoder base64decoder = new BASE64Decoder();
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
					base64decoder.decodeBuffer(pubKey));
			KeyFactory fac = KeyFactory.getInstance("RSA");
			RSAPublicKey rsaPubKey = (RSAPublicKey) fac.generatePublic(keySpec);

			Signature sigEng = Signature.getInstance("SHA1withRSA");
			sigEng.initVerify(rsaPubKey);
			sigEng.update(src.getBytes(encoding));
			byte[] signature = base64decoder.decodeBuffer(sign);
			rs = sigEng.verify(signature);
		} catch (Exception e) {
			logger.error("验签error", e);
		}
		return rs;
	}

}
