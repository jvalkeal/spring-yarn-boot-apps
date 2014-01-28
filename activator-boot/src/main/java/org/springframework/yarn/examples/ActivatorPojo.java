/*
 * Copyright 2014 the original author or authors.
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
import org.springframework.yarn.annotation.YarnContainer;
import org.springframework.yarn.annotation.YarnContainerActivator;

/**
 * A simple pojo to demonstrate container activator concept.
 * @{@code YarnContainer} makes this class as stereotype candidate for
 * something you'd execute on yarn, @{@code YarnContainerActivator}
 * makes a method candidate as what this class would actually do if
 * executed on Hadoop.
 *
 * @author Janne Valkealahti
 *
 */
@YarnContainer
public class ActivatorPojo {

	private static final Log log = LogFactory.getLog(ActivatorPojo.class);

	@YarnContainerActivator
	public void publicVoidNoArgsMethod() {
		log.info("Hello from ActivatorPojo");
	}

}
