package org.springframework.yarn.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.fs.FsShell;
import org.springframework.yarn.YarnSystemConstants;

public class HdfsTasklet implements Tasklet {

	private static final Log log = LogFactory.getLog(HdfsTasklet.class);

	@Autowired
	private Configuration configuration;

	@Autowired(required=false)
	private JobParameters jobParameters;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		try {
			log.info("getJobParameters: " + chunkContext.getStepContext().getJobParameters());
			log.info("getStepExecutionContext: " + chunkContext.getStepContext().getStepExecutionContext());
			log.info("getJobExecutionContext: " + chunkContext.getStepContext().getJobExecutionContext());
		} catch (Exception e) {
			log.error("error", e);
		}

		log.info("AMSERVICE_BATCH_STEPNAME=" + System.getenv(YarnSystemConstants.AMSERVICE_BATCH_STEPNAME));
		log.info("AMSERVICE_BATCH_JOBEXECUTIONID=" + System.getenv(YarnSystemConstants.AMSERVICE_BATCH_JOBEXECUTIONID));
		log.info("AMSERVICE_BATCH_STEPEXECUTIONID=" + System.getenv(YarnSystemConstants.AMSERVICE_BATCH_STEPEXECUTIONID));
		log.info("AMSERVICE_BATCH_STEPEXECUTIONNAME=" + System.getenv("SHDP_AMSERVICE_BATCH_STEPEXECUTIONNAME"));

		String fileName = System.getenv(YarnSystemConstants.AMSERVICE_BATCH_STEPEXECUTIONNAME).replaceAll("\\W", "");

		@SuppressWarnings("resource")
		FsShell shell = new FsShell(configuration);
		boolean exists = shell.test("/tmp/" + fileName);
		if (exists) {
			log.info("File /tmp/" + fileName + " exist");
			log.info("Hello from HdfsTasklet ok");
			return RepeatStatus.FINISHED;
		} else {
			log.info("Hello from HdfsTasklet fail");
			throw new RuntimeException("File /tmp/" + fileName + " does't exist");
		}

	}

}
