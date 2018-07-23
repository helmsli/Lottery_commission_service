package com.xinwei.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.xinwei.nnl.common.util.NNLoggerFactory;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
//容错的
@EnableHystrix
@EnableHystrixDashboard
@Configuration
@EnableAutoConfiguration
@ComponentScan("com.xinwei.commission")
//@ImportResource ({ "classpath:hessian/hessian-client.xml", "classpath:hessian/hessian-server.xml" })
public class CommissionServiceApplication {

	public static void main(String[] args) {
		NNLoggerFactory.MODULE_NAME = "plateform_service";
		SpringApplication.run(CommissionServiceApplication.class, args);
	}
}
