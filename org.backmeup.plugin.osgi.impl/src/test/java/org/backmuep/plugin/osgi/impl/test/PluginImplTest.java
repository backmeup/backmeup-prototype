package org.backmuep.plugin.osgi.impl.test;

import java.util.List;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.osgi.PluginImpl;
import org.junit.Before;
import org.junit.Test;

public class PluginImplTest {
  private Plugin pluginLayer;
  
  @Before
  public void setUp() {
    pluginLayer = new PluginImpl("C:/Fabian/git/master_branch/backmeup-prototype/org.backmeup.embedded/autodeploy", 
        "C:/temp/osgiTmpTest", 
        "org.backmeup.plugin.spi org.backmeup.model org.backmeup.model.spi org.backmeup.plugin.api.connectors org.backmeup.plugin.api.storage com.google.gson org.backmeup.plugin.api"
        );
    
    pluginLayer.startup();
    ((PluginImpl)pluginLayer).waitForInitialStartup();
  }
  
  @Test
  public void testPluginLayer() {
    List<SourceSinkDescribable>  sources = pluginLayer.getConnectedDatasources();
    for (int i=0; i < sources.size(); i++) {
      System.out.println(sources.get(i).getId());
    }
    
    List<SourceSinkDescribable>  sinks = pluginLayer.getConnectedDatasinks();
    for (int i=0; i < sinks.size(); i++) {
      System.out.println(sinks.get(i).getId());
    }
  }
}
