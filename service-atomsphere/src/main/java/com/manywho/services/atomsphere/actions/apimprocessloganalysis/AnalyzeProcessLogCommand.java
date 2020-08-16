package com.manywho.services.atomsphere.actions.apimprocessloganalysis;

import java.util.List;

import com.manywho.sdk.api.run.elements.config.ServiceRequest;
import com.manywho.sdk.services.actions.ActionCommand;
import com.manywho.sdk.services.actions.ActionResponse;
import com.manywho.services.apimlog.ProcessLogUtil;
import com.manywho.services.atomsphere.ServiceConfiguration;

public class AnalyzeProcessLogCommand implements ActionCommand<ServiceConfiguration, AnalyzeProcessLog, AnalyzeProcessLog.Inputs, AnalyzeProcessLog.Outputs>{

	@Override
	public ActionResponse<AnalyzeProcessLog.Outputs> execute(ServiceConfiguration configuration, ServiceRequest request,
			AnalyzeProcessLog.Inputs input) {
		List<ProcessLogItem> processLogItems=null;
		try {
			ProcessLogUtil util = new ProcessLogUtil();
			processLogItems = util.analyzeLog(configuration, input.getExecutionId(), input.getAggregate());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
//        Collections.sort(processLogItems, new AtomPropertyComparator());

		return new ActionResponse<>(new AnalyzeProcessLog.Outputs(processLogItems));
	}	
}
