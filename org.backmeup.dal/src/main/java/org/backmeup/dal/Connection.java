package org.backmeup.dal;


public interface Connection {
  
  public void beginOrJoin();
  
  public void begin();
  
  public void rollback();
  
  public void commit();
}
