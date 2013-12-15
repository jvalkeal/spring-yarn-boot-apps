/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.yarn.examples;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Spring Boot main definition for Yarn Appmaster.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableBatchProcessing
@ImportResource("appmaster-context.xml")
public class BatchAppmasterApplication {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobBuilderFactory jobFactory;

	@Autowired
	private StepBuilderFactory stepFactory;

//	@Bean
//	public YarnEventPublisher yarnEventPublisher() {
//		return new DefaultYarnEventPublisher();
//	}
//
//	@Bean
//	public AppmasterService batchService() {
//		BatchAppmasterService service = new BatchAppmasterService();
//		JobRepositoryService remoteService = new JobRepositoryService();
//		service.setJobRepositoryRemoteService(remoteService);
//		return service;
//	}
//
//

//	@Bean
//	protected Tasklet tasklet() {
//		return new Tasklet() {
//			@Override
//			public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
//				return RepeatStatus.FINISHED;
//			}
//		};
//	}

//	@Bean
//	public Job job() throws Exception {
////		return jobFactory.get("job").start(step1()).build();
//		return jobFactory.get("job").start(master1()).next(master2()).build();
//	}
//
//	@Bean
//	protected Step master1() throws Exception {
//		return stepFactory.get("master1").tasklet(null).build();
//	}
//
//	@Bean
//	protected Step master2() throws Exception {
//		return stepFactory.get("master2").tasklet(null).build();
//	}


	public static void main(String[] args) {
		SpringApplication.run(BatchAppmasterApplication.class, args);

//		System.exit(SpringApplication.exit(SpringApplication.run(
//                SampleBatchApplication.class, args)));

	}

}
