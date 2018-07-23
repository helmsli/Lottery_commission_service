package com.xinwei.commission.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderpost.domain.CommissionPresentInfo;
import com.xinwei.orderpost.facade.CommissionPresentService;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月16日 上午11:01:14
 * 
 */
@RestController
@RequestMapping("/commissionPresentService")
public class CommissionPresentServiceController {
	@Autowired
	@Qualifier("commissionPresentService")
	private CommissionPresentService commissionPresentService;

	@PostMapping("/presentCommission")
	public ProcessResult presentCommission(@RequestBody List<CommissionPresentInfo> commissionPresentInfoList) {
		return commissionPresentService.presentCommission(commissionPresentInfoList);
	}
}
