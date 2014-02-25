package org.springframework.yarn.examples;

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.yarn.am.AppmasterServiceClient;
import org.springframework.yarn.batch.container.DefaultBatchYarnContainer;
import org.springframework.yarn.integration.IntegrationAppmasterServiceClient;

public class BatchContainer extends DefaultBatchYarnContainer {

	@Autowired
	@Override
	public void setStepLocator(StepLocator stepLocator) {
		super.setStepLocator(stepLocator);
	}

	@Autowired
	@Override
	public void setJobExplorer(JobExplorer jobExplorer) {
		super.setJobExplorer(jobExplorer);
	}

	@Autowired
	public void setAppmasterServiceClient(AppmasterServiceClient appmasterServiceClient) {
		super.setIntegrationServiceClient((IntegrationAppmasterServiceClient<?>) appmasterServiceClient);
	}

}
