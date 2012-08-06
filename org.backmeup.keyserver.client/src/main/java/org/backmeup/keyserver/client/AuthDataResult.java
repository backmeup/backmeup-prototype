package org.backmeup.keyserver.client;

public class AuthDataResult {
  public static class UserData {
    private Long bmu_user_id;

    public Long getBmu_user_id() {
      return bmu_user_id;
    }

    public void setBmu_user_id(Long bmu_user_id) {
      this.bmu_user_id = bmu_user_id;
    }

    public UserData() {
      super();
    }

    public UserData(Long bmu_user_id) {
      super();
      this.bmu_user_id = bmu_user_id;
    }

  }

  public static class ServiceData {
    private Long bmu_service_id;

    public Long getBmu_service_id() {
      return bmu_service_id;
    }

    public void setBmu_service_id(Long bmu_service_id) {
      this.bmu_service_id = bmu_service_id;
    }

    public ServiceData(Long bmu_service_id) {
      super();
      this.bmu_service_id = bmu_service_id;
    }

    public ServiceData() {
      super();
    }

  }

  private ServiceData[] services;
  private UserData user;
  private AuthData[] authinfos;

  public AuthDataResult(ServiceData[] services, UserData user,
      AuthData[] authinfos) {
    super();
    this.services = services;
    this.user = user;
    this.authinfos = authinfos;
  }

  public AuthDataResult() {
    super();
  }

  public ServiceData[] getServices() {
    return services;
  }

  public void setServices(ServiceData[] services) {
    this.services = services;
  }

  public UserData getUser() {
    return user;
  }

  public void setUser(UserData user) {
    this.user = user;
  }

  public AuthData[] getAuthinfos() {
    return authinfos;
  }

  public void setAuthinfos(AuthData[] authinfos) {
    this.authinfos = authinfos;
  }

}
