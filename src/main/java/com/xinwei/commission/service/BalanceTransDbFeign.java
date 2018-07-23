package com.xinwei.commission.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.commission.service.impl.BalanceTransDbFeignCallBack;
import com.xinwei.nnl.common.domain.ProcessResult;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月18日 上午10:18:08
 * 
 */
@FeignClient(value = "lottery-commaccess-db", fallback = BalanceTransDbFeignCallBack.class)
public interface BalanceTransDbFeign {
	@PostMapping("/balanceTransDb/insertBalanceTransRunning")
	ProcessResult insertBalanceTransRunning(@RequestBody BalanceTransRunning balanceTransRunning);

	@PostMapping("/balanceTransDb/selectBalanceTransRunning")
	ProcessResult selectBalanceTransRunning(@RequestBody BalanceTransRunning balanceTransRunning);
	//	List<BalanceTransRunning> selectBalanceTransRunning(@RequestBody BalanceTransRunning balanceTransRunning);

	@PostMapping("/balanceTransDb/updateBalanceTransRunning")
	ProcessResult updateBalanceTransRunning(@RequestBody BalanceTransRunning balanceTransRunning);

	@PostMapping("/balanceTransDb/deleteBalanceTransRunning")
	ProcessResult deleteBalanceTransRunning(@RequestBody BalanceTransRunning balanceTransRunning);
}
