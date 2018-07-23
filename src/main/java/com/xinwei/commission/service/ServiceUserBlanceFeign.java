package com.xinwei.commission.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.xinwei.commission.service.impl.ServiceUserBlanceFeignCallBack;
import com.xinwei.lotteryDb.controller.rest.UpdateBalRequest;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月18日 上午10:39:09
 * 
 */
@FeignClient(value = "credit-writedb", fallback = ServiceUserBlanceFeignCallBack.class)
public interface ServiceUserBlanceFeign {
	@PostMapping(value = "/serviceUserBlance/updateBalance")
	UserBalanceApplyResult updateUserBalance(@RequestBody UpdateBalRequest updateBalRequest);

	@PostMapping(value = "/serviceUserBlance/queryTransaction")
	UserBalanceApplyResult queryTransaction(@RequestBody UserBalanceApply userBalanceApply);
}
