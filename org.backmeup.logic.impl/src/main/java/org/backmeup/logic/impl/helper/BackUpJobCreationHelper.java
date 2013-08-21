package org.backmeup.logic.impl.helper;

import java.util.Date;

import org.backmeup.logic.impl.BusinessLogicImpl;
import org.backmeup.model.dto.ExecutionTime;
import org.backmeup.model.dto.JobCreationRequest;

public class BackUpJobCreationHelper {
  public static ExecutionTime getExecutionTimeFor(JobCreationRequest request) {
    if (request.getTimeExpression().equalsIgnoreCase("daily")) {
      return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_DAILY, true);           
    } else if (request.getTimeExpression().equalsIgnoreCase("weekly")) {
      return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_WEEKLY, true);
    } else if (request.getTimeExpression().equalsIgnoreCase("monthly")) {
      return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_MONTHLY, true);
    } else if (request.getTimeExpression().equalsIgnoreCase("yearly")) {
      return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_YEARLY, true);
    } else if (request.getTimeExpression().equalsIgnoreCase("realtime")) {
    	return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_REALTIME, false);
    } else {
      return new ExecutionTime(new Date(), BusinessLogicImpl.DELAY_MONTHLY, false);
    }
  }
}
