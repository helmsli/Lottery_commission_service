package com.xinwei.commission.controller.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xinwei.lotteryDb.controller.rest.UpdateBalRequest;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;

@FeignClient(value = "credit-writedb")
@RequestMapping("/userbalance")
public interface RestServiceBalance {
	@PostMapping(value = "/updateBalance")
	public  UserBalanceApplyResult updateBlance(@RequestBody UpdateBalRequest updateBalRequest);
	@PostMapping(value = "/queryTransaction")
	public	UserBalanceApplyResult queryTransaction(@RequestBody UserBalanceApply userBalanceApply);
}
