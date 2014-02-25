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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests.EmptyConfig;
import org.springframework.yarn.test.context.MiniYarnCluster;
import org.springframework.yarn.test.context.YarnDelegatingSmartContextLoader;
import org.springframework.yarn.test.junit.ApplicationInfo;
import org.springframework.yarn.test.support.ContainerLogUtils;

@ContextConfiguration(loader = YarnDelegatingSmartContextLoader.class, classes = EmptyConfig.class)
@MiniYarnCluster
public class BatchTests extends AbstractBootYarnClusterTests {

	@Test
	public void testAppSubmission() throws Exception {
		String[] args = new String[] {
				"--spring.yarn.client.files[0]=file:build/libs/test-batch-restart-db-appmaster-2.0.0.BUILD-SNAPSHOT.jar",
				"--spring.yarn.client.files[1]=file:build/libs/test-batch-restart-db-container-2.0.0.BUILD-SNAPSHOT.jar" };

		@SuppressWarnings("resource")
		FsShell shell = new FsShell(getConfiguration());
		shell.touchz("/tmp/remoteStep1partition0");
		shell.touchz("/tmp/remoteStep1partition1");

		ApplicationInfo info1 = submitApplicationAndWait(BatchClientApplication.class, args, 3, TimeUnit.MINUTES);
		assertThat(info1.getYarnApplicationState(), is(YarnApplicationState.FINISHED));

		List<Resource> resources1 = ContainerLogUtils.queryContainerLogs(getYarnCluster(), info1.getApplicationId());
		assertThat(resources1, notNullValue());
		assertThat(resources1.size(), is(10));

		int ok = 0;
		int fail = 0;
		for (Resource res : resources1) {
			File file = res.getFile();
			if (file.getName().endsWith("stdout")) {
				// there has to be some content in stdout file
				assertThat(file.length(), greaterThan(0l));
				if (file.getName().equals("Container.stdout")) {
					Scanner scanner = new Scanner(file);
					String content = scanner.useDelimiter("\\A").next();
					scanner.close();
					// this is what container will log in stdout
					assertThat(content, containsString("Hello from HdfsTasklet"));
					if (content.contains("Hello from HdfsTasklet ok")) {
						ok++;
					}
					if (content.contains("Hello from HdfsTasklet fail")) {
						fail++;
					}
				}
			} else if (file.getName().endsWith("stderr")) {
				String content = "";
				if (file.length() > 0) {
					Scanner scanner = new Scanner(file);
					content = scanner.useDelimiter("\\A").next();
					scanner.close();
				}
				// can't have anything in stderr files
				assertThat("stderr file is not empty: " + content, file.length(), is(0l));
			}
		}
		assertThat("Failed to find ok's from logs", ok, is(2));
		assertThat("Failed to find fail's from logs", fail, is(2));

		shell.touchz("/tmp/remoteStep2partition0");
		shell.touchz("/tmp/remoteStep2partition1");

		ApplicationInfo info2 = submitApplicationAndWait(BatchClientApplication.class, args, 3, TimeUnit.MINUTES);
		assertThat(info2.getYarnApplicationState(), is(YarnApplicationState.FINISHED));

		List<Resource> resources2 = ContainerLogUtils.queryContainerLogs(getYarnCluster(), info2.getApplicationId());
		assertThat(resources2, notNullValue());
		assertThat(resources2.size(), is(6));

		ok = 0;
		fail = 0;
		for (Resource res : resources2) {
			File file = res.getFile();
			if (file.getName().endsWith("stdout")) {
				// there has to be some content in stdout file
				assertThat(file.length(), greaterThan(0l));
				if (file.getName().equals("Container.stdout")) {
					Scanner scanner = new Scanner(file);
					String content = scanner.useDelimiter("\\A").next();
					scanner.close();
					// this is what container will log in stdout
					assertThat(content, containsString("Hello from HdfsTasklet"));
					if (content.contains("Hello from HdfsTasklet ok")) {
						ok++;
					}
					if (content.contains("Hello from HdfsTasklet fail")) {
						fail++;
					}
				}
			} else if (file.getName().endsWith("stderr")) {
				String content = "";
				if (file.length() > 0) {
					Scanner scanner = new Scanner(file);
					content = scanner.useDelimiter("\\A").next();
					scanner.close();
				}
				// can't have anything in stderr files
				assertThat("stderr file is not empty: " + content, file.length(), is(0l));
			}
		}
		assertThat("Failed to find ok's from logs", ok, is(2));
		assertThat("Found fail's from logs", fail, is(0));
	}

}
