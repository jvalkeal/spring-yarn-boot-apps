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

import java.io.IOException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.store.split.Splitter;
import org.springframework.data.hadoop.store.split.StaticLengthSplitter;
import org.springframework.yarn.batch.BatchSystemConstants;
import org.springframework.yarn.batch.config.EnableYarnBatchProcessing;
import org.springframework.yarn.batch.partition.SplitterPartitionHandler;
import org.springframework.yarn.batch.partition.SplitterPartitioner;

@Configuration
@EnableAutoConfiguration
@EnableYarnBatchProcessing
public class BatchAppmasterApplication {

	@Autowired
	private JobBuilderFactory jobFactory;

	@Autowired
	private StepBuilderFactory stepFactory;

	@Bean
	public Job job() throws Exception {
		return jobFactory.get("job")
				.incrementer(jobParametersIncrementer())
				.start(master())
				.build();
	}

	@Bean
	public JobParametersIncrementer jobParametersIncrementer() {
		return new RunIdIncrementer();
	}

	@Bean
	protected Step master() throws Exception {
		return stepFactory
				.get("master")
				.partitioner("remoteStep", partitioner(null))
				.partitionHandler(partitionHandler())
				.build();
	}

	@Bean
	@StepScope
	protected Partitioner partitioner(
			@Value(BatchSystemConstants.JP_SPEL_KEY_INPUTPATTERNS) String inputPatterns
			) throws IOException {
		SplitterPartitioner partitioner = new SplitterPartitioner();
		partitioner.setSplitter(splitter());
		partitioner.setInputPatterns(inputPatterns);
		return partitioner;
	}

	@Bean
	protected Splitter splitter() {
		StaticLengthSplitter splitter = new StaticLengthSplitter(1000);
		return splitter;
	}

	@Bean
	protected PartitionHandler partitionHandler() {
		SplitterPartitionHandler handler = new SplitterPartitionHandler();
		handler.setStepName("remoteStep");
		return handler;
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchAppmasterApplication.class, args);
	}

}
