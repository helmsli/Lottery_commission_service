package com.xinwei.commission.domain;

import java.io.Serializable;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.orderpost.domain.CommissionPresentInfo;

public class BalanceServiceContext implements Serializable {
	//从内存中获取的transid
	private BalanceTransRunning cacheBtransRunning;
	//第三方发送的业务请求
	private CommissionPresentInfo commissionPresentInfo;
	//即将进行的业务信息
	private BalanceTransRunning willDoneBTransRunning;
	//余额数据库中的余额
	private UserBalance userDbBalance;
	
	
	//如果用户不存在，初始化余额时候的请求余额，主要给单元测试使用
	private UserBalance initUserDbBalance;
	//如果用户不存在，初始化余额时候的Transid，，主要给单元测试使用
	private BalanceTransRunning initBTransRunning;
	
	
	public BalanceTransRunning getCacheBtransRunning() {
		return cacheBtransRunning;
	}
	public void setCacheBtransRunning(BalanceTransRunning cacheBtransRunning) {
		this.cacheBtransRunning = cacheBtransRunning;
	}
	public CommissionPresentInfo getCommissionPresentInfo() {
		return commissionPresentInfo;
	}
	public void setCommissionPresentInfo(CommissionPresentInfo commissionPresentInfo) {
		this.commissionPresentInfo = commissionPresentInfo;
	}
	public BalanceTransRunning getWillDoneBTransRunning() {
		return willDoneBTransRunning;
	}
	public void setWillDoneBTransRunning(BalanceTransRunning willDoneBTransRunning) {
		this.willDoneBTransRunning = willDoneBTransRunning;
	}
	public UserBalance getUserDbBalance() {
		return userDbBalance;
	}
	public void setUserDbBalance(UserBalance userDbBalance) {
		this.userDbBalance = userDbBalance;
	}
	/**
	 * @return the initUserDbBalance
	 */
	public UserBalance getInitUserDbBalance() {
		return initUserDbBalance;
	}
	/**
	 * @param initUserDbBalance the initUserDbBalance to set
	 */
	public void setInitUserDbBalance(UserBalance initUserDbBalance) {
		this.initUserDbBalance = initUserDbBalance;
	}
	/**
	 * @return the initBTransRunning
	 */
	public BalanceTransRunning getInitBTransRunning() {
		return initBTransRunning;
	}
	/**
	 * @param initBTransRunning the initBTransRunning to set
	 */
	public void setInitBTransRunning(BalanceTransRunning initBTransRunning) {
		this.initBTransRunning = initBTransRunning;
	}
	
	
}
