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
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
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

@ContextConfiguration(loader=YarnDelegatingSmartContextLoader.class, classes=EmptyConfig.class)
@MiniYarnCluster
public class MultiContextTests extends AbstractBootYarnClusterTests {

	private final static Log log = LogFactory.getLog(MultiContextTests.class);

	@Test
	public void testAppSubmission() throws Exception {
		// need to override default paths in application.yml
		String[] args = new String[]{
				"--spring.yarn.client.files[0]=file:build/libs/test-staging-global-appmaster-2.0.0.BUILD-SNAPSHOT.jar",
				"--spring.yarn.client.files[1]=file:build/libs/test-staging-global-container-2.0.0.BUILD-SNAPSHOT.jar"
		};

		ApplicationInfo info = submitApplicationAndWait(MultiContextClientApplication.class, args, 2, TimeUnit.MINUTES);
		assertThat(info.getYarnApplicationState(), is(YarnApplicationState.FINISHED));

		listFiles();

		Path stagingBaseDir = new Path("/tmp/test-staging-global");
		assertThat(stagingBaseDir.getFileSystem(configuration).getFileStatus(stagingBaseDir).isDirectory(), is(true));

		List<Resource> resources = ContainerLogUtils.queryContainerLogs(getYarnCluster(), info.getApplicationId());
		assertThat(resources, notNullValue());
		assertThat(resources.size(), is(4));

		for (Resource res : resources) {
			File file = res.getFile();
			String content = ContainerLogUtils.getFileContent(file);
			if (file.getName().endsWith("stdout")) {
				// there has to be some content in stdout file
				assertThat(file.length(), greaterThan(0l));
				if (file.getName().equals("Container.stdout")) {
					assertThat(content, containsString("Hello from MultiContextContainer"));
				}
			} else if (file.getName().endsWith("stderr")) {
				assertThat("stderr file is not empty: " + content, file.length(), is(0l));
			}
		}
	}

	private void listFiles() {
		@SuppressWarnings("resource")
		FsShell shell = new FsShell(configuration);
		for (FileStatus s : shell.ls(true, "/")) {
			log.info(s);
		}
	}

}
