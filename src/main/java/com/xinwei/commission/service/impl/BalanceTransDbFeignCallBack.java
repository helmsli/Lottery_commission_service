package com.xinwei.commission.service.impl;

import org.springframework.stereotype.Service;

import com.xinwei.commAccessDb.domain.BalanceTransRunning;
import com.xinwei.nnl.common.domain.ProcessResult;

/**
 * @notes 
 * 
 * @author wangjiamin
 * 
 * @version 2018年7月14日 下午4:45:11
 * 
 */
@Service
public class BalanceTransDbFeignCallBack {
	//	private static final Logger logger = LoggerFactory.getLogger(BalanceTransDbFeignCallBack.class);
	//
	//	private final int SUCCESS = 0;
	//
	private final int FAILURE = -1;
	//
	//	@Value("${balanceTransDb.rest-url:http://172.18.10.73:8087/balanceTransDb}")
	//	private String restUrl;
	//
	//	@Autowired
	//	private RestTemplate restTemplate;

	/* (non-Javadoc)
	 * @see com.xinwei.commAccessDb.service.BalanceTransDb#insertBalanceTransRunning(com.xinwei.commAccessDb.domain.BalanceTransRunning)
	 */
	public ProcessResult insertBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		//		ProcessResult result = restTemplate.postForObject(restUrl + "/insertBalanceTransRunning", balanceTransRunning,
		//				ProcessResult.class);
		//		if (result != null && result.getRetCode() == SUCCESS) {
		//			return (int) result.getResponseInfo();
		//		} else {
		//			logger.error("return result=" + result);
		//		}

		ProcessResult result = new ProcessResult();
		result.setRetCode(FAILURE);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.xinwei.commAccessDb.service.BalanceTransDb#selectBalanceTransRunning(com.xinwei.commAccessDb.domain.BalanceTransRunning)
	 */
	public ProcessResult selectBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		//		ProcessResult result = restTemplate.postForObject(restUrl + "/selectBalanceTransRunning", balanceTransRunning,
		//				ProcessResult.class);
		//		if (result != null && result.getRetCode() == SUCCESS) {
		//			return JsonUtil.fromJson((String) result.getResponseInfo(), new TypeToken<List<BalanceTransRunning>>() {
		//				private static final long serialVersionUID = 1L;
		//			}.getType());
		//		} else {
		//			logger.error("return result=" + result);
		//		}
		ProcessResult result = new ProcessResult();
		result.setRetCode(FAILURE);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.xinwei.commAccessDb.service.BalanceTransDb#updateBalanceTransRunning(com.xinwei.commAccessDb.domain.BalanceTransRunning)
	 */
	public ProcessResult updateBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		//		ProcessResult result = restTemplate.postForObject(restUrl + "/updateBalanceTransRunning", balanceTransRunning,
		//				ProcessResult.class);
		//		if (result != null && result.getRetCode() == SUCCESS) {
		//			return (int) result.getResponseInfo();
		//		} else {
		//			logger.error("return result=" + result);
		//		}
		ProcessResult result = new ProcessResult();
		result.setRetCode(FAILURE);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.xinwei.commAccessDb.service.BalanceTransDb#deleteBalanceTransRunning(com.xinwei.commAccessDb.domain.BalanceTransRunning)
	 */
	public ProcessResult deleteBalanceTransRunning(BalanceTransRunning balanceTransRunning) {
		//		ProcessResult result = restTemplate.postForObject(restUrl + "/deleteBalanceTransRunning", balanceTransRunning,
		//				ProcessResult.class);
		//		if (result != null && result.getRetCode() == SUCCESS) {
		//			return (int) result.getResponseInfo();
		//		} else {
		//			logger.error("return result=" + result);
		//		}
		ProcessResult result = new ProcessResult();
		result.setRetCode(FAILURE);
		return result;
	}

}
