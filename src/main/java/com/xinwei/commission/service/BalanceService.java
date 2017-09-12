package com.xinwei.commission.service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.orderpost.domain.CommissionPresentInfo;

public interface BalanceService {
	/**
	 * 处理余额的变更
	 * @param bServiceContext
	 * @param bTransRunning
	 * @return
	 */
	public int processBalance(BalanceServiceContext bServiceContext,BalanceTransRunning  bTransRunning);
	/**
	 * 处理查询余额
	 * @param bServiceContext
	 * @param userid
	 * @return
	 */
	public int getBalance(BalanceServiceContext bServiceContext,long userid);
	
}
