package com.xinwei.commission.service.impl;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.stereotype.Service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.commission.service.BalanceCacheService;
import com.xinwei.commission.service.BalanceService;
import com.xinwei.coobill.bankproxy.domain.BankProxyRequest;
import com.xinwei.coobill.bankproxy.domain.BankProxyResponse;
import com.xinwei.coobill.bankproxy.facade.BankProxyInterface;
import com.xinwei.coobill.bankproxy.facade.BankProxyUtil;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;

@Service("bankProxyInterface")
public class BankServiceImpl implements BankProxyInterface {

	/**
	 * 用于对外的传输加密
	 */
	@Value("${bank.accesskey}")  
	private String bankAccessKey;
	
	@Resource(name="balanceService")
	private BalanceService balanceService;
	
	/**
	 * 对commission赠送的接入请求的加密校验
	 * @param commissionPresentInfo
	 * @return
	 */
	protected int checkAccessData(BankProxyRequest bankProxyRequest)
	{
		String securityInfo = BankProxyUtil.signBankProxyRequest(bankProxyRequest, bankAccessKey);
		if(securityInfo.equalsIgnoreCase(bankProxyRequest.getSecurityInfo()))
	    {
	    	return UserBalanceApplyConst.RESULT_SUCCESS;
	    }
	    return BalanceServiceConst.Error_signo_error;
	    
	   
	}
	
	public BalanceTransRunning getFromBankProxyReq(BankProxyRequest bankProxyRequest)
	{
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setUserid(bankProxyRequest.getUid());
		//因为是赠送接口
		double amount = bankProxyRequest.getAmt();
		balanceTransRunning.setAmount(amount);
		balanceTransRunning.setBalance(0d);
		//todo:
		balanceTransRunning.setBizsource("bank");
		balanceTransRunning.setBiztype("biztype");
		//todo:
		Calendar now = Calendar.getInstance();
		now.add(Calendar.HOUR_OF_DAY, 1);
		Date expireTime = now.getInstance().getTime();
		
		
		balanceTransRunning.setExpiretime(expireTime);
		balanceTransRunning.setOpertype("opertype");
		balanceTransRunning.setOrderid(bankProxyRequest.getTransactionId());
		//todo:
		//balanceTransRunning.setSrcipaddress(srcipaddress);
		balanceTransRunning.setStatus(BalanceServiceConst.Btrans_status_init);
		balanceTransRunning.setTransdesc("desc");
		balanceTransRunning.setTransid(bankProxyRequest.getTransactionId());
		
		balanceTransRunning.setTransactionTime(OrderPostUtil.getDateFromTransID(bankProxyRequest.getTransactionId()));
		return balanceTransRunning;
	}
	
	
	@Override
	public BankProxyResponse identifyUser(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * will to do ,先申请
	 */
	@Override
	public BankProxyResponse initialiseTransaction(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		BankProxyResponse bankProxyResponse = new BankProxyResponse();
		bankProxyResponse.setReturnCode(BankProxyResponse.STATUS_BANKREQSUCCESS);
		return bankProxyResponse;
	}

	/**
	 * will to do 
	 */
	@Override
	public BankProxyResponse queryBankBalance(BankProxyRequest arg0) {
		// TODO Auto-generated method stub

		BankProxyResponse bankProxyResponse = new BankProxyResponse();
		bankProxyResponse.setReturnCode(bankProxyResponse.STATUS_BANKREQTIMEOUT);
		try {
			BalanceServiceContext bServiceContext = new BalanceServiceContext();

			// balanceServiceContext.setCommissionPresentInfo(commissionPresentInfo);

			int iRet = this.balanceService.getBalance(bServiceContext, arg0.getUid());
			bankProxyResponse.setReturnCode(iRet);
			if(iRet==0)
			{
				DecimalFormat    df   = new DecimalFormat("#######.##");  
				//bak1保存余额
				bankProxyResponse.setBak1(df.format(bServiceContext.getUserDbBalance().getBalance()));
				//币种
				bankProxyResponse.setBak2("2");
			}
			bServiceContext=null;
			return bankProxyResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bankProxyResponse;
		
	}

	@Override
	public BankProxyResponse queryTransaction(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BankProxyResponse queryTransactionRefund(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BankProxyResponse refund(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BankProxyResponse resendOtp(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * will to do 
	 */
	@Override
	public BankProxyResponse verifyTransaction(BankProxyRequest arg0) {
		// TODO Auto-generated method stub
		BankProxyResponse bankProxyResponse  = new BankProxyResponse();
		bankProxyResponse.setReturnCode(bankProxyResponse.STATUS_BANKREQTIMEOUT);
		//校验签名
		try {
			int iRet = checkAccessData(arg0);
			if(iRet !=UserBalanceApplyConst.RESULT_SUCCESS)
			{
				bankProxyResponse.setReturnCode(iRet);
				String resCrc = BankProxyUtil.signBankProxyResponse(bankProxyResponse, bankAccessKey);
				bankProxyResponse.setSecurityInfo(resCrc);
				return bankProxyResponse;
			}
			
			BalanceServiceContext balanceServiceContext = new BalanceServiceContext();
			
			BalanceTransRunning bTransRunning= this.getFromBankProxyReq(arg0);
			balanceServiceContext.setWillDoneBTransRunning(bTransRunning);
			//balanceServiceContext.setCommissionPresentInfo(commissionPresentInfo);
			
			iRet = this.balanceService.processBalance(balanceServiceContext, bTransRunning);
			bankProxyResponse.setReturnCode(iRet);
			String resCrc = BankProxyUtil.signBankProxyResponse(bankProxyResponse, bankAccessKey);
			bankProxyResponse.setSecurityInfo(resCrc);
			return bankProxyResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String resCrc = BankProxyUtil.signBankProxyResponse(bankProxyResponse, bankAccessKey);
		bankProxyResponse.setSecurityInfo(resCrc);
		return bankProxyResponse;
	}

}
