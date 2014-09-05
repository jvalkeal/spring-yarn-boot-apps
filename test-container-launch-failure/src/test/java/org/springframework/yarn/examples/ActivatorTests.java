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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests;
import org.springframework.yarn.test.context.MiniYarnClusterTest;
import org.springframework.yarn.test.junit.ApplicationInfo;
import org.springframework.yarn.test.support.ContainerLogUtils;

@MiniYarnClusterTest
public class ActivatorTests extends AbstractBootYarnClusterTests {

	@Test
	public void testAppSubmission() throws Exception {

		String[] args = new String[] {
				"--spring.yarn.client.files[0]=file:build/libs/test-container-launch-failure-appmaster-2.0.0.BUILD-SNAPSHOT.jar",
				"--spring.yarn.client.files[1]=file:build/libs/test-container-launch-failure-container-2.0.0.BUILD-SNAPSHOT.jar" };

		ApplicationInfo info = submitApplicationAndWait(ActivatorClientApplication.class, args, 2, TimeUnit.MINUTES);
		assertThat(info.getYarnApplicationState(), is(YarnApplicationState.FINISHED));
		assertThat(info.getFinalApplicationStatus(), is(FinalApplicationStatus.FAILED));

		List<Resource> resources = ContainerLogUtils.queryContainerLogs(getYarnCluster(), info.getApplicationId());
		assertThat(resources, notNullValue());
		assertThat(resources.size(), is(4));

		for (Resource res : resources) {
			File file = res.getFile();
			String content = ContainerLogUtils.getFileContent(file);
			if (file.getName().endsWith("Appmaster.stdout")) {
				assertThat(file.length(), greaterThan(0l));
			} else if (file.getName().endsWith("Appmaster.stderr")) {
				assertThat("stderr file is not empty: " + content, file.length(), is(0l));
			} else if (file.getName().endsWith("Container.stdout")) {
				assertThat("stdout file is not empty: " + content, file.length(), is(0l));
			} else if (file.getName().endsWith("Container.stderr")) {
				assertThat(content, containsString("Unable to access jarfile"));
			}
		}
	}

}
