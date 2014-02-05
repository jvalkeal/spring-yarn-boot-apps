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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.yarn.client.YarnClient;
import org.springframework.yarn.test.context.YarnCluster;
import org.springframework.yarn.test.support.ClusterInfo;
import org.springframework.yarn.test.support.ContainerLogUtils;
import org.springframework.yarn.test.support.YarnClusterManager;

/**
 * Tests for multi context example. We're checking that
 * application status is ok and log files looks
 * what is expected.
 *
 * @author Janne Valkealahti
 *
 */
public class MultiContextBootClientTests  {

	public static class TestInitializer implements ApplicationContextInitializer {

		Configuration configuration;

		public TestInitializer(Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			applicationContext.getBeanFactory().registerSingleton("miniYarnConfiguration", configuration);
		}

	}

	@Test
	public void testAppSubmission() throws Exception {

		YarnClusterManager manager = YarnClusterManager.getInstance();
		YarnCluster cluster = manager.getCluster(new ClusterInfo());
		cluster.start();
		Configuration configuration = cluster.getConfiguration();

		String[] args = new String[]{
				"--spring.yarn.fsUri="+configuration.get("fs.defaultFS"),
				"--spring.yarn.rmAddress="+configuration.get("yarn.resourcemanager.address"),
				"--spring.yarn.schedulerAddress="+configuration.get("yarn.resourcemanager.scheduler.address"),
				"--spring.yarn.client.files[0]=file:build/libs/multi-context-boot-appmaster-2.0.0.BUILD-SNAPSHOT.jar",
				"--spring.yarn.client.files[1]=file:build/libs/multi-context-boot-container-2.0.0.BUILD-SNAPSHOT.jar"
		};

		System.out.println("ZZZZ: " + configuration.get("yarn.resourcemanager.scheduler.address"));
		ConfigurableApplicationContext context = new SpringApplicationBuilder(MultiContextClientApplication.class)
			.initializers(new TestInitializer(configuration))
			.run(args);

		YarnClient client = context.getBean(YarnClient.class);
		ApplicationId applicationId = client.submitApplication();
		assertThat(applicationId, notNullValue());

		YarnApplicationState waitState = waitState(client, applicationId, 70, TimeUnit.SECONDS, YarnApplicationState.FINISHED, YarnApplicationState.FAILED);
		assertThat(waitState, is(YarnApplicationState.FINISHED));

		List<Resource> resources = ContainerLogUtils.queryContainerLogs(cluster, applicationId);
		manager.close();
		context.close();


		// appmaster and 4 containers should
		// make it 10 log files
		assertThat(resources, notNullValue());
		assertThat(resources.size(), is(6));

		for (Resource res : resources) {
			File file = res.getFile();
			if (file.getName().endsWith("stdout")) {
				// there has to be some content in stdout file
				assertThat(file.length(), greaterThan(0l));
				if (file.getName().equals("Container.stdout")) {
					Scanner scanner = new Scanner(file);
					String content = scanner.useDelimiter("\\A").next();
					scanner.close();
					// this is what container will log in stdout
					assertThat(content, containsString("Hello from MultiContextContainer"));
				}
			} else if (file.getName().endsWith("stderr")) {
				String content = "";
				if (file.length() > 0) {
					Scanner scanner = new Scanner(file);
					content = scanner.useDelimiter("\\A").next();
					scanner.close();
				}
				// can't have anything in stderr files
//				assertThat("stderr file is not empty: " + content, file.length(), is(0l));
			}
		}
	}

	protected YarnApplicationState waitState(YarnClient yarnClient, ApplicationId applicationId, long timeout, TimeUnit unit, YarnApplicationState... applicationStates) throws Exception {
		Assert.notNull(yarnClient, "Yarn client must be set");
		Assert.notNull(applicationId, "ApplicationId must not be null");

		YarnApplicationState state = null;
		long end = System.currentTimeMillis() + unit.toMillis(timeout);

		// break label for inner loop
		done:
		do {
			state = findState(yarnClient, applicationId);
			if (state == null) {
				break;
			}
			for (YarnApplicationState stateCheck : applicationStates) {
				if (state.equals(stateCheck)) {
					break done;
				}
			}
			Thread.sleep(1000);
		} while (System.currentTimeMillis() < end);
		return state;
	}

	private YarnApplicationState findState(YarnClient client, ApplicationId applicationId) {
		YarnApplicationState state = null;
		for (ApplicationReport report : client.listApplications()) {
			if (report.getApplicationId().equals(applicationId)) {
				state = report.getYarnApplicationState();
				break;
			}
		}
		return state;
	}


}
