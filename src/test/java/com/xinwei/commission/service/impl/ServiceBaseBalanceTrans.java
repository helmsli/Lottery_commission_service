package com.xinwei.commission.service.impl;

import java.util.List;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commAccessDb.service.BalanceTransDb;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;

public class ServiceBaseBalanceTrans implements BalanceTransDb, ServiceUserBlance {

	@Override
	public UserBalanceApplyResult updateUserBalance(UserBalance nowUseBalance, UserBalanceApply userBalanceApply) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserBalanceApplyResult queryTransaction(UserBalanceApply userBalanceApply) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BalanceTransRunning> selectBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		return 0;
	}

}
