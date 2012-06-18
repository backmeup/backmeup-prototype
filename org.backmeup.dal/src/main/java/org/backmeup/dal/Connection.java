package org.backmeup.dal;


public interface Connection {
  
  public void begin();
  
  public void rollback();
  
  public void commit();

  public void releaseConnection();
}
