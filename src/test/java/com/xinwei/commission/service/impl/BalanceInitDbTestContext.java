package com.xinwei.commission.service.impl;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;

public class BalanceInitDbTestContext implements ServiceUserBlance{
	
	/**
	 * 测试用例的用例类型常量
	 */
	public static final int TestCaseType_initdb = 0;
    
	//当前用例的类型
	protected int testCaseType =TestCaseType_initdb; 
		
	
	/**
	 * 测试用例中接口的流程控制变量
	 */
	//查询余额的步骤
	public static final int Tc_step_lastBTrans = 0;
	/**
	 * 记录当前查询的用例流程
	 */
	protected int testCaseQuerySteps =Tc_step_lastBTrans;
	
	/**
	 * 创建数据库
	 */
	public static final int Tc_step_update_createBDb = 0;
	/**
	 * 余额增加100
	 */
	public static final int Tc_step_Update_balance_Add = 1;
	
	/**
	 * 余额较少20
	 */
	public static final int Tc_step_Update_balance_reduce = 2;
	
	/**
	 * 
	 */
	public static final int Tc_step_Update_balance_Trans_haveDone = 3;
	/**
	 *已经执行成功，但不是最后一个transid
	 */
	public static final int Tc_step_Update_balance_Trans_notLastTrans = 4;
	/**
	 *  更新余额的流程 
	 */
	protected int testCaseUpdateSteps =Tc_step_update_createBDb;
	
	//业务的上下文信息；
	BalanceServiceContext bServiceContext = null;
	
	protected UserBalance gUserBalance = null;
	
        
	/**
	 * 构造创建数据库的用例
	 * @param commissionPresentInfo
	
	public void initEnvForInitDb(CommissionPresentInfo commissionPresentInfo)
	{
		//初始化查询余额的用例对象
		//查询最后的transId，返回用户数据不存在；
		UserBalanceApply userBalanceApply = new UserBalanceApply();
		userBalanceApply.setTransaction(UserBalanceApplyConst.queryLastTransaction);
		userBalanceApply.setUserId(commissionPresentInfo.getSubsId());
		this.wantedQueryApplys.add(userBalanceApply);
		UserBalanceApplyResult userBalanceApplyResult = new UserBalanceApplyResult();
		userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_FAILURE);
		userBalanceApplyResult.setError(UserBalanceApplyConst.ERROR_UID_NOTEXIST);
		this.returnQueryResults.add(userBalanceApplyResult);
	    //end 初始化余额请求的用例	
		
		//初始化更新余额的用例对象
		this.wantedUpdateApplys.clear();
		this.wantedUpdateBals.clear();
		this.returnUpdateResults.clear();
		BalanceTransRunning initDbTransaction = new BalanceTransRunning();
		initDbTransaction.setTransid("002000082212000033333333");		
		initDbTransaction.setTransactionTime(OrderPostUtil.getDateFromTransID("002000082212000033333333"));
		initDbTransaction.setBalance(0d);
		initDbTransaction.setUpdatetime(now.getTime());
		now.set(2700, 01, 01, 23, 59,59);
		initDbTransaction.setExpiretime(now.getTime());
		initDbTransaction.setUserid(bServiceContext.getWillDoneBTransRunning().getUserid());
		initDbTransaction.setStatus(BalanceServiceConst.Btrans_status_init);
		Calendar now = Calendar.getInstance();
		now.set(2700, 01, 01, 23, 59,59);
		//构造初始化数据库的余额
		UserBalance initUserBalance = new UserBalance();
		initUserBalance.setUserIdExist();
		initUserBalance.setBalance(0d);
		initUserBalance.setExpiredata(now.getTime());
		initUserBalance.setUpdatetime(Calendar.getInstance().getTime());
		initUserBalance.setTransaction("002000082212000033333333");
		//构造初始化余额的请求
		userBalanceApply=new UserBalanceApply();
		userBalanceApply.setTransaction(initUserBalance.getTransaction());
		userBalanceApply.setUserId(this.bServiceContext.getCommissionPresentInfo().getSubsId());
		userBalanceApply.setTransactionTime(OrderPostUtil.getDateFromTransID("002000082212000033333333"));
		userBalanceApply.setAmount(0d);
		//构造初始化余额的返回值
		 userBalanceApplyResult = new UserBalanceApplyResult();
		userBalanceApplyResult.setAmount(0);
		userBalanceApplyResult.setBalance(1000);
		
		userBalanceApplyResult.setExpiredata(now.getTime());
		userBalanceApplyResult.setUserId(userBalanceApply.getUserId());
		userBalanceApplyResult.setTransaction(userBalanceApply.getTransaction());
		userBalanceApplyResult.setUpdatetime(Calendar.getInstance().getTime());
		userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_SUCCESS_init);
		
		//end 初始化余额的用例
		//start 更新余额的用例
		//end  更新余额的用例
		
	}
*/
	/**
	 * @return the testCaseType
	 */
	public int getTestCaseType() {
		return testCaseType;
	}
	/**
	 * @param testCaseType the testCaseType to set
	 */
	public void setTestCaseType(int testCaseType) {
		this.testCaseType = testCaseType;
	}
	@Override
	public UserBalanceApplyResult updateUserBalance(UserBalance nowUseBalance, UserBalanceApply userBalanceApply) {
		UserBalanceApplyResult userBalanceApplyResult =new UserBalanceApplyResult();
		if(this.testCaseUpdateSteps==this.Tc_step_update_createBDb)
		{
			
			UserBalance wantedUserBalance = new UserBalance();
			Calendar now = Calendar.getInstance();
			now.set(2700, 01, 01, 23, 59,59);
			wantedUserBalance.setUserIdExist();
			wantedUserBalance.setBalance(0d);
			wantedUserBalance.setExpiredata(now.getTime());
			wantedUserBalance.setUpdatetime(nowUseBalance.getUpdatetime());
			wantedUserBalance.setTransaction("002000082212000033333333");
		
			//构造初始化余额的请求
			UserBalanceApply wantedBalanceApply=new UserBalanceApply();
			wantedBalanceApply.setTransaction(wantedUserBalance.getTransaction());
			wantedBalanceApply.setUserId(this.bServiceContext.getCommissionPresentInfo().getSubsId());
			wantedBalanceApply.setTransactionTime(OrderPostUtil.getDateFromTransID("002000082212000033333333"));
			wantedBalanceApply.setAmount(0d);
			//构造初始化余额的返回值
			 userBalanceApplyResult = new UserBalanceApplyResult();
			userBalanceApplyResult.setAmount(0);
			userBalanceApplyResult.setBalance(0d);
			
			userBalanceApplyResult.setExpiredata(now.getTime());
			userBalanceApplyResult.setUserId(userBalanceApply.getUserId());
			userBalanceApplyResult.setTransaction(userBalanceApply.getTransaction());
			userBalanceApplyResult.setUpdatetime(Calendar.getInstance().getTime());
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_SUCCESS_init);
			
			assertEquals("updateUserBalance initdb nowUseBalance not equal;",wantedUserBalance.toString(),nowUseBalance.toString());
			assertEquals("updateUserBalance initdb Apply not equal;",wantedBalanceApply.toString(),userBalanceApply.toString());
			
			assertEquals("updateUserBalance initdb userid not equal;",bServiceContext.getCommissionPresentInfo().getSubsId(),userBalanceApply.getUserId());
			this.testCaseUpdateSteps = this.Tc_step_Update_balance_Add;
			this.gUserBalance = wantedUserBalance;
			gUserBalance.setUserId(wantedBalanceApply.getUserId());
		}
		else if(this.testCaseUpdateSteps==this.Tc_step_Update_balance_Add||
				this.Tc_step_Update_balance_reduce==testCaseUpdateSteps)
		{			
			
			//更新余额
			
			assertEquals("updateUserBalance updateFirst userid not equal:",gUserBalance.getUserId(),nowUseBalance.getUserId());
			assertEquals("updateUserBalance updateFirst transaction not equal:",gUserBalance.getTransaction(),nowUseBalance.getTransaction());
			assertEquals("updateUserBalance updateFirst balance not equal:",gUserBalance.getBalance(),nowUseBalance.getBalance(),0);
			assertEquals("updateUserBalance updateFirst expireTime not equal:",gUserBalance.getExpiredata().toString(),nowUseBalance.getExpiredata().toString());
			
			assertEquals("updateUserBalance updateFirst amnt not equal:",bServiceContext.getCommissionPresentInfo().getAmt(),userBalanceApply.getAmount(),0);
			
			assertEquals("updateUserBalance updateFirst userid not equal:",bServiceContext.getCommissionPresentInfo().getSubsId(),userBalanceApply.getUserId());
			assertEquals("updateUserBalance updateFirst transid not equal:",bServiceContext.getCommissionPresentInfo().getReqTransId(),userBalanceApply.getTransaction());
			assertEquals("updateUserBalance updateFirst transtime not equal:",bServiceContext.getWillDoneBTransRunning().getTransactionTime().toString(),userBalanceApply.getTransactionTime().toString());
			
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_SUCCESS);
			userBalanceApplyResult.setAmount(gUserBalance.getAmount());
			userBalanceApplyResult.setBalance(gUserBalance.getBalance() - userBalanceApply.getAmount());
			userBalanceApplyResult.setExpiredata(gUserBalance.getExpiredata());
			userBalanceApplyResult.setTransaction(userBalanceApply.getTransaction());
			userBalanceApplyResult.setUserId(userBalanceApply.getUserId());
			userBalanceApplyResult.setUpdatetime(Calendar.getInstance().getTime());
			this.testCaseUpdateSteps++;
			this.gUserBalance.setTransaction(userBalanceApply.getTransaction());
			this.gUserBalance.setBalance(gUserBalance.getBalance() - userBalanceApply.getAmount());
			
		}
		else if(Tc_step_Update_balance_Trans_haveDone==testCaseUpdateSteps)
		{
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_FAILURE);
			userBalanceApplyResult.setError(UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE);
			userBalanceApplyResult.setAmount(gUserBalance.getAmount());
			userBalanceApplyResult.setBalance(gUserBalance.getBalance());
			userBalanceApplyResult.setExpiredata(gUserBalance.getExpiredata());
			userBalanceApplyResult.setTransaction(gUserBalance.getTransaction());
			userBalanceApplyResult.setUserId(gUserBalance.getUserId());
			userBalanceApplyResult.setUpdatetime(Calendar.getInstance().getTime());
			
		}
		else if(Tc_step_Update_balance_Trans_notLastTrans==testCaseUpdateSteps)
		{
			
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_FAILURE);
			userBalanceApplyResult.setError(UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONENotLast);
			userBalanceApplyResult.setTransaction("00201203241213140000000");
			testCaseUpdateSteps=Tc_step_Update_balance_Trans_haveDone;
		}
		else
		{
			
			assertEquals("updateUserBalance updateFirst userid not equal:",gUserBalance.getUserId(),nowUseBalance.getUserId());
			assertEquals("updateUserBalance updateFirst transaction not equal:",gUserBalance.getTransaction(),nowUseBalance.getTransaction());
			assertEquals("updateUserBalance updateFirst balance not equal:",gUserBalance.getBalance(),nowUseBalance.getBalance(),0);
			assertEquals("updateUserBalance updateFirst expireTime not equal:",gUserBalance.getExpiredata().toString(),nowUseBalance.getExpiredata().toString());
			
			assertEquals("updateUserBalance updateFirst amnt not equal:",bServiceContext.getCommissionPresentInfo().getAmt(),userBalanceApply.getAmount(),0);
			
			assertEquals("updateUserBalance updateFirst userid not equal:",bServiceContext.getCommissionPresentInfo().getSubsId(),userBalanceApply.getUserId());
			assertEquals("updateUserBalance updateFirst transid not equal:",bServiceContext.getCommissionPresentInfo().getReqTransId(),userBalanceApply.getTransaction());
			assertEquals("updateUserBalance updateFirst transtime not equal:",bServiceContext.getWillDoneBTransRunning().getTransactionTime().toString(),userBalanceApply.getTransactionTime().toString());
			
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_SUCCESS);
			userBalanceApplyResult.setAmount(gUserBalance.getAmount());
			userBalanceApplyResult.setBalance(gUserBalance.getBalance() - userBalanceApply.getAmount());
			userBalanceApplyResult.setExpiredata(gUserBalance.getExpiredata());
			userBalanceApplyResult.setTransaction(userBalanceApply.getTransaction());
			userBalanceApplyResult.setUserId(userBalanceApply.getUserId());
			userBalanceApplyResult.setUpdatetime(Calendar.getInstance().getTime());
			this.gUserBalance.setTransaction(userBalanceApply.getTransaction());
			this.gUserBalance.setBalance(gUserBalance.getBalance() - userBalanceApply.getAmount());
			
			
			}
		return userBalanceApplyResult;
	}
	@Override
	public UserBalanceApplyResult queryTransaction(UserBalanceApply userBalanceApply) {
		UserBalanceApplyResult userBalanceApplyResult =null;
		UserBalanceApply wantUserQuery = null;
		if(Tc_step_lastBTrans==this.testCaseQuerySteps)
	    {
			
			 userBalanceApply = new UserBalanceApply();
			userBalanceApply.setTransaction(UserBalanceApplyConst.queryLastTransaction);
			userBalanceApply.setUserId(bServiceContext.getCommissionPresentInfo().getSubsId());
			 userBalanceApplyResult = new UserBalanceApplyResult();
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_FAILURE);
			userBalanceApplyResult.setError(UserBalanceApplyConst.ERROR_UID_NOTEXIST);
			
            assertEquals("queryTransaction initdb transaction not equal;",UserBalanceApplyConst.queryLastTransaction,userBalanceApply.getTransaction());
            if(wantUserQuery!=null)
			{
            	 assertEquals("queryTransaction initdb transaction not equal;",wantUserQuery.toString(),userBalanceApply.toString());
                 
			}
            System.out.println(bServiceContext.getCommissionPresentInfo());
			System.out.println(userBalanceApply.getUserId());
			assertEquals("queryTransaction initdb userid not equal;",bServiceContext.getCommissionPresentInfo().getSubsId(),userBalanceApply.getUserId());
			userBalanceApplyResult.setResult(UserBalanceApplyConst.RESULT_FAILURE);
			userBalanceApplyResult.setError(UserBalanceApplyConst.ERROR_UID_NOTEXIST);
			
	   
	   }
		return userBalanceApplyResult;
	}
	/**
	 * @return the bServiceContext
	 */
	public BalanceServiceContext getbServiceContext() {
		return bServiceContext;
	}
	/**
	 * @param bServiceContext the bServiceContext to set
	 */
	public void setbServiceContext(BalanceServiceContext bServiceContext) {
		this.bServiceContext = bServiceContext;
	}
	/**
	 * @return the testCaseQuerySteps
	 */
	public int getTestCaseQuerySteps() {
		return testCaseQuerySteps;
	}
	/**
	 * @param testCaseQuerySteps the testCaseQuerySteps to set
	 */
	public void setTestCaseQuerySteps(int testCaseQuerySteps) {
		this.testCaseQuerySteps = testCaseQuerySteps;
	}
	/**
	 * @return the testCaseUpdateSteps
	 */
	public int getTestCaseUpdateSteps() {
		return testCaseUpdateSteps;
	}
	/**
	 * @param testCaseUpdateSteps the testCaseUpdateSteps to set
	 */
	public void setTestCaseUpdateSteps(int testCaseUpdateSteps) {
		this.testCaseUpdateSteps = testCaseUpdateSteps;
	}
	
	
}
