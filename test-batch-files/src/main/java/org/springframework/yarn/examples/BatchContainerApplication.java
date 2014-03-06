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

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.hadoop.fs.HdfsResourceLoader;
import org.springframework.yarn.batch.config.EnableYarnRemoteBatchProcessing;
import org.springframework.yarn.batch.item.HdfsFileSplitItemReader;
import org.springframework.yarn.batch.item.PassThroughLineDataMapper;

@Configuration
@EnableAutoConfiguration
@EnableYarnRemoteBatchProcessing
public class BatchContainerApplication {

	@Autowired
	private StepBuilderFactory stepBuilder;

	@Autowired
	private org.apache.hadoop.conf.Configuration configuration;

	@Bean
	protected Tasklet tasklet() {
		return new PrintTasklet("Hello");
	}

	@Bean
	protected Step remoteStep() throws Exception {
		return stepBuilder
				.get("remoteStep")
				.<String,String>chunk(1000)
				.reader(itemReader(null, null))
				.writer(itemWriter())
				.build();
	}

	// we can't return it as ItemReader, open not called then!!

	@Bean
	@StepScope
	protected HdfsFileSplitItemReader<String> itemReader(
//			@Value("#{stepExecutionContext['fileName']}") Resource fileName,
			@Value("#{stepExecutionContext['splitStart']}") Long splitStart,
			@Value("#{stepExecutionContext['splitLength']}") Long splitLength
			) {
		HdfsFileSplitItemReader<String> itemReader = new HdfsFileSplitItemReader<String>();
		HdfsResourceLoader loader = new HdfsResourceLoader(configuration);
		Resource resource = loader.getResource("/syarn-tmp/batch-files/set1/data.txt");

		itemReader.setResource(resource);
		itemReader.setSplitStart(splitStart);
		itemReader.setSplitLength(splitLength);
		itemReader.setLineDataMapper(new PassThroughLineDataMapper());
		return itemReader;
	}

	@Bean
	protected ItemWriter<String> itemWriter() {
		return new LoggingItemWriter();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchContainerApplication.class, args);
	}

}
