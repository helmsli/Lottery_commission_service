/**
 * 
 */
package com.xinwei.commission.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commAccessDb.service.BalanceTransDb;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.commission.service.BalanceCacheService;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;
import com.xinwei.orderpost.facade.CommissionPresentService;

/**
 * @author helmsli
 *
 */
@Service("balanceService")
public class BalanceServiceImpl implements CommissionPresentService {

	@Autowired 
	private BalanceCacheService balanceCacheService;
	
	
	//所有交易的业务保留的天数
	protected final static long Trans_Expire_hours = 7; 
		
	/**
	 * 引入hessian客户端调用后台服务
	 */
	@Resource(name="serviceUserBlance")
	private ServiceUserBlance serviceUserBlance;
	
	@Resource(name="serviceBalanceTransDb")
	private BalanceTransDb balanceTransDb;
	
	protected String InitDbTrans_const="002000082212000033333333";
	/* (non-Javadoc)
	 * @see com.xinwei.orderpost.facade.CommissionPresentService#presentCommission(java.util.List)
	 */
	
	
	
	
	@Override
	public ProcessResult presentCommission(List<CommissionPresentInfo> commissionPresentInfoList) {
		// TODO Auto-generated method stub
		ProcessResult processResult = new ProcessResult();
		for(CommissionPresentInfo commissionPresentInfo:commissionPresentInfoList)
		{
			try {
				System.out.println(commissionPresentInfo.toString());
				double amount = commissionPresentInfo.getAmt();
				//因为是赠送commission，amount大于零是加钱，小于零是扣钱;因此需要变换一下符号
				
				
				amount = -1 * amount;
				commissionPresentInfo.setAmt(amount);
			
				BalanceServiceContext balanceServiceContext = new BalanceServiceContext();
				this.pOneCommBalance(balanceServiceContext, commissionPresentInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		processResult.setResponseInfo(commissionPresentInfoList);
		return processResult;
	}
	
	/**
	 * 获取过期时间
	 * @param expireTimestr
	 * @return
	 */
	protected Date getExpireTime(String expireTimestr)
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
	 * @return the initDbTrans_const
	 */
	public String getInitDbTrans_const() {
		return InitDbTrans_const;
	}

	/**
	 * @param initDbTrans_const the initDbTrans_const to set
	 */
	public void setInitDbTrans_const(String initDbTrans_const) {
		InitDbTrans_const = initDbTrans_const;
	}

	/**
	 * 
	 * @param commissionPresentInfo
	 * @return
	 */
	protected CommissionPresentInfo pOneCommBalance(BalanceServiceContext bServiceContext,CommissionPresentInfo commissionPresentInfo)
	{
		/**
		 * 1.获取信息后，进行信息转换。将外部信息转换为内部信息
		 * 2.首先构造redis的key，在redis中查询该用户的transid是否已经发生，并且已经内部确认该笔交易成功
		 * 3.如果交易已经成功，返回交易成功时间和相关信息
		 * 4.如果交易没有发生，首先申请锁，如果申请锁成功，执行交易；
		 * 5.如果申请锁失败，进入排队队列，等待调度；
		 */
		
		boolean haveBeginTrans = false;
		commissionPresentInfo.setResult(-1);
		BalanceTransRunning bTransRunning= getFromPresentInfo(commissionPresentInfo);
		bServiceContext.setWillDoneBTransRunning(bTransRunning);
		bServiceContext.setCommissionPresentInfo(commissionPresentInfo);
		//从redis中获取历史数据，判断是否已经交易完成
		boolean bTransHasDone = bTransHasDone(bServiceContext,bTransRunning);
		//该业务已经被执行
		if(bTransHasDone)
		{
			return pOneCommBalanceDone(commissionPresentInfo,bServiceContext);
		}
		//申请优先级锁
		try {
			haveBeginTrans = balanceCacheService.beginTransToCache(bTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
			if(haveBeginTrans)
			{
				//1.从内存中获取当前余额
				UserBalance cacheUserBalance = balanceCacheService.getUserBalance(bTransRunning.getUserid());
				boolean isGetBalFromDb = false;
				//处理用户余额的数据
				if(cacheUserBalance==null)
				{
					cacheUserBalance = getBalFromDb(bServiceContext);
					isGetBalFromDb = true;
				}
				if(cacheUserBalance==null)
				{
					commissionPresentInfo.setResult(BalanceServiceConst.Btrans_r_Balance_error);
					return commissionPresentInfo;
				}
				//更新余额
				int result = updateBalDb(bServiceContext, cacheUserBalance);
				//如果更新余额结果需要重新查询余额,并且余额不是从数据库获取的
				if(isNeedQueryBalFromDb(result)&&(!isGetBalFromDb))
				{
					//获取最新的余额
					cacheUserBalance = getBalFromDb(bServiceContext);
					if(cacheUserBalance!=null)
					{
						//重新更新数据库
						result = this.updateBalDb(bServiceContext, cacheUserBalance);
					}
					
					commissionPresentInfo.setResult(result);
				}
				else
				{
					commissionPresentInfo.setResult(result);
				}
				
				
			}
			//没有获取到锁
			else
			{
				commissionPresentInfo.setResult(BalanceServiceConst.Btrans_r_tooManyRequest);
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			//如果开启事务，需要结束事务
			if(haveBeginTrans)
			{
				balanceCacheService.endTransToCache(bTransRunning);
			}
		}
		return commissionPresentInfo;
	}
	
	/**
	 * 根据最后交易号查询当前余额信息；
	 * @param bServiceContext
	 * @param oldTransRunning
	 * @return null--信息查询错误
	 */
	protected UserBalance queryBalFromOldTrans(BalanceServiceContext bServiceContext,BalanceTransRunning oldTransRunning)
	{
		UserBalance initUserBalance = new UserBalance();
		initUserBalance.setUserId(oldTransRunning.getUserid());
		initUserBalance.setBalance(0d);
		initUserBalance.setExpiredata(oldTransRunning.getExpiretime());
		initUserBalance.setUpdatetime(oldTransRunning.getUpdatetime());
		initUserBalance.setTransaction(oldTransRunning.getTransid());
		UserBalanceApply userBalanceApply=new UserBalanceApply();
		userBalanceApply.setTransaction(oldTransRunning.getTransid());
		userBalanceApply.setUserId(oldTransRunning.getUserid());
		userBalanceApply.setTransactionTime(oldTransRunning.gettransactionTime());
		userBalanceApply.setAmount(oldTransRunning.getAmount());		
		UserBalanceApplyResult userBalanceApplyResult = this.serviceUserBlance.updateUserBalance(initUserBalance, userBalanceApply);
		if(userBalanceApplyResult.getError()==UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE)
	    {
			initUserBalance.setTransaction(userBalanceApplyResult.getTransaction());	
			initUserBalance.setBalance(userBalanceApplyResult.getBalance());
			initUserBalance.setExpiredata(userBalanceApplyResult.getExpiredata());
			initUserBalance.setUpdatetime(userBalanceApplyResult.getUpdatetime());
	    	return initUserBalance;
	    }
	    return null;
	}
	
	
	/**
	 * 初始化数据库为0
	 * @param bServiceContext
	 * @return null--失败
	 */
	protected UserBalance initUserDb(BalanceServiceContext bServiceContext)
	{
		
		//初始化新的ID
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);		
		BalanceTransRunning initDbTransaction = new BalanceTransRunning();
		initDbTransaction.setTransid(InitDbTrans_const);		
		initDbTransaction.setTransactionTime(OrderPostUtil.getDateFromTransID(InitDbTrans_const));
		initDbTransaction.setBalance(0d);
		initDbTransaction.setUpdatetime(now.getTime());
		now.set(2700, 01, 01, 23, 59,59);
		initDbTransaction.setExpiretime(now.getTime());
		initDbTransaction.setUserid(bServiceContext.getWillDoneBTransRunning().getUserid());
		initDbTransaction.setStatus(BalanceServiceConst.Btrans_status_init);
		bServiceContext.setInitBTransRunning(initDbTransaction);
		List<BalanceTransRunning> querList = this.getBTransRunningFromDb(initDbTransaction);
		if(querList!=null && querList.size()>0)
		{
			initDbTransaction=querList.get(0);
		}
		else
		{
			this.balanceTransDb.insertBalanceTransRunning(initDbTransaction);
				
		}
		UserBalance initUserBalance = new UserBalance();
		initUserBalance.setUserIdExist();
		initUserBalance.setBalance(0d);
		initUserBalance.setExpiredata(initDbTransaction.getExpiretime());
		initUserBalance.setUpdatetime(initDbTransaction.getUpdatetime());
		initUserBalance.setTransaction(initDbTransaction.getTransid());
		
		bServiceContext.setInitUserDbBalance(initUserBalance);		
		
		UserBalanceApply userBalanceApply=new UserBalanceApply();
		userBalanceApply.setTransaction(initDbTransaction.getTransid());
		userBalanceApply.setUserId(initDbTransaction.getUserid());
		userBalanceApply.setTransactionTime(initDbTransaction.gettransactionTime());
		userBalanceApply.setAmount(0d);
		UserBalanceApplyResult userBalanceApplyResult = this.serviceUserBlance.updateUserBalance(initUserBalance, userBalanceApply);
	    if(userBalanceApplyResult.getResult()==UserBalanceApplyConst.RESULT_SUCCESS_init)
	    {
	    	initDbTransaction.setTransid(userBalanceApplyResult.getTransaction());	
	    	initDbTransaction.setBalance(userBalanceApplyResult.getBalance());
	    	initDbTransaction.setExpiretime(userBalanceApplyResult.getExpiredata());
	    	initDbTransaction.setUpdatetime(userBalanceApplyResult.getUpdatetime());
	    	initDbTransaction.setUserid(userBalanceApplyResult.getUserId());
	    	initUserBalance.setUserId(userBalanceApplyResult.getUserId());
	    	initUserBalance.setBalance(userBalanceApplyResult.getBalance());
	    	initUserBalance.setExpiredata(userBalanceApplyResult.getExpiredata());
	    	initUserBalance.setTransaction(userBalanceApplyResult.getTransaction());
	    	initUserBalance.setUpdatetime(userBalanceApplyResult.getUpdatetime());
	    	this.balanceTransDb.updateBalanceTransRunning(initDbTransaction);
	    	return initUserBalance;
	    }
	    return null;
	    
	}
	
	/**
	 * 从用户余额信息中获取最终的交易号信息
	 * @param userBalanceApply
	 * @return
	 */
	protected UserBalanceApplyResult getBTransFromUserDb(UserBalanceApply userBalanceApply)
	{
		return serviceUserBlance.queryTransaction(userBalanceApply);
		//返回最后的交易记录
	}
	
	/**
	 * 从交易数据库中获取交易信息
	 * @param balTransRunning
	 * @return
	 */
	protected 	List<BalanceTransRunning> getBTransRunningFromDb(BalanceTransRunning balTransRunning)
	{
		return balanceTransDb.selectBalanceTransRunning(balTransRunning);		
	}
	/**
	 * 从余额管理模块Lottery_commission_db中获取当前余额
	 * @param bServiceContext
	 * @return
	 */
	protected UserBalance getBalFromDb(BalanceServiceContext bServiceContext)
	{
		//获取余额的
		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransaction(UserBalanceApplyConst.queryLastTransaction);
		userBalanceApply.setUserId(bServiceContext.getWillDoneBTransRunning().getUserid());
		userBalanceApply.setTransactionTime(OrderPostUtil.getDateFromTransID(UserBalanceApplyConst.queryLastTransaction));
		UserBalanceApplyResult userBalanceApplyResult = getBTransFromUserDb(userBalanceApply);
		//返回最后的交易记录
		if(userBalanceApplyResult.getError()==UserBalanceApplyConst.ERROR_TRANSACTION_LAST)
		{
			BalanceTransRunning queryBalTrans =  new BalanceTransRunning();
			queryBalTrans.setUserid(bServiceContext.getWillDoneBTransRunning().getUserid());
			queryBalTrans.setTransid(userBalanceApplyResult.getTransaction());
			//从数据库查询已经交易过的记录
			queryBalTrans.setTransactionTime(OrderPostUtil.getDateFromTransID(userBalanceApplyResult.getTransaction()));
			List<BalanceTransRunning> balTransRunnings = getBTransRunningFromDb(queryBalTrans);
			if(balTransRunnings!=null&&balTransRunnings.size()>0)
			{
				//查询当前余额
				UserBalance nowUserBalance =queryBalFromOldTrans(bServiceContext,balTransRunnings.get(0));
			    return nowUserBalance;
			}
		}
		//返回UID不存在，创建新的用户余额，并返回
		else if(userBalanceApplyResult.getError()==UserBalanceApplyConst.ERROR_UID_NOTEXIST)
		{
			//发送初始化请求给db
			UserBalance nowUserBalance = initUserDb(bServiceContext);
			return nowUserBalance;
		}
		else
		{
			//todo:其余错误
		}
	   return null;	
	}
	
	/**
	 * 更新门面的数据库记录
	 * @param bServiceContext
	 * @param userBalanceApplyResult
	 * @return
	 */
	protected UserBalance updateBTransRunningDb(BalanceServiceContext bServiceContext,UserBalanceApplyResult userBalanceApplyResult)
	{
		UserBalance userBalance = new UserBalance();
		userBalance.setBalance(userBalanceApplyResult.getBalance());
		userBalance.setUserId(bServiceContext.getWillDoneBTransRunning().getUserid());
		try {
			userBalance.setExpiredata(userBalanceApplyResult.getExpiredata());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			userBalance.setUpdatetime(userBalanceApplyResult.getUpdatetime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Calendar now = Calendar.getInstance();
			userBalance.setUpdatetime(now.getTime());
			
		}
		userBalance.setTransaction(userBalanceApplyResult.getTransaction());
		
		bServiceContext.getWillDoneBTransRunning().setBalance(userBalanceApplyResult.getBalance());
		//todo:if the balance have success done ,set status ok
		if(UserBalanceApplyConst.RESULT_SUCCESS==userBalanceApplyResult.getResult())
		{
			bServiceContext.getWillDoneBTransRunning().setStatus(BalanceServiceConst.Btrans_status_succ);
				
		}
		else
		{
		 bServiceContext.getWillDoneBTransRunning().setStatus(userBalanceApplyResult.getError());
		}
		//bServiceContext.getWillDoneBTransRunning().setTransid(userBalanceApplyResult.);
		
		List<BalanceTransRunning> lists= getBTransRunningFromDb(bServiceContext.getWillDoneBTransRunning());
		if(lists!=null && lists.size()>0)
		{
			bServiceContext.getWillDoneBTransRunning().setUpdatetime(Calendar.getInstance().getTime());
			balanceTransDb.updateBalanceTransRunning(bServiceContext.getWillDoneBTransRunning());
		}
		else
		{
			bServiceContext.getWillDoneBTransRunning().setUpdatetime(Calendar.getInstance().getTime());
			
			balanceTransDb.insertBalanceTransRunning(bServiceContext.getWillDoneBTransRunning());
		}
		return userBalance;
	}
	
	/**
	 * 判断哪些错误码需要重新查询余额
	 * @param updateDbResult
	 * @return
	 */
	protected boolean isNeedQueryBalFromDb(long updateDbResult)
	{
		if(updateDbResult==UserBalanceApplyConst.ERROR_CHECKSUM_ERROR||
		  UserBalanceApplyConst.ERROR_BALANCE_UID_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_BALANCE_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_UPDATETIME_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_EXPIRETIME_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_TRANSACTION_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_CHECKSUM_NOTEQUAL==updateDbResult||
		  UserBalanceApplyConst.ERROR_UPDATEDB_ERROR==updateDbResult||
		  UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONENotLast==updateDbResult)
		{
			return true;
		}
		return false;
	}
	/**
	 * 更新交易事务明细数据库
	 * @param balanceTransRunning
	 */
	protected void insertBTransRunning(BalanceTransRunning balanceTransRunning)
	{
		 balanceTransDb.insertBalanceTransRunning(balanceTransRunning);
	}
	
	/**
	 * 更新数据库余额
	 * @param nowUserbalance
	 * @param userBalanceApply
	 * @return
	 */
	protected UserBalanceApplyResult updateUserBalanceDb(UserBalance nowUserbalance,UserBalanceApply userBalanceApply)
	{
	 return serviceUserBlance.updateUserBalance(nowUserbalance, userBalanceApply);
	}
	
	/**
	 * 更新余额数据库信息和门面的数据库
	 * @param bServiceContext
	 * @param nowUserbalance
	 * @return
	 */
	protected int updateBalDb(BalanceServiceContext bServiceContext,UserBalance nowUserbalance)
	{
		//插入该记录；
		int ret = -1;
		try {
			insertBTransRunning(bServiceContext.getWillDoneBTransRunning());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//todo:主键冲突错误往后走，否则返回；
		}
		
		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransactionTime(OrderPostUtil.getDateFromTransID(bServiceContext.getWillDoneBTransRunning().getTransid()));
		userBalanceApply.setTransaction(bServiceContext.getWillDoneBTransRunning().getTransid());
		userBalanceApply.setUserId(bServiceContext.getWillDoneBTransRunning().getUserid());
		userBalanceApply.setAmount(bServiceContext.getWillDoneBTransRunning().getAmount());
		userBalanceApply.setBizType(bServiceContext.getWillDoneBTransRunning().getBiztype());
		userBalanceApply.setExpireDays(0);
		userBalanceApply.setOperType(bServiceContext.getWillDoneBTransRunning().getOpertype());
		userBalanceApply.setOrderId(bServiceContext.getWillDoneBTransRunning().getOrderid());
		userBalanceApply.setRemark(bServiceContext.getWillDoneBTransRunning().getTransdesc());
		userBalanceApply.setUpdatetime(bServiceContext.getWillDoneBTransRunning().getUpdatetime());
		UserBalanceApplyResult userBalanceApplyResult = updateUserBalanceDb(nowUserbalance, userBalanceApply);
		//该业务已经被成功执行
		ret = userBalanceApplyResult.getError();
		if(UserBalanceApplyConst.RESULT_SUCCESS==userBalanceApplyResult.getResult())
		{
			//更新Transid数据库
			UserBalance userbalance = updateBTransRunningDb(bServiceContext,userBalanceApplyResult);
			//更新transid到内存
			balanceCacheService.setTransToCache(bServiceContext.getWillDoneBTransRunning(),Trans_Expire_hours);
			//更新余额到内存
			balanceCacheService.setUserBalance(bServiceContext.getWillDoneBTransRunning(), userbalance);
			bServiceContext.setUserDbBalance(userbalance);
			ret= userBalanceApplyResult.getResult();
		}
		
		//如果该交易上次已经成功执行
		else if(userBalanceApplyResult.getError()==UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE)
		{			
			UserBalance userbalance = updateBTransRunningDb(bServiceContext,userBalanceApplyResult);
			bServiceContext.setUserDbBalance(userbalance);
		
		}
		//如果失败，但是不是最后的transid，需要重新查询最新的transid
		else
		{
			updateBTransRunningDb(bServiceContext,userBalanceApplyResult);			
		}
		
	    return ret;
	}
	
	/**
	 * 处理该事务已经被处理的返回流程；
	 * @param bServiceContext
	 * @return
	 */
	protected CommissionPresentInfo pOneCommBalanceDone(CommissionPresentInfo commissionPresentInfo,BalanceServiceContext bServiceContext)
	{
		CommissionPresentInfo retResult = commissionPresentInfo;
		//如果金额不相同
		if(bServiceContext.getCacheBtransRunning().getAmount() != bServiceContext.getWillDoneBTransRunning().getAmount())
		{
			retResult.setResult(BalanceServiceConst.Btrans_r_haveDone_error);
			return retResult;
		}
		if(bServiceContext.getCacheBtransRunning().getUserid() != bServiceContext.getWillDoneBTransRunning().getUserid())
		{
			retResult.setResult(BalanceServiceConst.Btrans_r_haveDone_error);
			return retResult;
		}
		if(!bServiceContext.getCacheBtransRunning().getTransid().equalsIgnoreCase(bServiceContext.getWillDoneBTransRunning().getTransid()))
		{
			retResult.setResult(BalanceServiceConst.Btrans_r_haveDone_error);
			return retResult;
		}
		retResult.setResult(BalanceServiceConst.Btrans_r_haveDone);
		return retResult;
		
	}
	
	
	
	/**
	 * 从redis中判断该业务是否已经被执行，并获取当前的状态
	 * @param balanceTransRunning
	 * @return
	 */
	protected boolean bTransHasDone(BalanceServiceContext bServiceContext, BalanceTransRunning balanceTransRunning)
	{
		BalanceTransRunning cacheBtransRunning =balanceCacheService.getTransFromCache(balanceTransRunning);
		bServiceContext.setCacheBtransRunning(cacheBtransRunning);
		if(cacheBtransRunning!=null)
		{
			//todo:需要对cacheBtransRunning做关键信息加密校验
			boolean isDone = BalanceServiceConst.haveDone(cacheBtransRunning.getStatus());
			//
			if(!isDone)
			{
				//todo:判断内存中的是否已经过期，是否需要重做等
			}
			
			
			return isDone;
		}
		return false;
	}
	
    /**
     * 完成外部请求信息转换为内部信息
     * @param commissionPresentInfo
     * @return
     */
	public BalanceTransRunning getFromPresentInfo(CommissionPresentInfo commissionPresentInfo)
	{
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setUserid(commissionPresentInfo.getSubsId());
		//因为是赠送接口
		double amount = commissionPresentInfo.getAmt();
		balanceTransRunning.setAmount(amount);
		balanceTransRunning.setBalance(0d);
		//todo:
		balanceTransRunning.setBizsource(commissionPresentInfo.getSignInfo());
		balanceTransRunning.setBiztype(String.valueOf(commissionPresentInfo.getBizType()));
		//todo:
		Date expireTime = getExpireTime(commissionPresentInfo.getExpireTime());
		
		
		balanceTransRunning.setExpiretime(expireTime);
		balanceTransRunning.setOpertype(String.valueOf(commissionPresentInfo.getOperType()));
		balanceTransRunning.setOrderid(commissionPresentInfo.getOrderID());
		//todo:
		//balanceTransRunning.setSrcipaddress(srcipaddress);
		balanceTransRunning.setStatus(BalanceServiceConst.Btrans_status_init);
		balanceTransRunning.setTransdesc(commissionPresentInfo.getReason());
		balanceTransRunning.setTransid(commissionPresentInfo.getReqTransId());
		
		balanceTransRunning.setTransactionTime(OrderPostUtil.getDateFromTransID(commissionPresentInfo.getReqTransId()));
		return balanceTransRunning;
	}
}
