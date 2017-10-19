package com.xinwei.commission.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.commission.service.BalanceService;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;

import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;
import com.xinwei.orderpost.facade.CommissionPresentService;
@Service("commissionPresentService")
public class CommissionServiceImpl implements CommissionPresentService {
	/**
	 * 用于对外的传输加密
	 */
	@Value("${transfer.accesskey}")  
	private String transferAccessKey;
	
	@Resource(name="balanceService")
	private BalanceService balanceService;
	
	/**
	 * 获取过期时间
	 * @param expireTimestr
	 * @return
	 */
	protected static Date getExpireTime(String expireTimestr)
	{
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
		try
		{
			return simpleDateFormat.parse(expireTimestr);
		}
		catch (Exception e)
		{
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_MONTH, 1);
			return now.getTime();
		}
	}
	/**
	 * 对commission赠送的接入请求的加密校验
	 * @param commissionPresentInfo
	 * @return
	 */
	protected int checkAccessData(CommissionPresentInfo commissionPresentInfo)
	{
		String key  = OrderPostUtil.getPresentSignInfo(commissionPresentInfo, this.transferAccessKey);
	    System.out.println(transferAccessKey);
	    System.out.println(key);
	    
		if(key.equalsIgnoreCase(commissionPresentInfo.getSignInfo()))
	    {
	    	return UserBalanceApplyConst.RESULT_SUCCESS;
	    }
	    return BalanceServiceConst.Error_signo_error;
	}
	/**
     * 完成外部请求信息转换为内部信息
     * @param commissionPresentInfo
     * @return
     */
	static public BalanceTransRunning getFromPresentInfo(CommissionPresentInfo commissionPresentInfo)
	{
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setUserid(commissionPresentInfo.getSubsId());
		//因为是赠送接口
		double amount = commissionPresentInfo.getAmt();
		balanceTransRunning.setAmount(amount);
		balanceTransRunning.setBalance(0d);
		//todo:
		try {
			if(commissionPresentInfo.getMsgInfo()!=null)
			{
				if(commissionPresentInfo.getMsgInfo().length()>64)
				{
					balanceTransRunning.setBizsource(commissionPresentInfo.getMsgInfo().substring(0,64));
					
				}
				else
				{
					balanceTransRunning.setBizsource(commissionPresentInfo.getMsgInfo());
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		balanceTransRunning.setBiztype(String.valueOf(commissionPresentInfo.getBizType()));
		//todo:
		Date expireTime = getExpireTime(commissionPresentInfo.getExpireTime());
		
		
		balanceTransRunning.setExpiretime(expireTime);
		balanceTransRunning.setOpertype(String.valueOf(commissionPresentInfo.getOperType()));
		try {
			if(commissionPresentInfo.getOrderID()!=null && commissionPresentInfo.getOrderID().length()>64)
			{
				balanceTransRunning.setOrderid(commissionPresentInfo.getOrderID().substring(0,64));
			}
			else
			{
				balanceTransRunning.setOrderid(commissionPresentInfo.getOrderID());
						
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//todo:
		//balanceTransRunning.setSrcipaddress(srcipaddress);
		balanceTransRunning.setStatus(BalanceServiceConst.Btrans_status_init);
		try {
			if(commissionPresentInfo.getReason()!=null && commissionPresentInfo.getReason().length()>128) {
				balanceTransRunning.setTransdesc(commissionPresentInfo.getReason().substring(0,128));
			}
			else
			{
				balanceTransRunning.setTransdesc(commissionPresentInfo.getReason());
					
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(commissionPresentInfo.getReqTransId().length()<=64)
		{
			balanceTransRunning.setTransid(commissionPresentInfo.getReqTransId());
		}
		else
		{
			return null;
		}
		balanceTransRunning.setTransactionTime(OrderPostUtil.getDateFromTransID(commissionPresentInfo.getReqTransId()));
		return balanceTransRunning;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.xinwei.orderpost.facade.CommissionPresentService#presentCommission(java.util.List)
	 */
	@Override
	public ProcessResult presentCommission(List<CommissionPresentInfo> commissionPresentInfoList) {
		// TODO Auto-generated method stub
		ProcessResult processResult = new ProcessResult();
		for(CommissionPresentInfo commissionPresentInfo:commissionPresentInfoList)
		{
			try {
				commissionPresentInfo.setResult(UserBalanceApplyConst.RESULT_FAILURE);
				int iRet = checkAccessData(commissionPresentInfo);
				if(iRet!=UserBalanceApplyConst.RESULT_SUCCESS)
				{
					commissionPresentInfo.setResult(iRet);
					continue;
				}
				System.out.println(commissionPresentInfo.toString());
				double amount = commissionPresentInfo.getAmt();
				//因为是赠送commission，amount大于零是加钱，小于零是扣钱;因此需要变换一下符号
				
				
				amount = -1 * amount;
				commissionPresentInfo.setAmt(amount);
			
				BalanceServiceContext balanceServiceContext = new BalanceServiceContext();
				
				commissionPresentInfo.setResult(-1);
				BalanceTransRunning bTransRunning= getFromPresentInfo(commissionPresentInfo);
				balanceServiceContext.setWillDoneBTransRunning(bTransRunning);
				//balanceServiceContext.setCommissionPresentInfo(commissionPresentInfo);
				
				iRet = this.balanceService.processBalance(balanceServiceContext, bTransRunning);
				commissionPresentInfo.setResult(iRet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		processResult.setResponseInfo(commissionPresentInfoList);
		return processResult;
	}
	

}
