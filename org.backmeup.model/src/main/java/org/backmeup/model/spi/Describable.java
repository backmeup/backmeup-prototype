package org.backmeup.model.spi;

import java.util.Properties;

/**
 * A plugin is eather a source, an action or a sink plugin.
 * 
 * The id of the plugin must be unique within the system and
 * it must be used within the plugins spring configuration as
 * the filter for each service, e.g.
 * 
 * getId() = "org.backmeup.dropbox"
 * 
 * spring configuration must be:
 * 
 * <service id="dropboxDescriptorService" ref="dropboxDescriptor" auto-export="interfaces">
 *  <service-properties>
 *    <!-- value must be getId() -->
 *    <entry key="name" value="org.backmeup.dropbox"/>
 *  </service-properties>
 * </service>
 * 
 * The title can be displayed within a client.
 * 
 * The description should state what the plugin does.
 *  
 * @author fschoeppl
 *
 */
public abstract interface Describable {
	public String getId();
	public String getTitle();
	public String getDescription();
	public Properties getMetadata(Properties accessData);
}
