/**
 * 
 */
package com.xinwei.commission.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.company.security.utils.SecurityUserAlgorithm;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.commission.service.BalanceCacheService;
import com.xinwei.commission.service.BalanceService;
import com.xinwei.commission.service.BalanceTransDbFeign;
import com.xinwei.commission.service.ServiceUserBlanceFeign;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.lotteryDb.controller.rest.UpdateBalRequest;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderpost.common.OrderPostUtil;

/**
 * @author helmsli
 *
 */
@Service("balanceService")
public class BalanceServiceImpl implements BalanceService {

	@Autowired
	private BalanceCacheService balanceCacheService;

	//所有交易的业务保留的天数
	protected final static long Trans_Expire_hours = 7;

	/**
	 * 引入hessian客户端调用后台服务
	 */
	//	@Autowired
	//	private ServiceUserBlance serviceUserBlanceFeign;

	@Autowired
	private ServiceUserBlanceFeign serviceUserBlanceFeign;

	//	@Resource(name="serviceBalanceTransDb")
	//	@Autowired
	//	private BalanceTransDb balanceTransDbFeign;

	@Autowired
	private BalanceTransDbFeign balanceTransDbFeign;

	/**
	 * 用户对内的传输加密
	 */
	@Value("${transfer.balanceKey}")
	private String transferBalKey;

	private Logger logger = LoggerFactory.getLogger(getClass());

	protected String InitDbTrans_const = "002000082212000033333333";
	/* (non-Javadoc)
	 * @see com.xinwei.orderpost.facade.CommissionPresentService#presentCommission(java.util.List)
	 */

	/**
	 * 对commission赠送的接入请求的加密校验
	 * @param commissionPresentInfo
	 * @return
	 
	protected int checkAccessData(CommissionPresentInfo commissionPresentInfo)
	{
		String key  = OrderPostUtil.getPresentSignInfo(commissionPresentInfo, this.transferAccessKey);
	    if(key.equalsIgnoreCase(commissionPresentInfo.getSignInfo()))
	    {
	    	return UserBalanceApplyConst.RESULT_SUCCESS;
	    }
	    return BalanceServiceConst.Error_signo_error;
	}
	
	
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
				this.pOneCommBalance(balanceServiceContext, commissionPresentInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		processResult.setResponseInfo(commissionPresentInfoList);
		return processResult;
	}
	*/
	/**
	 * 获取过期时间
	 * @param expireTimestr
	 * @return
	 */
	protected Date getExpireTime(String expireTimestr) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			return simpleDateFormat.parse(expireTimestr);
		} catch (Exception e) {
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_MONTH, 1);
			return now.getTime();
		}
	}

	@Override
	public int getBalance(BalanceServiceContext bServiceContext, long userid) {

		try {
			logger.debug(bServiceContext.toString());
			BalanceTransRunning bTransRunning = new BalanceTransRunning();
			bTransRunning.setUserid(userid);
			bServiceContext.setWillDoneBTransRunning(bTransRunning);
			UserBalance cacheUserBalance = balanceCacheService.getUserBalance(bTransRunning.getUserid());

			boolean isGetBalFromDb = false;
			//处理用户余额的数据
			if (cacheUserBalance == null) {
				logger.debug("cacheUserBalance is null");
				cacheUserBalance = getBalFromDb(bServiceContext);
				isGetBalFromDb = true;
			} else {
				logger.debug(cacheUserBalance.toString());
			}
			if (cacheUserBalance != null) {
				logger.debug(cacheUserBalance.toString());
				bServiceContext.setUserDbBalance(cacheUserBalance);
				return UserBalanceApplyConst.RESULT_SUCCESS;
			} else {
				logger.debug("cacheUserBalance is null");
			}
			return UserBalanceApplyConst.ERROR_BALANCE_UID_NOTEQUAL;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return UserBalanceApplyConst.RESULT_FAILURE;

	}

	/**
	 * 
	 * @param bServiceContext
	 * @param bTransRunning
	 * @return
	 */
	@Override
	public int processBalance(BalanceServiceContext bServiceContext, BalanceTransRunning bTransRunning) {

		/**
		 * 1.获取信息后，进行信息转换。将外部信息转换为内部信息
		 * 2.首先构造redis的key，在redis中查询该用户的transid是否已经发生，并且已经内部确认该笔交易成功
		 * 3.如果交易已经成功，返回交易成功时间和相关信息
		 * 4.如果交易没有发生，首先申请锁，如果申请锁成功，执行交易；
		 * 5.如果申请锁失败，进入排队队列，等待调度；
		 */

		boolean haveBeginTrans = false;
		int iRet = -1;

		if (bTransRunning.getAmount() == 0) {
			return -1;
		}

		bServiceContext.setWillDoneBTransRunning(bTransRunning);
		//从redis中获取历史数据，判断是否已经交易完成
		boolean bTransHasDone = bTransHasDone(bServiceContext, bTransRunning);
		//该业务已经被执行
		if (bTransHasDone) {
			return pOneCommBalanceDone(bServiceContext);
		}

		//申请优先级锁
		try {
			haveBeginTrans = balanceCacheService.beginTransToCache(bTransRunning,
					BalanceServiceConst.Btrans_Lock_Timeout);
			if (haveBeginTrans) {
				//1.从内存中获取当前余额
				UserBalance cacheUserBalance = balanceCacheService.getUserBalance(bTransRunning.getUserid());
				boolean isGetBalFromDb = false;
				//处理用户余额的数据
				if (cacheUserBalance == null) {
					cacheUserBalance = getBalFromDb(bServiceContext);
					isGetBalFromDb = true;
				}
				if (cacheUserBalance == null) {
					return BalanceServiceConst.Btrans_r_Balance_error;

				}
				//更新余额
				int result = updateBalDb(bServiceContext, cacheUserBalance);
				//如果更新余额结果需要重新查询余额,并且余额不是从数据库获取的
				if (isNeedQueryBalFromDb(result) && (!isGetBalFromDb)) {
					//获取最新的余额
					cacheUserBalance = getBalFromDb(bServiceContext);
					if (cacheUserBalance != null) {
						//重新更新数据库
						result = this.updateBalDb(bServiceContext, cacheUserBalance);
					}

					iRet = result;
				} else {
					iRet = result;
				}

			}
			//没有获取到锁
			else {
				iRet = BalanceServiceConst.Btrans_r_tooManyRequest;

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (iRet == UserBalanceApplyConst.RESULT_SUCCESS) {
				iRet = -1;
			}
		} finally {
			//如果开启事务，需要结束事务
			if (haveBeginTrans) {
				balanceCacheService.endTransToCache(bTransRunning);
			}
		}
		return iRet;
	}

	/**
	 * 形成对userBalanceApply的签名
	 * @param userBalanceApply
	 */
	protected void createCrcBalanceApply(UserBalanceApply userBalanceApply) {
		try {
			String key = this.transferBalKey;
			StringBuilder source = new StringBuilder();
			source.append(userBalanceApply.getUserId());
			source.append(SecurityUserAlgorithm.Prop_split);
			source.append(userBalanceApply.getTransaction());
			source.append(SecurityUserAlgorithm.Prop_split);
			source.append(userBalanceApply.getAmount());
			String checkCrc = SecurityUserAlgorithm.EncoderByMd5(key, source.toString());
			userBalanceApply.setBalanceext(checkCrc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	/**
	 * 根据最后交易号查询当前余额信息；
	 * @param bServiceContext
	 * @param oldTransRunning
	 * @return null--信息查询错误
	 */
	public UserBalance queryBalFromOldTrans(BalanceServiceContext bServiceContext,
			BalanceTransRunning oldTransRunning) {
		UserBalance initUserBalance = new UserBalance();
		initUserBalance.setUserId(oldTransRunning.getUserid());
		initUserBalance.setBalance(0d);
		initUserBalance.setExpiredata(oldTransRunning.getExpiretime());
		try {
			initUserBalance.setUpdatetime(oldTransRunning.getUpdatetime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			initUserBalance.setUpdatetime(Calendar.getInstance().getTime());
			e.printStackTrace();
		}
		initUserBalance.setTransaction(oldTransRunning.getTransid());
		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransaction(oldTransRunning.getTransid());
		userBalanceApply.setUserId(oldTransRunning.getUserid());
		userBalanceApply.setTransactionTime(oldTransRunning.getTransactionTime());
		userBalanceApply.setAmount(oldTransRunning.getAmount());
		//createCrcBalanceApply(userBalanceApply);
		UserBalanceApplyResult userBalanceApplyResult = null;
		try {
			userBalanceApplyResult = updateUserBalanceDb(initUserBalance, userBalanceApply);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//need to clear the redis balance;

			//balanceCacheService.delUserBalance(oldTransRunning.getUserid());

		}
		if (userBalanceApplyResult != null
				&& userBalanceApplyResult.getError() == UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE) {
			initUserBalance.setTransaction(userBalanceApplyResult.getTransaction());
			initUserBalance.setBalance(userBalanceApplyResult.getBalance());
			initUserBalance.setExpiredata(userBalanceApplyResult.getExpiredata());
			try {
				initUserBalance.setUpdatetime(userBalanceApplyResult.getUpdatetime());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return initUserBalance;
		}
		return null;
	}

	/**
	 * 用于对 BalanceTransRunning进行传输加密
	 * @param initDbTransaction
	 */
	protected void createCheckCrc(BalanceTransRunning initDbTransaction) {
		try {
			String key = this.transferBalKey;
			StringBuilder source = new StringBuilder();
			source.append(initDbTransaction.getUserid());
			source.append(SecurityUserAlgorithm.Prop_split);
			source.append(initDbTransaction.getTransid());
			source.append(SecurityUserAlgorithm.Prop_split);
			source.append(initDbTransaction.getAmount());
			String checkCrc = SecurityUserAlgorithm.EncoderByMd5(key, source.toString());
			initDbTransaction.setChecksum(checkCrc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	/**
	 * 初始化数据库为0
	 * @param bServiceContext
	 * @return null--失败
	 */
	protected UserBalance initUserDb(BalanceServiceContext bServiceContext) {

		//初始化新的ID
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		BalanceTransRunning initDbTransaction = new BalanceTransRunning();
		initDbTransaction.setTransid(InitDbTrans_const);
		initDbTransaction.setTransactionTime(OrderPostUtil.getDateFromTransID(InitDbTrans_const));
		initDbTransaction.setBalance(0d);
		initDbTransaction.setUpdatetime(now.getTime());
		now.set(2700, 01, 01, 23, 59, 59);
		initDbTransaction.setExpiretime(now.getTime());
		initDbTransaction.setUserid(bServiceContext.getWillDoneBTransRunning().getUserid());
		initDbTransaction.setStatus(BalanceServiceConst.Btrans_status_init);

		createCheckCrc(initDbTransaction);

		bServiceContext.setInitBTransRunning(initDbTransaction);
		List<BalanceTransRunning> querList = this.getBTransRunningFromDb(initDbTransaction);
		if (querList != null && querList.size() > 0) {
			initDbTransaction = querList.get(0);
		} else {
			try {
				this.balanceTransDbFeign.insertBalanceTransRunning(initDbTransaction);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		UserBalance initUserBalance = new UserBalance();
		initUserBalance.setUserIdExist();
		initUserBalance.setBalance(0d);
		initUserBalance.setExpiredata(initDbTransaction.getExpiretime());
		initUserBalance.setUpdatetime(initDbTransaction.getUpdatetime());
		initUserBalance.setTransaction(initDbTransaction.getTransid());

		bServiceContext.setInitUserDbBalance(initUserBalance);

		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransaction(initDbTransaction.getTransid());
		userBalanceApply.setUserId(initDbTransaction.getUserid());
		userBalanceApply.setTransactionTime(initDbTransaction.getTransactionTime());
		userBalanceApply.setAmount(0d);
		//createCrcBalanceApply(userBalanceApply);
		UserBalanceApplyResult userBalanceApplyResult = updateUserBalanceDb(initUserBalance, userBalanceApply);
		if (userBalanceApplyResult.getResult() == UserBalanceApplyConst.RESULT_SUCCESS_init) {
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
			createCheckCrc(initDbTransaction);
			this.balanceTransDbFeign.updateBalanceTransRunning(initDbTransaction);
			return initUserBalance;
		}
		return null;

	}

	/**
	 * 从用户余额信息中获取最终的交易号信息
	 * @param userBalanceApply
	 * @return
	 */
	protected UserBalanceApplyResult getBTransFromUserDb(UserBalanceApply userBalanceApply) {
		createCrcBalanceApply(userBalanceApply);
		UserBalanceApplyResult result = serviceUserBlanceFeign.queryTransaction(userBalanceApply);
		return result;
		//返回最后的交易记录
	}

	/**
	 * 从交易数据库中获取交易信息
	 * @param balTransRunning
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BalanceTransRunning> getBTransRunningFromDb(BalanceTransRunning balTransRunning) {
		createCheckCrc(balTransRunning);
		logger.debug(balTransRunning + "***");
		ProcessResult result = balanceTransDbFeign.selectBalanceTransRunning(balTransRunning);
		List<BalanceTransRunning> list = new ArrayList<>();
		if (result != null && result.getRetCode() == 0) {
			Gson gson = new Gson();
			list = gson.fromJson((String) result.getResponseInfo(), new TypeToken<List<BalanceTransRunning>>() {
				private static final long serialVersionUID = 1L;
			}.getType());
		}
		return list;
	}

	/**
	 * 从余额管理模块Lottery_commission_db中获取当前余额
	 * @param bServiceContext
	 * @return
	 */
	public UserBalance getBalFromDb(BalanceServiceContext bServiceContext) {
		//获取余额的
		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransaction(UserBalanceApplyConst.queryLastTransaction);
		userBalanceApply.setUserId(bServiceContext.getWillDoneBTransRunning().getUserid());
		userBalanceApply
				.setTransactionTime(OrderPostUtil.getDateFromTransID(UserBalanceApplyConst.queryLastTransaction));
		UserBalanceApplyResult userBalanceApplyResult = getBTransFromUserDb(userBalanceApply);
		logger.debug(userBalanceApplyResult.toString() + "***");
		//返回最后的交易记录
		if (userBalanceApplyResult.getError() == UserBalanceApplyConst.ERROR_TRANSACTION_LAST) {
			BalanceTransRunning queryBalTrans = new BalanceTransRunning();
			queryBalTrans.setUserid(bServiceContext.getWillDoneBTransRunning().getUserid());
			queryBalTrans.setTransid(userBalanceApplyResult.getTransaction());
			//从数据库查询已经交易过的记录
			queryBalTrans.setTransactionTime(OrderPostUtil.getDateFromTransID(userBalanceApplyResult.getTransaction()));
			logger.debug(queryBalTrans.toString() + "***1");
			List<BalanceTransRunning> balTransRunnings = getBTransRunningFromDb(queryBalTrans);
			logger.debug(JsonUtil.toJson(balTransRunnings) + "***2");
			if (balTransRunnings != null && balTransRunnings.size() > 0) {
				//查询当前余额
				UserBalance nowUserBalance = queryBalFromOldTrans(bServiceContext, balTransRunnings.get(0));
				return nowUserBalance;
			}
		}
		//返回UID不存在，创建新的用户余额，并返回
		else if (userBalanceApplyResult.getError() == UserBalanceApplyConst.ERROR_UID_NOTEXIST) {
			//发送初始化请求给db
			UserBalance nowUserBalance = initUserDb(bServiceContext);
			return nowUserBalance;
		} else {
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
	protected UserBalance updateBTransRunningDb(BalanceServiceContext bServiceContext,
			UserBalanceApplyResult userBalanceApplyResult) {
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
		if (UserBalanceApplyConst.RESULT_SUCCESS == userBalanceApplyResult.getResult()) {
			bServiceContext.getWillDoneBTransRunning().setStatus(BalanceServiceConst.Btrans_status_succ);

		} else {
			bServiceContext.getWillDoneBTransRunning().setStatus(userBalanceApplyResult.getError());
		}
		//bServiceContext.getWillDoneBTransRunning().setTransid(userBalanceApplyResult.);

		List<BalanceTransRunning> lists = getBTransRunningFromDb(bServiceContext.getWillDoneBTransRunning());
		if (lists != null && lists.size() > 0) {
			bServiceContext.getWillDoneBTransRunning().setUpdatetime(Calendar.getInstance().getTime());
			this.createCheckCrc(bServiceContext.getWillDoneBTransRunning());
			balanceTransDbFeign.updateBalanceTransRunning(bServiceContext.getWillDoneBTransRunning());
		} else {
			bServiceContext.getWillDoneBTransRunning().setUpdatetime(Calendar.getInstance().getTime());
			this.createCheckCrc(bServiceContext.getWillDoneBTransRunning());
			balanceTransDbFeign.insertBalanceTransRunning(bServiceContext.getWillDoneBTransRunning());
		}
		return userBalance;
	}

	/**
	 * 判断哪些错误码需要重新查询余额
	 * @param updateDbResult
	 * @return
	 */
	protected boolean isNeedQueryBalFromDb(long updateDbResult) {
		if (updateDbResult == UserBalanceApplyConst.ERROR_CHECKSUM_ERROR
				|| UserBalanceApplyConst.ERROR_BALANCE_UID_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_BALANCE_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_UPDATETIME_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_EXPIRETIME_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_TRANSACTION_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_CHECKSUM_NOTEQUAL == updateDbResult
				|| UserBalanceApplyConst.ERROR_UPDATEDB_ERROR == updateDbResult
				|| UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONENotLast == updateDbResult) {
			return true;
		}
		return false;
	}

	/**
	 * 更新交易事务明细数据库
	 * @param balanceTransRunning
	 */
	protected void insertBTransRunning(BalanceTransRunning balanceTransRunning) {
		this.createCheckCrc(balanceTransRunning);
		balanceTransDbFeign.insertBalanceTransRunning(balanceTransRunning);
	}

	/**
	 * 更新数据库余额
	 * @param nowUserbalance
	 * @param userBalanceApply
	 * @return
	 */
	protected UserBalanceApplyResult updateUserBalanceDb(UserBalance nowUserbalance,
			UserBalanceApply userBalanceApply) {
		try {
			createCrcBalanceApply(userBalanceApply);
			UpdateBalRequest updateBalRequest = new UpdateBalRequest();
			updateBalRequest.setNowUseBalance(nowUserbalance);
			updateBalRequest.setUserBalanceApply(userBalanceApply);
			return serviceUserBlanceFeign.updateUserBalance(updateBalRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			balanceCacheService.delUserBalance(userBalanceApply.getUserId());
		}
		return null;
	}

	/**
	 * 更新余额数据库信息和门面的数据库
	 * @param bServiceContext
	 * @param nowUserbalance
	 * @return
	 */
	public int updateBalDb(BalanceServiceContext bServiceContext, UserBalance nowUserbalance) {
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
		userBalanceApply.setTransactionTime(
				OrderPostUtil.getDateFromTransID(bServiceContext.getWillDoneBTransRunning().getTransid()));
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
		if (UserBalanceApplyConst.RESULT_SUCCESS == userBalanceApplyResult.getResult()) {
			//更新Transid数据库
			UserBalance userbalance = updateBTransRunningDb(bServiceContext, userBalanceApplyResult);
			//更新transid到内存
			balanceCacheService.setTransToCache(bServiceContext.getWillDoneBTransRunning(), Trans_Expire_hours);
			//更新余额到内存
			balanceCacheService.setUserBalance(bServiceContext.getWillDoneBTransRunning(), userbalance);
			bServiceContext.setUserDbBalance(userbalance);
			ret = userBalanceApplyResult.getResult();
		}

		//如果该交易上次已经成功执行
		else if (userBalanceApplyResult.getError() == UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE) {
			UserBalance userbalance = updateBTransRunningDb(bServiceContext, userBalanceApplyResult);
			bServiceContext.setUserDbBalance(userbalance);

		}
		//如果失败，但是不是最后的transid，需要重新查询最新的transid
		else {
			updateBTransRunningDb(bServiceContext, userBalanceApplyResult);
		}

		return ret;
	}

	/**
	 * 处理该事务已经被处理的返回流程；
	 * @param bServiceContext
	 * @return
	 */
	protected int pOneCommBalanceDone(BalanceServiceContext bServiceContext) {
		int iRet = -1;
		//如果金额不相同
		if (bServiceContext.getCacheBtransRunning().getAmount() != bServiceContext.getWillDoneBTransRunning()
				.getAmount()) {
			return BalanceServiceConst.Btrans_r_haveDone_error;

		}
		if (bServiceContext.getCacheBtransRunning().getUserid() != bServiceContext.getWillDoneBTransRunning()
				.getUserid()) {
			return BalanceServiceConst.Btrans_r_haveDone_error;

		}
		if (!bServiceContext.getCacheBtransRunning().getTransid()
				.equalsIgnoreCase(bServiceContext.getWillDoneBTransRunning().getTransid())) {
			return BalanceServiceConst.Btrans_r_haveDone_error;

		}
		return BalanceServiceConst.Btrans_r_haveDone;

	}

	/**
	 * 从redis中判断该业务是否已经被执行，并获取当前的状态
	 * @param balanceTransRunning
	 * @return
	 */
	protected boolean bTransHasDone(BalanceServiceContext bServiceContext, BalanceTransRunning balanceTransRunning) {
		BalanceTransRunning cacheBtransRunning = balanceCacheService.getTransFromCache(balanceTransRunning);
		bServiceContext.setCacheBtransRunning(cacheBtransRunning);
		if (cacheBtransRunning != null) {
			//todo:需要对cacheBtransRunning做关键信息加密校验
			boolean isDone = BalanceServiceConst.haveDone(cacheBtransRunning.getStatus());
			//
			if (!isDone) {
				//todo:判断内存中的是否已经过期，是否需要重做等
			} else {
				UserBalance userDbBalance = new UserBalance();
				userDbBalance.setBalance(cacheBtransRunning.getBalance());
				bServiceContext.setUserDbBalance(userDbBalance);
			}
			return isDone;
		}
		return false;
	}

}
