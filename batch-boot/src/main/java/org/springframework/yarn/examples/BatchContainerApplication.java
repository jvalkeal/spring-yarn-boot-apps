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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.yarn.YarnSystemConstants;
import org.springframework.yarn.container.YarnContainer;

/**
 * Batch container definition for Spring Boot Application.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
@EnableAutoConfiguration
@ImportResource("container-context.xml")
public class BatchContainerApplication {

	@Bean(name=YarnSystemConstants.DEFAULT_ID_CONTAINER_CLASS)
	public Class<? extends YarnContainer> yarnContainerClass() {
//		return DefaultBatchYarnContainer.class;
		return BatchContainer.class;
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchContainerApplication.class, args);
	}

}
