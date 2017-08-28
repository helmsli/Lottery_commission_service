package com.xinwei.commission.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commAccessDb.service.BalanceTransDb;

public class ServiceBalanceTransDbMock extends BalanceCacheKeyServiceImpl implements BalanceTransDb {
    protected Map<String, BalanceTransRunning> bTransRunningMap  = new HashMap();
	
    protected Map<String, BalanceTransRunning> wantRunningMap  = new HashMap();
    
    protected BalanceTransDb balanceTransDb=null;
	
	@Override
	public synchronized void  insertBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		if(balanceTransDb!=null)
		{
			 balanceTransDb.insertBalanceTransRunning(balanceTransRunning);
			 return;
		}
		String key = this.buildTransidKey(balanceTransRunning);
		bTransRunningMap.put(key, balanceTransRunning);
		return ;

	}

	@Override
	public synchronized List<BalanceTransRunning> selectBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		if(balanceTransDb!=null)
		{
			return balanceTransDb.selectBalanceTransRunning(balanceTransRunning);
			 
		}
		// TODO Auto-generated method stub
		List<BalanceTransRunning> bTransRunnings = new ArrayList();
		
		String key = this.buildTransidKey(balanceTransRunning);
		if(bTransRunningMap.containsKey(key))
		{
			bTransRunnings.add(bTransRunningMap.get(key));
		}
		return bTransRunnings;
	}

	@Override
	public int updateBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		if(balanceTransDb!=null)
		{
			return balanceTransDb.updateBalanceTransRunning(balanceTransRunning);
			 
		}
		String key = this.buildTransidKey(balanceTransRunning);
		bTransRunningMap.put(key, balanceTransRunning);
		
		return 0;
	}

	@Override
	public int deleteBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		if(balanceTransDb!=null)
		{
			return balanceTransDb.deleteBalanceTransRunning(balanceTransRunning);
			 
		}
		String key = this.buildTransidKey(balanceTransRunning);
		bTransRunningMap.remove(key);
		
		return 1;
		
	}

	public BalanceTransDb getBalanceTransDb() {
		return balanceTransDb;
	}

	public void setBalanceTransDb(BalanceTransDb balanceTransDb) {
		this.balanceTransDb = balanceTransDb;
	}

}
