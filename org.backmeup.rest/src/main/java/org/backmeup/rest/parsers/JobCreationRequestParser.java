package org.backmeup.rest.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;

import org.backmeup.model.dto.ActionProfileEntry;
import org.backmeup.model.dto.JobCreationRequest;
import org.backmeup.model.dto.SourceProfileEntry;

public class JobCreationRequestParser {
  public static JobCreationRequest parse(MultivaluedMap<String, String> formParameters) {
    JobCreationRequest jcr = new JobCreationRequest();
    
    for (String sourceProfile : formParameters.get("sourceProfiles")) {
      SourceProfileEntry spe = new SourceProfileEntry();
      spe.setId(Long.parseLong(sourceProfile));
      for (Entry<String, String> entry : getOptions(spe.getId()+"", formParameters).entrySet()) {
        spe.getOptions().put(entry.getKey(), entry.getValue());
      }
      
      jcr.getSourceProfiles().add(spe);
    }
    
    for (String action : formParameters.get("actions")) {
      ActionProfileEntry ape = new ActionProfileEntry();
      ape.setId(action);
      for (Entry<String, String> entry : getOptions(ape.getId()+"", formParameters).entrySet()) {
        ape.getOptions().put(entry.getKey(), entry.getValue());
      }
      jcr.getActions().add(ape);
    }
    
    jcr.setSinkProfileId(Long.parseLong(formParameters.getFirst("sinkProfileId")));
    
    jcr.setJobTitle(formParameters.getFirst("jobTitle"));
    jcr.setKeyRing(formParameters.getFirst("keyRing"));
    jcr.setTimeExpression(formParameters.getFirst("timeExpression"));
    return jcr;
  }
  
  private static Map<String, String> getOptions(String startString, MultivaluedMap<String, String> formParameters) {
    Map<String, String> o = new HashMap<String, String>();
    for (Entry<String, List<String>> entry : formParameters.entrySet()) {      
      if (entry.getKey().startsWith(startString + ".")) {
        String valueName = entry.getKey().substring((startString + ".").length());
        o.put(valueName, entry.getValue().get(0));
      }
    }
    return o;
  }  
}
