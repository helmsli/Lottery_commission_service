package com.xinwei.commission.service.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.Const.BalanceServiceConst;
import com.xinwei.commission.service.BalanceCacheService;
import com.xinwei.lotteryDb.domain.UserBalance;
@Service("balanceCacheService")
public class BalanceCacheServiceImpl extends BalanceCacheKeyServiceImpl implements BalanceCacheService  {
    	
	//单个业务持有锁的时间30s,防止死锁
	protected final static long LOCK_EXPIRE = 30 * 1000L;  
	
	//默认30ms尝试获取一次锁
	protected final static long LOCK_TRY_INTERVAL = 30L;   
    // 获取锁超时时间 30s
	protected final static long LOCK_TRY_TIMEOUT = 30 * 1000L;   
	  
	
	
	/**
	 *等待处理的低优先级别的队列个数。 
	 */
	protected final static int Max_wait_hProcess=5;
	
	//高级优先级锁过期时间
    
	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
		
	/**
	 * 判断锁是否被该用户加锁
	 * @param balanceTransRunning
	 * @return
	 */
	public boolean isLockByOwner(BalanceTransRunning balanceTransRunning)
	{
		try {
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			String lockKey = buildUserLockKey(balanceTransRunning.getUserid());
			String transValue = buildUserLockTransValue(balanceTransRunning);
			//获取redis的cache；
			String cacheTransValue = (String)opsForValue.get(lockKey);
			if(transValue.equalsIgnoreCase(cacheTransValue))
			{
				return true;
			}
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param lock
	 */
	public synchronized void releaseUserTransLock(BalanceTransRunning balanceTransRunning) {  
        
		try {
			
			boolean lockOk = isLockByOwner(balanceTransRunning);
			if(lockOk)
			{
				//need to redo;maybe 3 times
				String lockKey = buildUserLockKey(balanceTransRunning.getUserid());				
				redisTemplate.delete(lockKey);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }  
	
	/**
	 * 判断是否具备申请锁的条件
	 * @param balanceTransRunning
	 * @return -- 返回高优先级锁的个数
	 */
	protected long prepareGetLock(BalanceTransRunning balanceTransRunning)
	{
		String hCountkey = this.buildUserHighLockKey(balanceTransRunning);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		Long nowLockHNums = (Long)opsForValue.increment(hCountkey, 0);
		
		if(nowLockHNums==null)
		{
			return 0;
		}
		long retValue =nowLockHNums.longValue();
		if(retValue<=0)
		{
			redisTemplate.delete(hCountkey);
			return retValue;
		}
		String hExpireKey = this.buildUserHighExpireLockKey(balanceTransRunning);
		Long expireTime = (Long)opsForValue.get(hExpireKey);
		if(expireTime!=null && System.currentTimeMillis() - expireTime.longValue()> LOCK_TRY_TIMEOUT)
		{
			redisTemplate.delete(hCountkey);			
			return 0;
		}
		return retValue;
		
		
	}
	
	/**
	 * 注册高优先级别的队列
	 * @param balanceTransRunning
	 * @return
	 */
	protected long regisHighLock(BalanceTransRunning balanceTransRunning)
	{
		long retValue =-1;
		if(BalanceServiceConst.isHighRunning(balanceTransRunning.getRunPriority()))
		{
			String hCountkey = this.buildUserHighLockKey(balanceTransRunning);
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			Long registerdNumber = opsForValue.increment(hCountkey, 1);
			if(registerdNumber.longValue()<=0)
			{
				redisTemplate.delete(hCountkey);
				opsForValue.increment(hCountkey, 1);
			}
			String hExpireKey = this.buildUserHighExpireLockKey(balanceTransRunning);
			long currentTime = System.currentTimeMillis();
			opsForValue.set(hExpireKey,new Long(currentTime));
			return BalanceServiceConst.Btrans_run_priority_high;
		}
		return retValue;
	}
	/**
	 * 高优先级的操作释放队列
	 * @param balanceTransRunning
	 * @return
	 */
	protected boolean releaseHighLock(BalanceTransRunning balanceTransRunning)
	{
		
		if(BalanceServiceConst.isHighRunning(balanceTransRunning.getRunPriority()))
		{
			final String hCountkey = this.buildUserHighLockKey(balanceTransRunning);
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		     /*
			Long a = redisTemplate.execute(new RedisCallback<Long>() {

				@Override
				public Long doInRedis(RedisConnection conn) throws DataAccessException {
					RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
					byte[] serialize = stringSerializer.serialize(hCountkey);
					Long ret = conn.incr(serialize);
					conn.incr(arg0)
					byte[] bs = conn.get(serialize);
					String deserialize = stringSerializer.deserialize(bs);
					redisTemplate.get
					Long.valueOf(l)
					return ret;
				}
				
			});
			*/
			Long retValue = opsForValue.increment(hCountkey, -1);
			if(retValue.longValue()<=0)
			{
				redisTemplate.delete(hCountkey);
				String hExpireKey = this.buildUserHighExpireLockKey(balanceTransRunning);
				redisTemplate.delete(hExpireKey);
			}
			
		}
		return true;
	}
	
	/**
	 * 获取分布式锁
	 * @param balanceTransRunning  锁的对象
	 * @param timeout 获取的超时时间
	 * @param tryInterval 多少ms尝试一次
	 * @param lockExpireTime 获取成功后锁的过期时间
	 * @return true 获取成功，false获取失败
	 */
	protected boolean  getUserTransLock(BalanceTransRunning balanceTransRunning,long timeout,long tryInterval,long lockExpireTime){  
		long registerHProcess = -1;
        try{  
            
        	String lockKey = buildUserLockKey(balanceTransRunning.getUserid());
            long startTime = System.currentTimeMillis();  
            String transValue = buildUserLockTransValue(balanceTransRunning);
            registerHProcess=this.regisHighLock(balanceTransRunning);
            while (true){
            	//获取目前等待处理的高优先级的任务个数
            	long nowWaitHPNums = prepareGetLock(balanceTransRunning);
            	if(nowWaitHPNums>=Max_wait_hProcess)
            	{
            		return false;
            	}
            	//如果是高优先级任务或者高优先级任务等待个数为0
            	if(registerHProcess==BalanceServiceConst.Btrans_run_priority_high||nowWaitHPNums<=0)
            	{
	            	if(redisTemplate.opsForValue().setIfAbsent(lockKey,transValue)){  
	                	redisTemplate.opsForValue().set(lockKey,transValue,lockExpireTime,TimeUnit.SECONDS);  
	                	
	                	return true;  
	                }
            	}
                //如果没有获取到，并且已经超时
                if(System.currentTimeMillis() - startTime > timeout){  
                    return false;  
                }  
                //延迟一段时间
                Thread.sleep(tryInterval);  
            }  
        }catch (Exception e){  
              e.printStackTrace();
            return false;  
        }  
        finally
        {
        	//如果注册了高优先级别队列，需要释放锁
        	if(registerHProcess==BalanceServiceConst.Btrans_run_priority_high)
        	{
        		try {
        		this.releaseHighLock(balanceTransRunning);
        		}
        		catch(Exception e) {
        			
        		}
        	}
        }
    }  
	
	/**
	 * 获取默认锁
	 * @param balanceTransRunning
	 * @return
	 */
	protected boolean getDefalutUserTransLock(BalanceTransRunning balanceTransRunning)
	{
		return getUserTransLock(balanceTransRunning,LOCK_TRY_TIMEOUT,this.LOCK_TRY_INTERVAL,this.LOCK_EXPIRE);
	}
	
	@Override
	public BalanceTransRunning getTransFromCache(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		long userid = balanceTransRunning.getUserid();
		Date transactionTime = balanceTransRunning.getTransactionTime();
		String transid = balanceTransRunning.getTransid();
		
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		
		String transKey = buildTransidKey(userid,transactionTime,transid);
		
		BalanceTransRunning cacheBTransRunning = (BalanceTransRunning)opsForValue.get(transKey);	
		return cacheBTransRunning;
	}

	/**
	 * 设置cache数值，没有锁
	 * @param balanceTransRunning
	 * @return
	 */
	protected boolean setCacheValue(BalanceTransRunning balanceTransRunning)
	{
		
		try {
			return setCacheValue(balanceTransRunning,240);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 设置cache数值，没有锁
	 * @param balanceTransRunning
	 * @param expireHours
	 * @return
	 */
	protected boolean setCacheValue(BalanceTransRunning balanceTransRunning,long expireHours)
	{
		
		try {
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			long userid = balanceTransRunning.getUserid();
			
			Date transactionTime = balanceTransRunning.getTransactionTime();
			String transid = balanceTransRunning.getTransid();
			String transKey = buildTransidKey(userid,transactionTime,transid);
			//opsForValue.set(transKey, balanceTransRunning);
			opsForValue.set(transKey, balanceTransRunning, expireHours, TimeUnit.HOURS);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 
	 * @param UserBalance
	 * @return
	 */
	protected boolean setCacheValue(UserBalance userBalance)
	{
		
		try {
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			long userid = userBalance.getUserId();
			String transKey = this.buildUserBalKey(userid);
			opsForValue.set(transKey, userBalance);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	@Override
	public boolean delTransFromCache(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
				long userid = balanceTransRunning.getUserid();
				Date transactionTime = balanceTransRunning.getTransactionTime();
				String transid = balanceTransRunning.getTransid();
				
				
				String transKey = buildTransidKey(userid,transactionTime,transid);
				redisTemplate.delete(transKey);
				return true;

	}
	
	@Override
	public synchronized  boolean setTransToCache(BalanceTransRunning balanceTransRunning,long durations) {
		// TODO Auto-generated method stub
		boolean isGotLock =false;
		try {
			isGotLock =  isLockByOwner(balanceTransRunning);
			if(isGotLock)
			{
				return setCacheValue(balanceTransRunning);
			}
			else
			{
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
		finally
		{
			
			
		}
		
	}

	@Override
	public boolean beginTransToCache(BalanceTransRunning balanceTransRunning,long durations) {
		// TODO Auto-generated method stub
		return getUserTransLock(balanceTransRunning,durations * 1000,this.LOCK_TRY_INTERVAL,durations);
		
	}
    /*
	@Override
	public int commit(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub  isLockByOwner
		// TODO Auto-generated method stub
		//判读是否是自己的锁
		boolean isLockOk = isLockByOwner(balanceTransRunning);
		if(isLockOk)
		{
			//更新内存
			boolean setResult=setCacheValue(balanceTransRunning);
			//如果更新成功
			if(setResult)
			{
				//释放锁
				this.releaseUserTransLock(balanceTransRunning);
				return BalanceServiceConst.Cache_r_succ;
			}
			return BalanceServiceConst.Cache_r_updateError;
		}
		return BalanceServiceConst.Cache_r_lockByOthers;
	}
*/
	@Override
	public int endTransToCache(BalanceTransRunning balanceTransRunning) {
		// TODO Auto-generated method stub
		boolean isLockOk = isLockByOwner(balanceTransRunning);
		if(isLockOk)
		{
			
				//释放锁
			this.releaseUserTransLock(balanceTransRunning);
			return BalanceServiceConst.Cache_r_succ;
			
		}
		return BalanceServiceConst.Cache_r_lockByOthers;
	}
	@Override
	public UserBalance getUserBalance(long userId) {
		// TODO Auto-generated method stub
		String userBalKey = buildUserBalKey(userId);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		UserBalance cacheUserBalance = (UserBalance)opsForValue.get(userBalKey);
		return cacheUserBalance;
	}
	
	@Override
	public boolean setUserBalance(BalanceTransRunning balanceTransRunning,UserBalance userBalance) {
		// TODO Auto-generated method stub
				boolean isGotLock =false;
				try {
					isGotLock =  isLockByOwner(balanceTransRunning);
					if(isGotLock)
					{
						return setCacheValue(userBalance);
					}
					else
					{
						return false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}		
				finally
				{
					
					
				}
			
	}


	


	@Override
	public boolean delUserBalance(long userId) {
		// TODO Auto-generated method stub
		String userBalKey = buildUserBalKey(userId);
		redisTemplate.delete(userBalKey);		
		return true;
	}

}
