package com.xinwei.commission.service;

import java.util.Date;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.lotteryDb.domain.UserBalance;

public interface BalanceCacheService {
	/**
	 * 根据用户ID，交易时间和交易事务号，获取交易信息
	 * @param userid
	 * @param transactionTime
	 * @param transid
	 * @return -- 如果不存在，返回null，如果有数据返回正确的信息
	 */
	public BalanceTransRunning getTransFromCache(BalanceTransRunning balanceTransRunning);

	/**
	 * 设置最新的用户交易信息
	 * @param balanceTransRunning
	 * @param expireHours
	 */
	public boolean setTransToCache(BalanceTransRunning balanceTransRunning,long expireHours);
	
	/**
	 * 删除缓存中的事务
	 * @param balanceTransRunning
	 * @return
	 */
	public boolean delTransFromCache(BalanceTransRunning balanceTransRunning);

	/**
	 * 申请需要变更数据的锁,开始事务
	 * @param balanceTransRunning
	 * @param durations  -- 秒，加锁多少秒
	 * @return
	 */
	public boolean beginTransToCache(BalanceTransRunning balanceTransRunning,long durations);
	
	/**
	 * 释放需要变更数据的锁,结束事务,仅仅是释放锁，不会更新内存中的balanceTransRunning数据
	 * @param balanceTransRunning
	 * @param key
	 * @return
	 */
	public int endTransToCache(BalanceTransRunning balanceTransRunning);
	
	
	/**
	 * 获取当前用户的余额信息
	 * @param balanceTransRunning
	 */
	public UserBalance getUserBalance(long userId);
	
	/**
	 * 获取当前用户的余额信息
	 * @param balanceTransRunning
	 */
	public boolean setUserBalance(BalanceTransRunning balanceTransRunning,UserBalance userBalance);
	
	/**
	 * 删除缓存中的余额
	 * @param balanceTransRunning
	 * @param userBalance
	 * @return
	 */
	public boolean delUserBalance(long userId);
	
}
