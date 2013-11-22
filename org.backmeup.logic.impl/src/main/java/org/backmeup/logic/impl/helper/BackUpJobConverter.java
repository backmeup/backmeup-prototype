package org.backmeup.logic.impl.helper;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.ActionProfile.ActionProperty;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.JobUpdateRequest;
import org.backmeup.model.dto.SourceProfileEntry;

public class BackUpJobConverter {
  private static ActionProfileEntry findPreviousEntry(String id, List<ActionProfileEntry> profiles) {
    for (ActionProfileEntry ape : profiles) {
      if (ape.getId().equals(id))
        return ape;
    }
    return null;
  }
  
  public static JobUpdateRequest convertJobToUpdateRequest(BackupJob job) {
    JobUpdateRequest jur = new JobUpdateRequest();
    jur.setJobId(job.getId());
    jur.setJobTitle(job.getJobTitle());
    jur.setSinkProfileId(job.getSinkProfile().getProfileId());
    jur.setTimeExpression(job.getTimeExpression());
    
    // convert the action profiles
    List<ActionProfileEntry> actions = new ArrayList<ActionProfileEntry>();
    for (ActionProfile ap : job.getRequiredActions()) {
      ActionProfileEntry ape = findPreviousEntry(ap.getActionId(), actions);
      if (ape == null) {
        ape = new ActionProfileEntry();
        ape.setId(ap.getActionId());
        actions.add(ape);
      }
      for (ActionProperty property : ap.getActionOptions()) {
        ape.getOptions().put(property.getKey(), property.getValue());
      }
    }
    
    // convert the source profiles
    List<SourceProfileEntry> sources = new ArrayList<SourceProfileEntry>();
    for (ProfileOptions po : job.getSourceProfiles()) {
      SourceProfileEntry entry = new SourceProfileEntry();
      entry.setId(po.getProfile().getProfileId());
      for (String option : po.getOptions()) {
        entry.getOptions().put(option, "true");
      }
      sources.add(entry);
    }
    
    jur.setActions(actions);
    jur.setSourceProfiles(sources);
    
    return jur;
  }
}
