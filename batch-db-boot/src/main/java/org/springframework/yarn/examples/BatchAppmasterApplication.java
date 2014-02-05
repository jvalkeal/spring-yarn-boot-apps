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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.batch.am.AbstractBatchAppmaster;
import org.springframework.yarn.batch.config.EnableYarnBatchProcessing;
import org.springframework.yarn.batch.partition.StaticBatchPartitionHandler;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableBatchProcessing
@EnableYarnBatchProcessing
public class BatchAppmasterApplication {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobBuilderFactory jobFactory;

	@Autowired
	private StepBuilderFactory stepFactory;

	@Autowired
	private YarnAppmaster yarnAppmaster;

//	public BatchAppmaster batchAppmaster() {
//		return new BatchAppmaster();
//	}

	// {"-restart", "-next", "-stop", "-abandon"}

	@Bean
	public Job job() throws Exception {
		return jobFactory.get("job")
				.start(master1())
				.next(master2())
				.build();
	}

	@Bean
	protected Step master1() throws Exception {
		return stepFactory.get("master1")
				.partitioner("remoteStep", partitioner())
				.partitionHandler(partitionHandler())
				.build();
	}

	@Bean
	protected Step master2() throws Exception {
		return stepFactory.get("master2")
				.partitioner("remoteStep", partitioner())
				.partitionHandler(partitionHandler())
				.build();
	}

	@Bean
	protected Partitioner partitioner() {
		return new SimplePartitioner();
	}

	@Bean
	protected PartitionHandler partitionHandler() {
		return new StaticBatchPartitionHandler((AbstractBatchAppmaster) yarnAppmaster, 2);
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchAppmasterApplication.class, new String[]{"--spring.batch.job.enabled=false"});
	}

}
