package org.osdp.util;


import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

import com.tuniu.ngsp.tnc.dto.SmsInfoDto;
import com.tuniu.ngsp.tnc.util.JsonExceptionUtil;
import com.tuniu.operation.platform.hogwarts.util.JsonUtil;
/**
 * 汇添富RSA签名服务类
 * @author wangran
 *
 */
@Service
public class SignManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SignManager.class);
	
	@Resource
	NeteaseEncrypt neteaseEncrypt;
	
	@Resource
	NeteaseEncryptAES neteaseEncryptAES;
    
	/**
	 * 解析签名后将数据为json字符串
	 * @param sign 签名RSA串
	 * @param msg  业务数据base64编码数据
	 * @return 解析出来json字符串
	 */
	public String getDecryptData(String sign, String msg) {
		if (sign == null || "".equals(sign)) {
			return "";
		} else {
			String djson = "";

			//NeteaseEncryptAES neteaseEncryptAES = new NeteaseEncryptAES();
			msg = neteaseEncryptAES.decryptByKey(msg);
			djson = neteaseEncrypt.DecryptData(sign, msg);
			
			if (djson == "" || djson == null) {
				return "";
			}
			return djson;
		}
	}
	
	/**
	 * 解析签名后将数据转为业务DTO
	 * @param sign 签名RSA串
	 * @param msg  业务数据base64编码数据
	 * @param sampleObject 业务DTO实例
	 * @return 解析出来的业务数据对象
	 */
	public <T> T getRelevantBean(String sign, String msg,
			T sampleObject) throws Exception {
		String json = getDecryptData(sign, msg);

		if ("".equals(json)) {
			return null;
		}
		return (T) JsonExceptionUtil.toBean(json, sampleObject.getClass());
	}

	/**
	 * 对业务数据对象进行加签
	 * @param obj 业务数据对象
	 * @return 结果  index 0 ：RSA签名结果，1：json+base64加密的业务数据，即接口中得msg字段
	 */
	public String[] getRetParam(Object obj) {
		JSONObject jsonObject = JSONObject.fromObject(obj);
		String content = jsonObject.toString();

		try {
			content = neteaseEncrypt.base64Encoder(content);
		} catch (Exception e) {
			logger.error("调用neteaseEncrypt.base64Encoder方法失败", e);
		}
		// msg在base64之后对其进行AES加密
		String econ = neteaseEncrypt.envolopData(content);
//		NeteaseEncryptAES neteaseEncryptAES = new NeteaseEncryptAES();
		content = neteaseEncryptAES.encryptByKey(content);
		String param[] = new String[2];
		param[0] = econ;
		param[1] = content;
		return param;
	}
}
