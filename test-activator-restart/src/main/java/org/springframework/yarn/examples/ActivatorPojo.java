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
import org.springframework.yarn.YarnSystemConstants;
import org.springframework.yarn.annotation.OnYarnContainerStart;
import org.springframework.yarn.annotation.YarnContainer;
import org.springframework.yarn.annotation.YarnEnvironment;
import org.springframework.yarn.launch.ExitStatus;

@YarnContainer
public class ActivatorPojo {

	private static final Log log = LogFactory.getLog(ActivatorPojo.class);

	@OnYarnContainerStart
	public String publicMethod(@YarnEnvironment(YarnSystemConstants.SYARN_CONTAINER_ID) String containerIdString) {
		log.info("Hello from ActivatorPojo");
		log.info("SYARN_CONTAINER_ID=" + containerIdString);
		int containerId = Integer.parseInt(containerIdString.substring(containerIdString.length()-1));

		// We just use the container id found from token variable
		// to fail first container (with id 2)
		if ((containerId == 2)) {
			log.info("Exiting with error");
			return ExitStatus.FAILED.getExitCode();
		} else {
			log.info("Exiting with ok");
			return ExitStatus.COMPLETED.getExitCode();
		}

	}

}