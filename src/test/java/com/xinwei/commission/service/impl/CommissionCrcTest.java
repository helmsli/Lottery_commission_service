package com.xinwei.commission.service.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;
import com.xinwei.orderpost.common.OrderPostUtil;
import com.xinwei.orderpost.domain.CommissionPresentInfo;

public class CommissionCrcTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		/*
		 * 19:10:31.132 [pool-1-thread-1] INFO  com.xinwei.aspect.AspectLog - before presentCommission ,args:[[CommissionPresentInfo [reqTransId=3517101319191000090, subsId=1644467711, orderID=321710131909210011, amt=0.039, bizType=20041901, operType=2, reason=, reqTime=20171013191910, expireTime=null, signInfo=c8a7a4983888c64ff85c498492124de1, result=-1, msgInfo=null]]] 
		reqTransId=3517101319191000090, subsId=1644467711, orderID=321710131909210011, amt=0.039, bizType=20041901, operType=2, reason=, reqTime=20171013191910, expireTime=null, signInfo=c8a7a4983888c64ff85c498492124de1, result=8954, msgInfo=null]]]
		 c8a7a4983888c64ff85c498492124de1
		 */
		CommissionPresentInfo commissionPresentInfo = new CommissionPresentInfo();
		commissionPresentInfo.setAmt(0.039d);
		commissionPresentInfo.setBizType(20041901);
		commissionPresentInfo.setOperType(2);
		commissionPresentInfo.setReqTransId("3517101319191000090");
		commissionPresentInfo.setSubsId(1644467711);
		commissionPresentInfo.setSignInfo("c8a7a4983888c64ff85c498492124de1");
		
		String key  = OrderPostUtil.getPresentSignInfo(commissionPresentInfo, "354&*)^%^#$%");
	    if(key.equalsIgnoreCase(commissionPresentInfo.getSignInfo()))
	    {
	    	System.out.println("llllllllllllll");
	    }
	    System.out.println(key);
		fail("Not yet implemented");
	}

}
