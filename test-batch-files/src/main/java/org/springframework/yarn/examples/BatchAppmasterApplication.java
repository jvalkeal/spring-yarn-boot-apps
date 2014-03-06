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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.hadoop.HadoopSystemConstants;
import org.springframework.data.hadoop.fs.CustomResourceLoaderRegistrar;
import org.springframework.data.hadoop.fs.HdfsResourceLoader;
import org.springframework.yarn.am.YarnAppmaster;
import org.springframework.yarn.batch.am.AbstractBatchAppmaster;
import org.springframework.yarn.batch.config.EnableYarnBatchProcessing;
import org.springframework.yarn.batch.partition.HdfsSplitBatchPartitionHandler;
import org.springframework.yarn.batch.partition.MultiHdfsResourcePartitioner;
import org.springframework.yarn.batch.partition.StaticBatchPartitionHandler;

@Configuration
@EnableAutoConfiguration
@EnableYarnBatchProcessing
public class BatchAppmasterApplication {

	private final static Log log = LogFactory.getLog(BatchAppmasterApplication.class);

	@Autowired
	private JobBuilderFactory jobFactory;

	@Autowired
	private StepBuilderFactory stepFactory;

	@Autowired
	private org.apache.hadoop.conf.Configuration configuration;

	@Autowired
	private YarnAppmaster yarnAppmaster;

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
				.partitioner("remoteStep", partitioner())
				.partitionHandler(partitionHandler())
				.build();
	}

	@Bean
	protected Partitioner partitioner() {
		MultiHdfsResourcePartitioner partitioner = new MultiHdfsResourcePartitioner();

		HdfsResourceLoader loader = new HdfsResourceLoader(configuration);
		Resource resource = loader.getResource("/syarn-tmp/batch-files/set1/data.txt");
		partitioner.setResources(new Resource[]{resource});

		partitioner.setSplitSize(2);
		partitioner.setForceSplit(true);
		partitioner.setConfiguration(configuration);
		return partitioner;
	}

	@Bean
	@StepScope
	protected PartitionHandler partitionHandler() {
		HdfsSplitBatchPartitionHandler handler = new HdfsSplitBatchPartitionHandler();
		handler.setStepName("remoteStep");
		handler.setConfiguration(configuration);
		return handler;
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchAppmasterApplication.class, args);
	}

}
