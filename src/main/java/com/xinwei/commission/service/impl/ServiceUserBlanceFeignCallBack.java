package com.xinwei.commission.service.impl;

import org.springframework.stereotype.Service;

import com.xinwei.lotteryDb.domain.UserBalance;
import com.xinwei.lotteryDb.domain.UserBalanceApply;
import com.xinwei.lotteryDb.domain.UserBalanceApplyResult;
import com.xinwei.lotteryDb.service.ServiceUserBlance;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月14日 下午4:46:10
 * 
 */
@Service
public class ServiceUserBlanceFeignCallBack implements ServiceUserBlance {
	//	@Value("${serviceUserBlance.rest-url:http://172.18.10.73:8077/serviceUserBlance}")
	//	private String restUrl;

	//	@Autowired
	//	private RestTemplate restTemplate;

	/* (non-Javadoc)
	 * @see com.xinwei.lotteryDb.service.ServiceUserBlance#updateUserBalance(com.xinwei.lotteryDb.domain.UserBalance, com.xinwei.lotteryDb.domain.UserBalanceApply)
	 */
	@Override
	public UserBalanceApplyResult updateUserBalance(UserBalance nowUseBalance, UserBalanceApply userBalanceApply) {
		//		UpdateBalRequest updateBalRequest = new UpdateBalRequest();
		//		updateBalRequest.setNowUseBalance(nowUseBalance);
		//		updateBalRequest.setUserBalanceApply(userBalanceApply);
		//		UserBalanceApplyResult result = restTemplate.postForObject(restUrl + "/updateBalance", updateBalRequest,
		//				UserBalanceApplyResult.class);
		//		return result;
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinwei.lotteryDb.service.ServiceUserBlance#queryTransaction(com.xinwei.lotteryDb.domain.UserBalanceApply)
	 */
	@Override
	public UserBalanceApplyResult queryTransaction(UserBalanceApply userBalanceApply) {
		//		UserBalanceApplyResult result = restTemplate.postForObject(restUrl + "/queryTransaction", userBalanceApply,
		//				UserBalanceApplyResult.class);
		//		return result;
		return null;
	}

}
