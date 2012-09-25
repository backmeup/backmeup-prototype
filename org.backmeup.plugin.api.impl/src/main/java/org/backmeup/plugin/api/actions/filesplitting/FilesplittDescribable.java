package org.backmeup.plugin.api.actions.filesplitting;

import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.actions.BaseActionDescribable;
//TODO move in own project
public class FilesplittDescribable extends BaseActionDescribable { 
  public FilesplittDescribable() {
    super("filesplitt.properties");
  }
  
  @Override
  public List<String> getAvailableOptions ()
  {
	  List<String> options = new LinkedList<String> ();
	  
	  options.add ("Test Option 1");
	  options.add ("Test Option 2");
	  
	  return options;
  }
}
