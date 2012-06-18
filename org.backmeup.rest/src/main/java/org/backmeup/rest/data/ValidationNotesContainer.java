package org.backmeup.rest.data;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.ValidationNotes.ValidationEntry;
import org.backmeup.model.spi.ValidationExceptionType;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement
public class ValidationNotesContainer {
  private boolean hasErrors;
  private List<ValidationEntry> errors;
  private List<ValidationEntry> warnings;
  
  public ValidationNotesContainer(ValidationNotes notes) {
    this.errors = new ArrayList<ValidationEntry>();
    this.warnings = new ArrayList<ValidationEntry>();
    for (ValidationEntry e : notes.getValidationEntries()) {
      if (e.getType() == ValidationExceptionType.Warning) {
        warnings.add(e);
      } else {
        errors.add(e);
      }
    }
    // Warnings are not being treated as errors!
    this.hasErrors = errors.size() > 0; 
    if (this.errors.size() == 0) 
      this.errors = null;
    if (this.warnings.size() == 0)
      this.warnings = null;
  }
  public boolean isHasErrors() {
    return hasErrors;
  }
  public void setHasErrors(boolean hasErrors) {
    this.hasErrors = hasErrors;
  }
  public List<ValidationEntry> getErrors() {
    return errors;
  }
  public void setErrors(List<ValidationEntry> errors) {
    this.errors = errors;
  }
  public List<ValidationEntry> getWarnings() {
    return warnings;
  }
  public void setWarnings(List<ValidationEntry> warnings) {
    this.warnings = warnings;
  }
  
  
}
