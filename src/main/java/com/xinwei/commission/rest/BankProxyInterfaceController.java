package com.xinwei.commission.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinwei.coobill.bankproxy.domain.BankProxyRequest;
import com.xinwei.coobill.bankproxy.domain.BankProxyResponse;
import com.xinwei.coobill.bankproxy.facade.BankProxyInterface;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月16日 上午10:59:40
 * 
 */
@RestController
@RequestMapping("/bankProxyInterface")
public class BankProxyInterfaceController {
	@Autowired
	@Qualifier("bankProxyInterface")
	private BankProxyInterface bankProxyInterface;

	@PostMapping("/initialiseTransaction")
	public BankProxyResponse initialiseTransaction(@RequestBody BankProxyRequest arg0) {
		return bankProxyInterface.initialiseTransaction(arg0);
	}

	@PostMapping("/queryBankBalance")
	public BankProxyResponse queryBankBalance(@RequestBody BankProxyRequest arg0) {
		return bankProxyInterface.queryBankBalance(arg0);
	}

	@PostMapping("/verifyTransaction")
	public BankProxyResponse verifyTransaction(@RequestBody BankProxyRequest arg0) {
		return bankProxyInterface.verifyTransaction(arg0);
	}

}
