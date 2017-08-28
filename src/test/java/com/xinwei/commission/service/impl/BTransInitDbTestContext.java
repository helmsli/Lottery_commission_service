package com.xinwei.commission.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commAccessDb.service.BalanceTransDb;
import com.xinwei.commission.domain.BalanceServiceContext;

public class BTransInitDbTestContext extends BalanceCacheKeyServiceImpl implements BalanceTransDb{
	
	protected Map<String,BalanceTransRunning> bTransRunnintMaps = new HashMap();
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
	public static final int Tc_step_QueryNotExist = 0;
	/**
	 * 记录当前查询的用例流程
	 */
	protected int testCaseQuerySteps =Tc_step_QueryNotExist;
	
	/**
	 * 更新数据库
	 */
	public static final int Tc_step_update_ok = 0;
	
	/**
	 *  更新余额的流程 
	 */
	protected int testCaseUpdateSteps =Tc_step_update_ok;

	/**
	 * 更新数据库
	 */
	public static final int Tc_step_insert_ok = 0;
	
	/**
	 *  更新余额的流程 
	 */
	protected int testCaseInsertSteps =Tc_step_insert_ok;
	
	
	
	//业务的上下文信息；
	BalanceServiceContext bServiceContext = null;



	@Override
	public void insertBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public List<BalanceTransRunning> selectBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		List<BalanceTransRunning> lists = new ArrayList();
		
		if(Tc_step_QueryNotExist == this.testCaseQuerySteps)
		{
			return lists;
		}
		else
		{
			String key = this.buildTransidKey(balanceTransRunning);
			if(this.bTransRunnintMaps.containsKey(key))
			{
				BalanceTransRunning qbTransRunning = bTransRunnintMaps.get(key);
				lists.add(qbTransRunning);
				
			}
		}
		return lists;
	}



	@Override
	public int updateBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		String key = this.buildTransidKey(balanceTransRunning);
		bTransRunnintMaps.put(key, balanceTransRunning);
		return 1;
	}



	@Override
	public int deleteBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		String key = this.buildTransidKey(balanceTransRunning);
		bTransRunnintMaps.remove(key);
		return 1;
	}



	/**
	 * @return the bTransRunnintMaps
	 */
	public Map<String, BalanceTransRunning> getbTransRunnintMaps() {
		return bTransRunnintMaps;
	}



	/**
	 * @param bTransRunnintMaps the bTransRunnintMaps to set
	 */
	public void setbTransRunnintMaps(Map<String, BalanceTransRunning> bTransRunnintMaps) {
		this.bTransRunnintMaps = bTransRunnintMaps;
	}
	
}
