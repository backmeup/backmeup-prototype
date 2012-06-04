package org.backmeup.plugin;

import java.util.List;

import org.backmeup.model.spi.ActionDescribable;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.InputBased;
import org.backmeup.plugin.spi.OAuthBased;

/**
 * The Plugin interface 
 * encapsulates all operations
 * that interact with a plugin.
 * 
 * If an error occurs, a PluginException will be thrown.
 * 
 * @author fschoeppl
 *
 */
public interface Plugin {

	List<SourceSinkDescribable> getConnectedDatasources();
	
	List<SourceSinkDescribable> getConnectedDatasinks();
	
	List<ActionDescribable> getActions();
	
	ActionDescribable getActionById(String actionId);
	
	SourceSinkDescribable getSourceSinkById(String sourceSinkId);	
	
	Authorizable getAuthorizable(String sourceSinkId);
	
	OAuthBased getOAuthBasedAuthorizable(String sourceSinkId);
	
	InputBased getInputBasedAuthorizable(String sourceSinkId);
	
	Datasource getDatasource(String sourceId);
	
	Datasink getDatasink(String sinkId);
	
	Validationable getValidator(String sourceSinkId);
	
	void shutdown();

	void startup();
}
