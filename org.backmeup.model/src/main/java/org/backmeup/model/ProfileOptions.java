package org.backmeup.model;

/**
 * The ProfileOptions class wraps a profile and items that shall be backed up.
 * E.g. options[0] = "My Data\Folder1" which means that this folder should be
 * backed up.
 * 
 * @author fschoeppl
 * 
 */
public class ProfileOptions {
  private Profile profile;
  private String[] options;

  public ProfileOptions() {
  }

  public ProfileOptions(Profile profile, String[] options) {
    this.profile = profile;
    this.options = options;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public String[] getOptions() {
    return options;
  }

  public void setOptions(String[] options) {
    this.options = options;
  }
}