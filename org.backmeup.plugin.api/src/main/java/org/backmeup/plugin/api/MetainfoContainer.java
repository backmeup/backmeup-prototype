package org.backmeup.plugin.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetainfoContainer implements Iterable<Metainfo> {
  private List<Metainfo> metainfo = new ArrayList<Metainfo>();
  
  public void addMetainfo(Metainfo info) {
    this.metainfo.add(info);
  }
  
  public void removeMetainfo(Metainfo info) {
    this.metainfo.remove(info);
  }

  @Override
  public Iterator<Metainfo> iterator() {
    return metainfo.iterator();
  }
  
  public Metainfo get(int index) {
    return index < metainfo.size() ? metainfo.get(index) : null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Metainfo m : metainfo) {
      sb.append(m.toString());
    }
    return sb.toString();
  }
  
  
}
