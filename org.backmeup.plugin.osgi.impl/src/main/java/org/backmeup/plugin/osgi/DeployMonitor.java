package org.backmeup.plugin.osgi;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * The DeployMonitor class starts a thread
 * which will periodically check the
 * osgi.deploymentDirectory (found within plugins.properties)
 * installing new bundles found there. If a bundle gets deleted,
 * it will also be deleted within OSGi. 
 * 
 * @author fschoeppl
 *
 */
public class DeployMonitor implements Runnable {

	private Map<File, Bundle> deployed = new HashMap<File, Bundle>();
	private ScheduledExecutorService executor = Executors
			.newScheduledThreadPool(1);
	private List<Bundle> newlyInstalledBundles = new LinkedList<Bundle>();
	private List<File> toBeRemovedBundles = new LinkedList<File>();
	private BundleContext context;
	private File deploymentDirectory;
	private Object monitor = new Object();
	private boolean firstRun = false;

	public DeployMonitor(BundleContext context, File deploymentDirectory) {
		this.context = context;
		this.deploymentDirectory = deploymentDirectory;
	}

	public void start() {
		if (executor != null)
			stop();
		executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);

	}
	
	public void waitForInitialRun() {
	  try {
	    if (!firstRun)
	      synchronized(monitor){
	        monitor.wait();
	      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }	  
	}

	public void stop() {
		executor.shutdown();
		executor.shutdownNow();
		try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
      System.err.println("Awaited termination of executor!");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
	}

	
	public void run() {
		if (deploymentDirectory.exists()) {
			for (File f : deploymentDirectory.listFiles()) {
				if (f.getName().endsWith(".jar")) {
					if (!deployed.containsKey(f)) {
						try {
							Bundle b = context.installBundle("file:"
									+ f.getAbsolutePath());
							deployed.put(f, b);
							newlyInstalledBundles.add(b);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			for (Bundle newlyInstalledBundle : newlyInstalledBundles) {
				try {
					if (newlyInstalledBundle.getHeaders().get(
							Constants.FRAGMENT_HOST) == null) {
						newlyInstalledBundle.start();
						while(newlyInstalledBundle.getState() == Bundle.STARTING) {
						  Thread.sleep(10);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			newlyInstalledBundles.clear();

			for (File f : deployed.keySet()) {
				if (!f.exists()
						|| deployed.get(f).getState() == Bundle.UNINSTALLED) {
					try {
						Bundle b = deployed.get(f);
						if (b.getState() == Bundle.ACTIVE) {
							b.stop();
						}
						if (b.getState() != Bundle.UNINSTALLED) {
							b.uninstall();
						}
						toBeRemovedBundles.add(f);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			for (File toBeRemovedFile : toBeRemovedBundles) {
				deployed.remove(toBeRemovedFile);
			}
			toBeRemovedBundles.clear();
			if (!firstRun) {			  
			  synchronized(monitor) {
			    firstRun = true;
			    monitor.notifyAll();
			  }
			}
		}
	}
}
