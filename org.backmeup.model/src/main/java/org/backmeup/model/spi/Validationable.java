package org.backmeup.model.spi;

import java.util.Properties;

import org.backmeup.model.ValidationNotes;

public interface Validationable {
  public ValidationNotes validate(Properties accessData);
}
