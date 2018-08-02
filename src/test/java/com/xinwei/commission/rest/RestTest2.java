package com.xinwei.commission.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.xinwei.coobill.bankproxy.domain.BankProxyRequest;
import com.xinwei.coobill.bankproxy.domain.BankProxyResponse;
import com.xinwei.coobill.bankproxy.facade.BankProxyInterface;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;
import com.xinwei.orderpost.facade.CommissionPresentService;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月19日 下午1:32:22
 * 
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RestTest2 {
	@Value("${transfer.accesskey}")
	private String transferAccessKey;

	@Autowired
	private BankProxyInterface bankProxyInterface;

	@Autowired
	private CommissionPresentService commissionPresentService;

	@Test
	public void getCreditBalanceTest() {
		//		String userId = "1671438677";
		String userId = "1532001100";
		ProcessResult result1 = getCreditBalance(userId);
		System.out.println("################### start getCreditBalanceTest ##############################");
		System.out.println("###getCreditBalance=" + result1);
		System.out.println("################### end getCreditBalanceTest ##############################");
	}

	//	@Test
	public void reduceBalanceTest() {
		String userId = "4294987297";
		String orderId = "";
		double money = 10;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date date = null;
		try {
			date = simpleDateFormat.parse("2018-04-02 23:21:22");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("################### start reduceBalanceTest ##############################");
		ProcessResult result1 = reduceBalance(userId, orderId, money, date);
		System.out.println("###getCreditBalance=" + result1);
		System.out.println("################### end reduceBalanceTest ##############################");
	}

	public ProcessResult getCreditBalance(String uid) {
		ProcessResult processResult = new ProcessResult();
		BankProxyRequest bankProxyRequest = new BankProxyRequest();
		long creditUid = getCreditUid(uid);
		bankProxyRequest.setUid(creditUid);
		BankProxyResponse bankProxyResponse = bankProxyInterface.queryBankBalance(bankProxyRequest);
		processResult.setRetCode(bankProxyResponse.getReturnCode());
		//		if (ScratchCardConst.RESULT_SUCCESS != bankProxyResponse.getReturnCode()) {
		if (0 != bankProxyResponse.getReturnCode()) {
			return processResult;
		}
		/**
		 * bak1 保存余额
		 */
		processResult.setResponseInfo(bankProxyResponse.getBak1());
		return processResult;
	}

	public long getCreditUid(String uid) {
		// long creditUid = 10*0xFFFFFFFF + Long.parseLong(uid);
		return Long.parseLong(uid);
	}

	/**
	 * 减钱
	 * 
	 * @return
	 */
	public ProcessResult reduceBalance(String uid, String orderId, double money, Date requestTime) {
		List<CommissionPresentInfo> commissionPresentInfoList = new ArrayList<CommissionPresentInfo>();
		CommissionPresentInfo commissionPresentInfo = new CommissionPresentInfo();
		commissionPresentInfo.setSubsId(getCreditUid(uid));
		commissionPresentInfo.setAmt(money);
		commissionPresentInfo.setMsgInfo("reduce money");
		//		commissionPresentInfo.setMsgInfo("add money");
		commissionPresentInfo.setBizType(0);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, 1);
		commissionPresentInfo.setExpireTime(getTimeyyyyMMDDHHmmss(now.getTime()));
		commissionPresentInfo.setOperType(0);
		commissionPresentInfo.setOrderID(orderId);
		commissionPresentInfo.setReqTransId(getTransactionId(orderId, requestTime));
		commissionPresentInfo.setReason("reduce money");
		commissionPresentInfo
				.setSignInfo(OrderPostUtil.getPresentSignInfo(commissionPresentInfo, this.transferAccessKey));
		commissionPresentInfoList.add(commissionPresentInfo);
		ProcessResult processResult = null;
		try {
			processResult = commissionPresentService.presentCommission(commissionPresentInfoList);
			@SuppressWarnings("unchecked")
			List<CommissionPresentInfo> retCommissionPresentInfoList = (List<CommissionPresentInfo>) processResult
					.getResponseInfo();
			//			Gson gson = new Gson();
			//			List<CommissionPresentInfo> retCommissionPresentInfoList = gson
			//					.fromJson((String) processResult.getResponseInfo(), new TypeToken<List<CommissionPresentInfo>>() {
			//					}.getType());

			if (retCommissionPresentInfoList != null && retCommissionPresentInfoList.size() > 0) {
				processResult.setRetCode(retCommissionPresentInfoList.get(0).getResult());
				processResult.setResponseInfo(retCommissionPresentInfoList.get(0));
				processResult.setRetMsg(retCommissionPresentInfoList.get(0).getReason());
			} else {
				//				return ControllerUtils.getErrorResponse(-1, "operate balance error");

				ProcessResult result = new ProcessResult();
				result.setRetCode(-1);
				result.setRetMsg("operate balance error");
				return result;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//			processResult = ControllerUtils.getFromResponse(e, ScratchCardConst.RESULT_HANDLE_EXCEPTION, processResult);

			ProcessResult result = new ProcessResult();
			result.setRetCode(-1);
			result.setRetMsg(getStringFromException(e));
			return result;
		}
		return processResult;
	}

	public String getTimeyyyyMMDDHHmmss(Date time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return simpleDateFormat.format(time);
	}

	public String getTransactionId(String orderId, Date requestTime) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String transId = "20" + simpleDateFormat.format(requestTime) + orderId;
		return transId;
	}

	private String getStringFromException(Exception e) {
		if (e != null) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String errorStr = errors.toString();
			return errorStr;
		}
		return "";
	}
}
