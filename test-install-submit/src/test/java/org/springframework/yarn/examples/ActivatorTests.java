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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.yarn.boot.app.YarnPushApplication;
import org.springframework.yarn.boot.app.YarnSubmitApplication;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests;
import org.springframework.yarn.boot.test.junit.AbstractBootYarnClusterTests.EmptyConfig;
import org.springframework.yarn.client.YarnClient;
import org.springframework.yarn.client.YarnClientFactoryBean;
import org.springframework.yarn.test.context.MiniYarnCluster;
import org.springframework.yarn.test.context.YarnDelegatingSmartContextLoader;
import org.springframework.yarn.test.support.ContainerLogUtils;

@ContextConfiguration(loader = YarnDelegatingSmartContextLoader.class, classes = EmptyConfig.class)
@MiniYarnCluster
public class ActivatorTests extends AbstractBootYarnClusterTests {

	private static final Log log = LogFactory.getLog(ActivatorTests.class);

	@Test
	public void testAppInstallSubmit() throws Exception {

		String ID = "foo-1";
		String BASE = "/apps/";
		setYarnClient(buildYarnClient());

		String[] installAppArgs = new String[] {
				"--spring.hadoop.fsUri=" + getConfiguration().get("fs.defaultFS"),
				"--spring.hadoop.resourceManagerAddress=" + getConfiguration().get("yarn.resourcemanager.address"),
				"--spring.yarn.client.files[0]=file:build/libs/test-install-submit-appmaster-2.0.0.BUILD-SNAPSHOT.jar",
				"--spring.yarn.client.files[1]=file:build/libs/test-install-submit-container-2.0.0.BUILD-SNAPSHOT.jar"
				};
		Properties appProperties = new Properties();
		appProperties.setProperty("spring.yarn.applicationDir", BASE + ID + "/");
		YarnPushApplication installApp = new YarnPushApplication();
		installApp.applicationVersion(ID);
		installApp.applicationBaseDir(BASE);
		installApp.configFile("application.properties", appProperties);
		installApp.run(installAppArgs);
		listFiles();
		catFile(BASE + ID + "/application.properties");

		String[] submitAppArgs = new String[] {
				"--spring.yarn.applicationDir=" + BASE + ID + "/",
				"--spring.hadoop.fsUri=" + getConfiguration().get("fs.defaultFS"),
				"--spring.hadoop.resourceManagerAddress=" + getConfiguration().get("yarn.resourcemanager.address"),
				"--spring.hadoop.resourceManagerSchedulerAddress=" + getConfiguration().get("yarn.resourcemanager.scheduler.address")
				};
		YarnSubmitApplication submitApp = new YarnSubmitApplication();
		submitApp.applicationVersion(ID);
		submitApp.applicationBaseDir(BASE);
		ApplicationId applicationId = submitApp.run(submitAppArgs);

		YarnApplicationState state = waitState(applicationId, 1, TimeUnit.MINUTES, YarnApplicationState.FINISHED);
		assertThat(state, is(YarnApplicationState.FINISHED));

		List<Resource> resources = ContainerLogUtils.queryContainerLogs(getYarnCluster(), applicationId);
		assertThat(resources, notNullValue());
		assertThat(resources.size(), is(6));

		for (Resource res : resources) {
			File file = res.getFile();
			String content = ContainerLogUtils.getFileContent(file);
			if (file.getName().endsWith("stdout")) {
				assertThat(file.length(), greaterThan(0l));
				if (file.getName().equals("Container.stdout")) {
					assertThat(content, containsString("Hello from ActivatorPojo"));
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

	private void catFile(String uri) {
		FsShell shell = new FsShell(configuration);
		Collection<String> text = shell.text(uri);
		log.info("XXX cat files");
		for (String t : text) {
			log.info(t);
		}
		try {
			shell.close();
		} catch (IOException e) {
		}
	}

	private YarnClient buildYarnClient() throws Exception {
		YarnClientFactoryBean factory = new YarnClientFactoryBean();
		factory.setConfiguration(getConfiguration());
		factory.afterPropertiesSet();
		return factory.getObject();
	}


}
