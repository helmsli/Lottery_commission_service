package com.xinwei.commission.service.impl;

import java.util.Calendar;

import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;

public class ServiceUserBalanceMock implements ServiceUserBlance {

	protected ServiceUserBlance serviceUserBlance;
	
	
	/**
	 * @return the serviceUserBlance
	 */
	public ServiceUserBlance getServiceUserBlance() {
		return serviceUserBlance;
	}

	/**
	 * @param serviceUserBlance the serviceUserBlance to set
	 */
	public void setServiceUserBlance(ServiceUserBlance serviceUserBlance) {
		this.serviceUserBlance = serviceUserBlance;
	}

	@Override
	public UserBalanceApplyResult updateUserBalance(UserBalance nowUseBalance, UserBalanceApply userBalanceApply) {
		// TODO Auto-generated method stub
		if(serviceUserBlance!=null)
		{
			return serviceUserBlance.updateUserBalance(nowUseBalance,userBalanceApply);
		}
		return createDefaultApplyResult();
	}

	@Override
	public UserBalanceApplyResult queryTransaction(UserBalanceApply userBalanceApply) {
		// TODO Auto-generated method stub
		if(serviceUserBlance!=null)
		{
			return serviceUserBlance.queryTransaction(userBalanceApply);
		}
		return createDefaultApplyResult();
	}
	
	protected UserBalanceApplyResult createDefaultApplyResult()
	{
		UserBalanceApplyResult userBalanceApplyResult = new UserBalanceApplyResult();
		UserBalance userBalance = new UserBalance();
		userBalance.setAmount(123);
		userBalance.setBalance(1212);
		userBalance.setBalanceext("balanceext");
		userBalance.setExpiredata(Calendar.getInstance().getTime());
		userBalance.setOldBalanceext("oldbalancext");
		userBalance.setTelphonenum("1111");
		userBalance.setTransaction("002011122312131400000");
		userBalance.setUpdatetime(Calendar.getInstance().getTime());
		userBalance.setUserId(10000);
		userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_SUCCESS);
		userBalanceApplyResult.setBalance(userBalance.getBalance());
		userBalanceApplyResult.setTransaction("002017082317401223333");
		userBalanceApplyResult.setExpiredata(userBalance.getExpiredata());
		userBalanceApplyResult.setUserId(userBalance.getUserId());
		return userBalanceApplyResult;
	}

}
