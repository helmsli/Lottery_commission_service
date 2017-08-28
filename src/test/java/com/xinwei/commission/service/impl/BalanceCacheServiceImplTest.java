package com.xinwei.commission.service.impl;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.service.BalanceCacheService;
@RunWith(SpringRunner.class)
@SpringBootTest
public class BalanceCacheServiceImplTest extends BalanceCacheKeyServiceImpl{
	private int transidIndex=0;
	private Map<String,Integer> lockMaps = new java.util.concurrent.ConcurrentHashMap<String,Integer>();
	private ExecutorService pool =  Executors.newCachedThreadPool();
	@Autowired
	protected BalanceCacheService balanceCacheService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReleaseUserByOthers() {
		//构造第一个业务申请锁
				BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
				balanceTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_high);
				balanceTransRunning.setUserid(3000);
				balanceTransRunning.setTransactionTime(Calendar.getInstance().getTime());
				balanceTransRunning.setTransid("00201409081213149999999");
				boolean isLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
				assertEquals("testNoDeadLock lock first error:",true,isLock);
				
				try {
					
					//构造第二个低优先级锁
					BalanceTransRunning bTransRunning1=cloneLPriorityBtrans(balanceTransRunning);
					runTask(bTransRunning1);
					balanceTransRunning.setTransid("traaa");
					balanceCacheService.endTransToCache(balanceTransRunning);
					Thread.sleep(BalanceServiceConst.Btrans_Lock_Timeout*1000+1000);
					String key1  = this.buildTransidKey(bTransRunning1);
					Integer mapValue = lockMaps.get(key1);
					//低优先级拿不到锁
					assertEquals("testNoDeadLock lock first error:",0,mapValue.intValue());
							
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					fail("testBeginTransToCache error lock");
					e.printStackTrace();
				}
	}
	
    protected void runTask(BalanceTransRunning balanceTransRunning) throws InterruptedException
    {
    	
    	final String key = this.buildTransidKey(balanceTransRunning);
    	pool.execute(new Runnable() {
				public void run() {
					try {
						boolean isGetLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout-1);
						if(isGetLock)
						{
							
							lockMaps.put(key, new Integer(1));
						}
						else
						{
							lockMaps.put(key, new Integer(0));
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});
			
    }
    public synchronized BalanceTransRunning cloneBtrans(BalanceTransRunning balanceTransRunning)
    {
    	BalanceTransRunning rTransRunning=new BalanceTransRunning();
    	rTransRunning.setUserid(balanceTransRunning.getUserid());
    	transidIndex++;
    	rTransRunning.setTransid(balanceTransRunning.getTransid()+String.valueOf(transidIndex));
    	rTransRunning.setTransactionTime(balanceTransRunning.getTransactionTime());
    	return rTransRunning;
    }
    public synchronized BalanceTransRunning cloneHPriorityBtrans(BalanceTransRunning balanceTransRunning)
    {
    	BalanceTransRunning rTransRunning=cloneBtrans(balanceTransRunning);
    	rTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_high);
    	return rTransRunning;
    }
    public synchronized BalanceTransRunning cloneLPriorityBtrans(BalanceTransRunning balanceTransRunning)
    {
    	BalanceTransRunning rTransRunning=cloneBtrans(balanceTransRunning);
    	rTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_low);
    	return rTransRunning;
    }
	@Test
	public void testBeginTransToCache() {
		//构造第一个业务申请锁
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_low);
		balanceTransRunning.setUserid(2000);
		balanceTransRunning.setTransactionTime(Calendar.getInstance().getTime());
		balanceTransRunning.setTransid("00201409081213149999999");
		boolean isLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
		assertEquals("lock first error:",true,isLock);
		
		try {
			//构造第二个低优先级锁
			BalanceTransRunning bTransRunning1=cloneLPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning1);
			//构造第三个低优先级锁
			BalanceTransRunning bTransRunning2=cloneLPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning2);
			//构造第一个高优先级锁			
			Thread.sleep(1000);
			BalanceTransRunning bTransRunning3=cloneHPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning3);
			
			//结束第一个低优先级的锁 
			Thread.sleep(1000);
			balanceCacheService.endTransToCache(balanceTransRunning);
			
			//加入第二个高优先级锁
			Thread.sleep(1000);
			BalanceTransRunning bTransRunning4=cloneHPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning4);
			//结束第一个高优先级锁
			balanceCacheService.endTransToCache(bTransRunning3);
			
			
			Thread.sleep(BalanceServiceConst.Btrans_Lock_Timeout*1000);
			assertEquals("lock first error:",4,this.lockMaps.size());
			String key1  = this.buildTransidKey(bTransRunning1);
			Integer mapValue = lockMaps.get(key1);
			//低优先级拿不到锁
			assertEquals("lock first error:",0,mapValue.intValue());
			//低优先级拿不到锁
			key1  = this.buildTransidKey(bTransRunning2);
			 mapValue = lockMaps.get(key1);
			assertEquals("lock first error:",0,mapValue.intValue());
			//高优先级拿到锁
			key1  = this.buildTransidKey(bTransRunning3);
			 mapValue = lockMaps.get(key1);
			assertEquals("lock first error:",1,mapValue.intValue());
			//高优先级拿到锁
			key1  = this.buildTransidKey(bTransRunning4);
			 mapValue = lockMaps.get(key1);
			assertEquals("lock first error:",1,mapValue.intValue());
				
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("testBeginTransToCache error lock");
			e.printStackTrace();
		}
		
		
		
		
	}
	/**
	 * 低优先级锁不释放是否会死锁
	 */
	@Test
	public void testNoDeadLock() {
		//构造第一个业务申请锁
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_low);
		balanceTransRunning.setUserid(2000);
		balanceTransRunning.setTransactionTime(Calendar.getInstance().getTime());
		balanceTransRunning.setTransid("00201409081213149999999");
		boolean isLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
		assertEquals("testNoDeadLock lock first error:",true,isLock);
		
		try {
			
			Thread.sleep(BalanceServiceConst.Btrans_Lock_Timeout*1000);
			//构造第二个低优先级锁
			BalanceTransRunning bTransRunning1=cloneLPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning1);
			Thread.sleep(1000);
			
			String key1  = this.buildTransidKey(bTransRunning1);
			Integer mapValue = lockMaps.get(key1);
			//低优先级拿不到锁
			assertEquals("testNoDeadLock lock first error:",1,mapValue.intValue());
					
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("testBeginTransToCache error lock");
			e.printStackTrace();
		}
		
		
		
		
	}
	/**
	 * 高优先级锁不释放是否会死锁
	 */
	@Test
	public void testNoDeadLockForHigh() {
		//构造第一个业务申请锁
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_high);
		balanceTransRunning.setUserid(2000);
		balanceTransRunning.setTransactionTime(Calendar.getInstance().getTime());
		balanceTransRunning.setTransid("00201409081213149999999");
		boolean isLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
		assertEquals("testNoDeadLock lock first error:",true,isLock);
		
		try {
			
			Thread.sleep(BalanceServiceConst.Btrans_Lock_Timeout*1000);
			//构造第二个低优先级锁
			BalanceTransRunning bTransRunning1=cloneLPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning1);
			Thread.sleep(1000);
			String key1  = this.buildTransidKey(bTransRunning1);
			Integer mapValue = lockMaps.get(key1);
			//低优先级拿不到锁
			assertEquals("testNoDeadLock lock first error:",1,mapValue.intValue());
					
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("testBeginTransToCache error lock");
			e.printStackTrace();
		}
	}
	/**
	 * 高优先级锁释放后立即交给低优先级
	 */
	@Test
	public void testHighReleaseOk() {
		//构造第一个业务申请锁
		BalanceTransRunning balanceTransRunning = new BalanceTransRunning();
		balanceTransRunning.setRunPriority(BalanceServiceConst.Btrans_run_priority_high);
		balanceTransRunning.setUserid(2000);
		balanceTransRunning.setTransactionTime(Calendar.getInstance().getTime());
		balanceTransRunning.setTransid("00201409081213149999999");
		boolean isLock = balanceCacheService.beginTransToCache(balanceTransRunning, BalanceServiceConst.Btrans_Lock_Timeout);
		assertEquals("testNoDeadLock lock first error:",true,isLock);
		
		try {
			
			//构造第二个低优先级锁
			BalanceTransRunning bTransRunning1=cloneLPriorityBtrans(balanceTransRunning);
			runTask(bTransRunning1);
			balanceCacheService.endTransToCache(balanceTransRunning);
			Thread.sleep(1000);
			String key1  = this.buildTransidKey(bTransRunning1);
			Integer mapValue = lockMaps.get(key1);
			//低优先级拿不到锁
			assertEquals("testNoDeadLock lock first error:",1,mapValue.intValue());
					
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			fail("testBeginTransToCache error lock");
			e.printStackTrace();
		}
	}
}
