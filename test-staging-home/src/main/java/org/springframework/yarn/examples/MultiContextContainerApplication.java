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
import org.springframework.yarn.YarnSystemConstants;

/**
 * Multi context container definition for Spring Boot Application.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
@EnableAutoConfiguration
public class MultiContextContainerApplication {

//	@Bean(name=YarnSystemConstants.DEFAULT_ID_CONTAINER_CLASS)
//	public Class<? extends YarnContainer> yarnContainerClass() {
//		return MultiContextContainer.class;
//	}

	@Bean(name=YarnSystemConstants.DEFAULT_ID_CONTAINER_REF)
	public Object yarnContainer() {
		// TODO: fix so that we can return MultiContextContainer instead of Object
		//       fails because of @ConditionalOnMissingBean(YarnContainer.class) on
		//       YarnContainerAutoConfiguration
		return new MultiContextContainer();
	}

	public static void main(String[] args) {
		SpringApplication.run(MultiContextContainerApplication.class, args);
	}

}
