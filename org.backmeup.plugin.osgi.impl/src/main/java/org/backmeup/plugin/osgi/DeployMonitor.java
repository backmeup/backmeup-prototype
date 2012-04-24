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

public class DeployMonitor implements Runnable {

	private Map<File, Bundle> deployed = new HashMap<File, Bundle>();
	private ScheduledExecutorService executor = Executors
			.newScheduledThreadPool(1);
	private List<Bundle> newlyInstalledBundles = new LinkedList<Bundle>();
	private List<File> toBeRemovedBundles = new LinkedList<File>();
	private BundleContext context;
	private File deploymentDirectory;

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

	public void stop() {
		executor.shutdown();
		executor.shutdownNow();
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
							Constants.FRAGMENT_HOST) == null)
						newlyInstalledBundle.start();
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
		}
	}
}
