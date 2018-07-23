package com.xinwei.commission.controller.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinwei.commission.service.BalanceTransDbFeign;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.nnl.common.domain.ProcessResult;

@RestController
@RequestMapping("/credit")
public class CreditGatewayController {
	//	@Resource(name="serviceBalanceTransDb")
	//	@Autowired
	//	private BalanceTransDb balanceTransDb;

	@Autowired
	private BalanceTransDbFeign balanceTransDbFeign;

	@GetMapping(value = "{userId}/{transactionTime}/{transactionId}/{amount}/{balance}/updateTransaction")
	public ProcessResult queryTransaction(@RequestBody UserBalanceApply userBalanceApply) {
		return null;
	}

}
