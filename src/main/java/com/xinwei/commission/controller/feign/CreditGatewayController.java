package com.xinwei.commission.controller.feign;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinwei.commAccessDb.service.BalanceTransDb;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.nnl.common.domain.ProcessResult;

@RestController
@RequestMapping("/credit")
public class CreditGatewayController {
	@Resource(name="serviceBalanceTransDb")
	private BalanceTransDb balanceTransDb;
	@GetMapping(value = "{userId}/{transactionTime}/{transactionId}/{amount}/{balance}/updateTransaction")
	public	ProcessResult queryTransaction(@RequestBody UserBalanceApply userBalanceApply)
	{
		return null;
	}
	
}
