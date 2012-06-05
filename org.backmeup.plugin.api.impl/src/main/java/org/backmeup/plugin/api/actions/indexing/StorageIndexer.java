package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.IndexWriter;
import org.apache.tika.exception.TikaException;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.xml.sax.SAXException;

/**
 * A simple {@link BaseIndexer} subclass that creates a Lucene index from a 
 * {@link StorageReader}. Note that this class is not Hadoop-compatible!
 *  
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class StorageIndexer extends BaseIndexer {
	
	private StorageReader storage;
	
	public StorageIndexer(StorageReader storage) {
		this.storage = storage;
	}
	
	public void run() throws IOException, StorageException, SAXException, TikaException {
		// TODO replace with decent logging
		System.out.println("Starting indexing");

		// TODO remove hardcoded index dir location
		IndexWriter indexWriter = createIndexWriter(".");
		
		Iterator<DataObject> it = storage.getDataObjects();
		while (it.hasNext()) {
			DataObject next = it.next();
			index(indexWriter, next.getPath(), new ByteArrayInputStream(next.getBytes()));
		}

		indexWriter.close();
		// TODO replace with decent logging
		System.out.println("Indexing complete");
	}

}
