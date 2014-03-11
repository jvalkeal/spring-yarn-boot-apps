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

import static org.springframework.yarn.batch.BatchSystemConstants.SEC_SPEL_KEY_FILENAME;
import static org.springframework.yarn.batch.BatchSystemConstants.SEC_SPEL_KEY_SPLITLENGTH;
import static org.springframework.yarn.batch.BatchSystemConstants.SEC_SPEL_KEY_SPLITSTART;

import org.apache.hadoop.fs.Path;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.store.DataStoreReader;
import org.springframework.data.hadoop.store.input.TextFileReader;
import org.springframework.data.hadoop.store.split.GenericSplit;
import org.springframework.data.hadoop.store.split.Split;
import org.springframework.yarn.batch.config.EnableYarnRemoteBatchProcessing;
import org.springframework.yarn.batch.item.DataStoreItemReader;
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
	protected Step remoteStep() throws Exception {
		return stepBuilder
				.get("remoteStep")
				.<String,String>chunk(1000)
				.reader(itemReader(null, null, null))
				.writer(itemWriter())
				.build();
	}

	@Bean
	@StepScope
	protected DataStoreItemReader<String> itemReader(
			@Value(SEC_SPEL_KEY_FILENAME) String fileName,
			@Value(SEC_SPEL_KEY_SPLITSTART) Long splitStart,
			@Value(SEC_SPEL_KEY_SPLITLENGTH) Long splitLength
			) {
		Split split = new GenericSplit(splitStart, splitLength, null);
		DataStoreReader<String> reader = new TextFileReader(configuration, new Path(fileName), null, split, null);
		DataStoreItemReader<String> itemReader = new DataStoreItemReader<String>();
		itemReader.setDataStoreReader(reader);
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
