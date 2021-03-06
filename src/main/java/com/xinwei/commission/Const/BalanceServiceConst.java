package com.xinwei.commission.Const;

import com.xinwei.lotteryDb.Const.UserBalanceApplyConst;

public class BalanceServiceConst {
	/*8950-8999  全闭区间*/
	public static final int Btrans_run_priority_high = 1;
	public static final int Btrans_run_priority_low = 0;
	public static final int Btrans_Lock_Timeout = 30;
	
    /**
     * balanceTransRunning status 的状态数值
     */
	//初值状态
	public static final int Btrans_status_init = 1;
	public static final int Btrans_status_succ = 0;
	
	//错误码
	public static final int Btrans_r_begin = 8950;
	
	//已经被执行，并且信息相同
	public static final int Btrans_r_haveDone = 8950+0;
	//已经被执行，并且信息不相同。
	public static final int Btrans_r_haveDone_error = Btrans_r_begin+1;
	//获取账户余额信息错误
	public static final int Btrans_r_Balance_error = Btrans_r_begin+2;
	//
	public static final int Btrans_r_tooManyRequest = Btrans_r_begin+3;
	/**
	 * 签名错误
	 */
	public static final int Error_signo_error = Btrans_r_begin+4;
	
	
	//内存更新结果
	public static final int Cache_r_succ = 0;
	//提交cache被别人加锁
	public static final int Cache_r_lockByOthers = 1;
	
	public static final int Cache_r_updateError = 2;
	
	
	
	
	
	
	public static boolean haveDone(int status)
	{
		return status==Btrans_status_succ;
	}
   
	public static boolean isHighRunning(int runPriority)
	{
		return Btrans_run_priority_high==runPriority;
	}
}
