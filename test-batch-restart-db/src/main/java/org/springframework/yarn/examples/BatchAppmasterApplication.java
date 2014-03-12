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
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.batch.am.BatchYarnAppmaster;
import org.springframework.yarn.batch.config.EnableYarnBatchProcessing;
import org.springframework.yarn.batch.partition.StaticPartitionHandler;

@Configuration
@EnableAutoConfiguration
@EnableYarnBatchProcessing
public class BatchAppmasterApplication {

	@Autowired
	private JobBuilderFactory jobFactory;

	@Autowired
	private StepBuilderFactory stepFactory;

	@Autowired
	private YarnAppmaster yarnAppmaster;

	@Bean
	public Job job() throws Exception {
		return jobFactory.get("job")
				.incrementer(jobParametersIncrementer())
				.start(master1())
				.next(master2())
				.build();
	}

	@Bean
	public JobParametersIncrementer jobParametersIncrementer() {
		return new RunIdIncrementer();
	}

	@Bean
	protected Step master1() throws Exception {
		return stepFactory.get("master1")
				.partitioner("remoteStep1", partitioner())
				.partitionHandler(partitionHandler("remoteStep1"))
				.build();
	}

	@Bean
	protected Step master2() throws Exception {
		return stepFactory.get("master2")
				.partitioner("remoteStep2", partitioner())
				.partitionHandler(partitionHandler("remoteStep2"))
				.build();
	}

	@Bean
	protected Partitioner partitioner() {
		return new SimplePartitioner();
	}

	protected PartitionHandler partitionHandler(String stepName) {
		StaticPartitionHandler handler = new StaticPartitionHandler((BatchYarnAppmaster)yarnAppmaster);
		handler.setStepName(stepName);
		handler.setGridSize(2);
		return handler;
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchAppmasterApplication.class, new String[]{"foo=jee"});
	}

}
