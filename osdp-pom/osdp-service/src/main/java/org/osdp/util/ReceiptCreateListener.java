package org.osdp.util;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.security.Credential.MD5;

import com.dianping.combiz.spring.util.PropertiesLoaderSupportUtils;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.BackoutMessageException;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.tpfun.promo.biz.utils.JsonUtils;
import com.dianping.tuangou.receipt.dto.notify.ReceiptIssueNotifyDTO;
import com.dianping.userremote.base.dto.UserDTO;
import com.dianping.userremote.base.service.UserService;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;



public class ReceiptCreateListener implements MessageListener{

	
	private final Logger logger = Logger.getLogger(getClass());
	private UserService userService;
	@Override
	public void onMessage(Message msg) throws BackoutMessageException {
		// TODO Auto-generated method stub
		
    	logger.info("receipt swallow create content="+msg.getContent());
    	ReceiptIssueNotifyDTO issueDTO = msg.transferContentToBean(ReceiptIssueNotifyDTO.class);
    	if(isWandaDeal(issueDTO)){
    		logger.info("start send wanda deal");
    		sendWandaDeal(issueDTO);
    	}

	}
	
	private boolean isWandaDeal(ReceiptIssueNotifyDTO dto){
		if(dto==null)
			{
			return false;
			}
		//判断团单id是否为万达的团单
		String dealgroupId=PropertiesLoaderSupportUtils.getProperty("tpfun-promo-service.temp15.wanda.dealgroupId");
		if(StringUtils.isEmpty(dealgroupId))
		{
			return false;
		}
		if(dto.getOrder().getProductGroupId()==Integer.parseInt(dealgroupId)){
			return true;
		}
		return false;
	}
	public void sendWandaDeal(ReceiptIssueNotifyDTO dto){

		//String mobileNo=dto.getOrder().getMobileNo();
		String mobileNo="17091839605";
		if(StringUtils.isEmpty(mobileNo))
		{
			UserDTO userDTO=userService.loadUser(dto.getOrder().getUserId());
			mobileNo=userDTO.getMobileNO();
			
		}
		if(StringUtils.isEmpty(mobileNo)){
			logger.error("can't get mobileNO orderId ="+dto.getOrder().getOrderId()+" userId= "+dto.getOrder().getUserId());
			return;
		}
		try {
			
		Protocol protocol=new Protocol("https", new MySSLSocketFactory(), 443);
		Protocol.registerProtocol("https", protocol);
        HttpClient httpClient=buildHttpClient();
//        String url="https://openapi.wanhui.cn/ucenter/bind_third_user";
//        String token="044cec0e88dcb14f7be1510c33ef5920";
//        String appid="dianping";
//        String channel="1";
//        String third_id="th140211104740000011";
        String urlParas=PropertiesLoaderSupportUtils.getProperty("tpfun-promo-service.temp15.wanda.client-config");
        Map<String, Object> paraMap=JsonUtils.fromJson(urlParas);

        Date date=new Date();
        long ts=date.getTime()/1000;
        String neenSignString=(String)paraMap.get("token")+ts+paraMap.get("appid");
        String sign=MD5.digest(neenSignString);
		String hostUrl=(String)paraMap.get("url");
		
        String sign2=sign.split(":")[1];
        String postUrl=hostUrl+"?"+"appid="+paraMap.get("appid")+"&channel="+paraMap.get("channel")+"&mobile="+mobileNo+"&third_id="+paraMap.get("third_id")+"&ts="+ts+"&sign="+sign2;
        PostMethod postMethod= new PostMethod(postUrl);
		
			
			httpClient.getParams().setContentCharset("UTF-8"); 
			int responseStatus=httpClient.executeMethod(postMethod);
	        if (responseStatus!= HttpStatus.SC_OK) {
	            logger.warn("send wanda failed reason: " +responseStatus + " " + ", orderId = " + dto.getOrder().getOrderId());
	        }
	        else
	        {
	           String body = postMethod.getResponseBodyAsString();  
	           logger.info("send wanda success !  response content :"+body);

	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("send wanda error ", e);
		}
		
	}
	private HttpClient buildHttpClient(){
		MultiThreadedHttpConnectionManager conmgr = new MultiThreadedHttpConnectionManager();
		try {
			conmgr.getParams().setConnectionTimeout(30000);
			conmgr.getParams().setSoTimeout(30000);
			conmgr.getParams().setDefaultMaxConnectionsPerHost(100);
			conmgr.getParams().setMaxTotalConnections(100);
			conmgr.closeIdleConnections(30000*2);
			HttpClient httpClient = new HttpClient(conmgr);
			httpClient.getParams().setConnectionManagerTimeout(30000);
			// 重试次数 默认为3
			DefaultHttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(3, false);
			httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
			return httpClient;
		} catch (Exception e){// TODO Auto-generated catch block
		
			logger.error("build httpclient error ", e);;
			
		} 
	     
		return null;
		
		
	}
	

}
