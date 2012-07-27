package org.backmeup.plugin.api.actions.indexing;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorageReader;
import org.backmeup.plugin.api.storage.StorageReader;
import org.junit.Test;

public class IndexActionTest {
	
	private Progressable logProgressable = new Progressable() {
		@Override
		public void progress(String message) {
			System.out.println("PROGRESS: " + message);
		}
	};
	
	@Test
    public void testIndexAction() throws ActionException {
		StorageReader reader = new DummyStorageReader();
	  
		IndexAction action = new IndexAction();
		action.doAction(null, reader, logProgressable);
	}

}
