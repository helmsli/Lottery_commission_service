package com.xinwei.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.xinwei.nnl.common.util.NNLoggerFactory;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan ("com.xinwei.commission")
@ImportResource ({ "classpath:hessian/hessian-client.xml", "classpath:hessian/hessian-server.xml" })
public class CommissionServiceApplication
{
	
	public static void main(String[] args)
	{
		NNLoggerFactory.MODULE_NAME = "plateform_service";
		SpringApplication.run(CommissionServiceApplication.class, args);
	}
}
