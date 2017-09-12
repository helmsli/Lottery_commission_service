package com.xinwei.commission.service.impl;

import java.util.Date;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;

public class BalanceCacheKeyServiceImpl {
	/**
     * 用户交易事务号信息的key前缀
     */
	protected final static String Key_prefix_transId = "uTrans:";
	/**
	 * 更新事务信息后的加锁key
	 */
	protected final static String Key_prefix_user_balance = "uBal:";
	
	/**
	 * 用户全局锁的key前缀
	 */
	protected final static String Key_prefix_user_Lock = "uLock:";
	//高优先级队列个数
	protected final static String Key_prefix_user_b_high = "uProc_h:";
	//高优先级队列个数
	protected final static String Key_prefix_user_b_htime = "uProc_hd:";

	/**
	 * 创建业务交易的key
	 * @param userid
	 * @param transactionTime
	 * @param transid
	 * @return
	 */
	public String buildTransidKey(long userid, Date transactionTime, String transid)
	{
		StringBuilder strKey = new StringBuilder();
		strKey.append(Key_prefix_transId);
		strKey.append("*");
		strKey.append(userid);
		strKey.append("*");
		strKey.append(transactionTime.toString());
		strKey.append("*");
		strKey.append(transid);		
		return strKey.toString();		
	}


	/**
	 * 创建业务交易的key
	 * @param balanceTransRunning
	 * @return
	 */
	public String buildTransidKey(BalanceTransRunning balanceTransRunning)
	{
		
		long userid = balanceTransRunning.getUserid();
		Date transactionTime = balanceTransRunning.getTransactionTime();
		String transid = balanceTransRunning.getTransid();
		return buildTransidKey(userid,transactionTime,transid);	
	}
	
	/**
	 * 获取更新锁的KEY
	 * @param userid
	 * @param transactionTime
	 * @param transid
	 * @return
	 */
	public String buildUserLockKey(long userid)
	{
		
		StringBuilder strKey = new StringBuilder();
		strKey.append(this.Key_prefix_user_Lock);
		strKey.append("*");		
		strKey.append(userid);				
		/*strKey.append("*");
		strKey.append(transactionTime.toString());
		strKey.append("*");
		strKey.append(transid);
		*/		
		return strKey.toString();		
	}
	
	/**
	 * 用户余额的KEY
	 * @param userid
	 * @return
	 */
	public String buildUserBalKey(long userid)
	{
		
		StringBuilder strKey = new StringBuilder();
		strKey.append(this.Key_prefix_user_balance);
		strKey.append("*");		
		strKey.append(userid);				
		/*strKey.append("*");
		strKey.append(transactionTime.toString());
		strKey.append("*");
		strKey.append(transid);
		*/		
		return strKey.toString();		
	}
	/**
	 * 获取更新锁key
	 * @param balanceTransRunning
	 * @return
	 
	protected String buildUserLockKey(BalanceTransRunning balanceTransRunning)
	{
		
		long userid = balanceTransRunning.getUserid();
	//	Date transactionTime = balanceTransRunning.gettransactionTime();
	//	String transid = balanceTransRunning.getTransid();
		return buildUserLockKey(userid);		
	}
	*/
	
	/**
	 * 构建高优先级队列个数
	 * @param balanceTransRunning
	 * @return
	 */
	public String buildUserHighLockKey(BalanceTransRunning balanceTransRunning)
	{
		
		StringBuilder strKey = new StringBuilder();
		strKey.append(this.Key_prefix_user_b_high);
		strKey.append("*");		
		strKey.append(balanceTransRunning.getUserid());				
		/*strKey.append("*");
		strKey.append(transactionTime.toString());
		strKey.append("*");
		strKey.append(transid);
		*/		
		return strKey.toString();				
	}
	/**
	 * 构建高优先级队列过期时间
	 * @param balanceTransRunning
	 * @return
	 */
	public String buildUserHighExpireLockKey(BalanceTransRunning balanceTransRunning)
	{
		
		StringBuilder strKey = new StringBuilder();
		strKey.append(this.Key_prefix_user_b_htime);
		strKey.append("*");		
		strKey.append(balanceTransRunning.getUserid());				
		/*strKey.append("*");
		strKey.append(transactionTime.toString());
		strKey.append("*");
		strKey.append(transid);
		*/		
		return strKey.toString();				
	}
	
	/**
	 * 构建lock的value
	 * @param balanceTransRunning
	 * @return
	 */
	public String buildUserLockTransValue(BalanceTransRunning balanceTransRunning)
	{
		
		String transidKey = buildTransidKey(balanceTransRunning);
		return transidKey;		
	}
	/**
	 * 构建用户全局锁的value
	 * @param userid
	 * @param transactionTime
	 * @param transid
	 * @return
	
	protected String buildUserLockTransValue(long userid, Date transactionTime, String transid)
	{
		
		String transidKey = buildTransidKey(userid,transactionTime,transid);
		return transidKey;		
	}
 */
}
