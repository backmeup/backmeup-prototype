package org.backmeup.model.tests;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.model.spi.SourceSinkDescribable.Type;
import org.junit.Test;

public class SerializiationTests {
  
  private void testProfiles(Profile p1, Profile p2) {
    assert p1.getDesc().equals(p2.getDesc());
    assert p1.getProfileId().equals(p2.getProfileId());
    assert p1.getProfileName().equals(p2.getProfileName());
    assert p1.getType().equals(p2.getType());
    testUser(p1.getUser(), p2.getUser());    
  }
  
  private void testUser(User u1, User u2) {
    assert u1.getUserId() == u2.getUserId();
    assert u1.getEmail().equals(u2.getEmail());
  }
  
  private void testActionProfile(ActionProfile ap1, ActionProfile ap2) {
    assert ap1.getActionId() == ap2.getActionId();
    assert ap1.getId() == ap2.getId();
  }
  
  private void testProfileOptions(ProfileOptions po1 ,ProfileOptions po2) {
    assert po1.getOptionId() == po2.getOptionId();
    for (int i=0; i < po1.getOptions().length; i++) {
      assert po1.getOptions()[i].equals(po2.getOptions()[i]);  
    }
    testProfiles(po1.getProfile(), po2.getProfile());
  }
  
  @Test
  public void testBackupJobSerializiation() {
    User user = new User(1L, "Sepp", "Wasfajsdfjasfl", "asdfoiasdfoas", "Sepp@Mail.at");
    Set<ProfileOptions> options = new HashSet<ProfileOptions>();
    Profile source = new Profile(2L, user, "TestProfile", "org.backmeup.source", Type.Source);
    ProfileOptions po = new ProfileOptions(source, new String[]{"folder1", "folder2"});
    options.add(po);
    Profile sink = new Profile(2L, user, "TestProfile2", "org.backmeup.sink", Type.Sink);
    Set<ActionProfile> actions = new HashSet<ActionProfile>();
    BackupJob job = new BackupJob(user, options, sink, actions, new Date(), new Date().getTime() + 1000000L);
    String serializedJob = JsonSerializer.serialize(job);
    BackupJob restored = JsonSerializer.deserialize(serializedJob, BackupJob.class);
    restored.toString();
    assert job.getDelay() == restored.getDelay();
    assert job.getId() == restored.getId();
    assert job.getStart().equals(restored.getStart());
    testUser(job.getUser(), restored.getUser());
    for (int i=0; i < job.getRequiredActions().size(); i++) {
      Iterator<ActionProfile> apIt1 = job.getRequiredActions().iterator();
      Iterator<ActionProfile> apIt2 = restored.getRequiredActions().iterator();
      while(apIt1.hasNext()) {
        testActionProfile(apIt1.next(), apIt2.next());
      }
    }
    testProfiles(job.getSinkProfile(), restored.getSinkProfile());
    for (int i=0; i < job.getSourceProfiles().size(); i++) {
      Iterator<ProfileOptions> apIt1 = job.getSourceProfiles().iterator();
      Iterator<ProfileOptions> apIt2 = restored.getSourceProfiles().iterator();
      while(apIt1.hasNext()) {
        testProfileOptions(apIt1.next(), apIt2.next());
      }
    }    
  }
}
