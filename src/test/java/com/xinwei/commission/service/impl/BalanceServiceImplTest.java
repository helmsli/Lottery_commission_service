/**
 * 
 */
package com.xinwei.commission.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commAccessDb.service.BalanceTransDb;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.domain.BalanceServiceContext;
import com.xinwei.commission.service.BalanceCacheService;
import com.xinwei.commission.service.BalanceService;
import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;
import com.xinwei.orderpost.domain.CommissionPresentInfo;
import com.xinwei.orderpost.facade.CommissionPresentService;

/**
 * @author helmsli
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
//@ContextConfiguration(locations = {"classpath:spring.xml","classpath:spring-mybatis.xml"})
public class BalanceServiceImplTest{
    
	@Autowired
	protected CommissionPresentService balanceService;
	
	@Resource(name="serviceUserBlance")
	private ServiceUserBlance serviceUserBlance;
	
	
	@Autowired
	protected BalanceTransDb balanceTransDb;
	@Autowired
	protected BalanceCacheService balanceCacheService;
	
	
	/**
	 * 查询的结果返回不存在
	 */
	protected int Control_BtransDb_query_notExist = 1;
	
	
	
	BalanceServiceContext bServiceContext = new BalanceServiceContext();
	
	/**
	 * Test method for {@link com.xinwei.commission.service.impl.BalanceServiceImpl#pOneCommBalance(com.xinwei.commission.domain.BalanceServiceContext, com.xinwei.orderpost.domain.CommissionPresentInfo)}.
	 */
	
	@Before
	public void setUp() throws Exception {
		
	}

	
	protected CommissionPresentInfo createCommissionPresentInfo()
	{
		 
	   
		CommissionPresentInfo commissionPresentInfo = new CommissionPresentInfo();
		commissionPresentInfo.setAmt(-100);
		commissionPresentInfo.setBizType(1);
		commissionPresentInfo.setExpireTime("20170911121314");
		commissionPresentInfo.setMsgInfo("msginfo");
		commissionPresentInfo.setOperType(1);
		commissionPresentInfo.setOrderID("orderid");
		commissionPresentInfo.setReason("reason");
		commissionPresentInfo.setReqTime("20170911121314");
		commissionPresentInfo.setReqTransId("00201708231735001234567");
		commissionPresentInfo.setSubsId(10000);
		return commissionPresentInfo;
	}
	/**
	 * 测试正常余额变更，假设余额为100，交易金额为10，应该余额成功，；最终形成交易记录，并且保存交易记录；
	 */
	@Test
	public void testInitDb() {
		
		//初始化context 
		BalanceInitDbTestContext balanceDbTestContext = new BalanceInitDbTestContext();
		 ServiceUserBalanceMock sUserviceBaseBalanceTrans = (ServiceUserBalanceMock)serviceUserBlance;
		 sUserviceBaseBalanceTrans.setServiceUserBlance(balanceDbTestContext);
		
		  BTransInitDbTestContext bTransDbTestContext = new BTransInitDbTestContext();
			
			ServiceBalanceTransDbMock serviceBalanceTransDbMock = (ServiceBalanceTransDbMock)balanceTransDb;
			serviceBalanceTransDbMock.setBalanceTransDb(bTransDbTestContext);
			
			
		BalanceServiceImpl balanceServiceImpl = (BalanceServiceImpl)balanceService;
		CommissionPresentInfo commissionPresentInfo = this.createCommissionPresentInfo();
		BalanceTransRunning bTransRunning = balanceServiceImpl.getFromPresentInfo(commissionPresentInfo);
		this.balanceCacheService.delUserBalance(commissionPresentInfo.getSubsId());
		this.balanceCacheService.delTransFromCache(bTransRunning);
		
		//从用户数据库查询余额应该返回余额不存在
		balanceDbTestContext.setTestCaseType(balanceDbTestContext.TestCaseType_initdb);
		
		//balanceDbTestContext.setControlService_query(UserBalanceApplyConst.ERROR_UID_NOTEXIST);
		//balanceDbTestContext.setControlService_update(UserBalanceApplyConst.ERROR_UID_NOTEXIST);
		//bTransDbTestContext.setControlService_query(Control_BtransDb_query_notExist);
		bServiceContext.setCommissionPresentInfo(commissionPresentInfo);	
		balanceDbTestContext.setbServiceContext(bServiceContext);
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		BalanceTransRunning balanceTransRunning = balanceServiceImpl.getFromPresentInfo(commissionPresentInfo);
		System.out.println(commissionPresentInfo);
		assertEquals("inituser error:",UserBalanceApplyConst.RESULT_SUCCESS,commissionPresentInfo.getResult());
		//继续测试减钱
		testReduceMonyeDb(balanceDbTestContext,commissionPresentInfo);
		testAccessCacheHaveDone(balanceDbTestContext,commissionPresentInfo);
		testAddMonyeDb(balanceDbTestContext,commissionPresentInfo);
		testTransHaveDone(balanceDbTestContext,commissionPresentInfo);
	}
	/**
	 * 测试减余额
	 * @param balanceDbTestContext
	 * @param commissionPresentInfo
	 */
	protected  void testReduceMonyeDb(BalanceInitDbTestContext balanceDbTestContext,CommissionPresentInfo commissionPresentInfo) {
		balanceDbTestContext.setTestCaseUpdateSteps(balanceDbTestContext.Tc_step_Update_balance_reduce);
		BalanceServiceImpl balanceServiceImpl = (BalanceServiceImpl)balanceService;
		
		commissionPresentInfo.setReqTransId("002017092312345609999999");
		commissionPresentInfo.setAmt(20);
		BalanceTransRunning bTransRunning = balanceServiceImpl.getFromPresentInfo(commissionPresentInfo);
		this.balanceCacheService.delTransFromCache(bTransRunning);
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		assertEquals("testReduceMonyeDb error:",UserBalanceApplyConst.RESULT_SUCCESS,commissionPresentInfo.getResult());
		
	}
	
	/**
	 * 测试交易信息已经执行并且数据正确和数据不正确
	 * @param balanceDbTestContext
	 * @param commissionPresentInfo
	 */
	protected  void testAccessCacheHaveDone(BalanceInitDbTestContext balanceDbTestContext,CommissionPresentInfo commissionPresentInfo) {
		balanceDbTestContext.setTestCaseUpdateSteps(balanceDbTestContext.Tc_step_Update_balance_reduce);
		BalanceServiceImpl balanceServiceImpl = (BalanceServiceImpl)balanceService;
		commissionPresentInfo.setReqTransId("002017092312345609999999");
		commissionPresentInfo.setAmt(20);
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		assertEquals("testAccessCacheHaveDone error:",BalanceServiceConst.Btrans_r_haveDone,commissionPresentInfo.getResult());
		
		commissionPresentInfo.setReqTransId("002017092312345609999999");
		commissionPresentInfo.setAmt(120);
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		assertEquals("testAccessCacheHaveDone error:",BalanceServiceConst.Btrans_r_haveDone_error,commissionPresentInfo.getResult());
		
	}
	/**
	 * 测试增加余额
	 * @param balanceDbTestContext
	 * @param commissionPresentInfo
	 */
	protected  void testAddMonyeDb(BalanceInitDbTestContext balanceDbTestContext,CommissionPresentInfo commissionPresentInfo) {
		balanceDbTestContext.setTestCaseUpdateSteps(balanceDbTestContext.Tc_step_Update_balance_Add);
		BalanceServiceImpl balanceServiceImpl = (BalanceServiceImpl)balanceService;
		commissionPresentInfo.setReqTransId("0020170923123456088888");
		commissionPresentInfo.setAmt(-30);
		BalanceTransRunning bTransRunning = balanceServiceImpl.getFromPresentInfo(commissionPresentInfo);
		this.balanceCacheService.delTransFromCache(bTransRunning);
		
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		assertEquals("testReduceMonyeDb error:",UserBalanceApplyConst.RESULT_SUCCESS,commissionPresentInfo.getResult());
		
	}
	/**
	 * 测试数据库已经执行完毕该业务的返回
	 * @param balanceDbTestContext
	 * @param commissionPresentInfo
	 */
	protected  void testTransHaveDone(BalanceInitDbTestContext balanceDbTestContext,CommissionPresentInfo commissionPresentInfo) {
		balanceDbTestContext.setTestCaseUpdateSteps(balanceDbTestContext.Tc_step_Update_balance_Trans_haveDone);
		BalanceServiceImpl balanceServiceImpl = (BalanceServiceImpl)balanceService;
		commissionPresentInfo.setReqTransId("002017092312345607777777");
		commissionPresentInfo.setAmt(-30);
		BalanceTransRunning bTransRunning = balanceServiceImpl.getFromPresentInfo(commissionPresentInfo);
		this.balanceCacheService.delTransFromCache(bTransRunning);
		balanceServiceImpl.pOneCommBalance(bServiceContext, commissionPresentInfo);
		assertEquals("testReduceMonyeDb error:",UserBalanceApplyConst.ERROR_TRANSACTION_HAVEDONE,commissionPresentInfo.getResult());
		
	}
	public UserBalanceApplyResult initdbQueryTransaction(UserBalanceApply userBalanceApply)
	{
		return null;
	}


}
