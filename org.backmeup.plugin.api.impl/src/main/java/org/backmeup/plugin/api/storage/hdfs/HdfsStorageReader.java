package org.backmeup.plugin.api.storage.hdfs;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;

public class HdfsStorageReader extends StorageReader {

	private FileSystem filesystem;
	private String path;
	private SequenceFile.Reader reader;
	private HdfsIterator hdfsIt;
	
	public HdfsStorageReader(FileSystem filesystem) {
		this.filesystem = filesystem;
	}

	@Override
	public void open(String path) throws StorageException {
		this.path = path;
		Path thePath = new Path(path);
		try {
			reader = new SequenceFile.Reader(filesystem, thePath, filesystem.getConf());
		} catch (IOException e) {
			throw new StorageException(e);
		}
		this.hdfsIt = null;
	}

	@Override
	public void close() throws StorageException {
		try {
			reader.close();
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {
		if (hdfsIt == null) {
			try {
				hdfsIt = new HdfsIterator();
			} catch (InstantiationException e) {
				throw new StorageException(e);
			} catch (IllegalAccessException e) {
				throw new StorageException(e);
			}
		}
		return hdfsIt;
	}

	public class HdfsIterator implements Iterator<DataObject> {

		private boolean hasNextCalled;
		private Text key;
		private BytesWritable value;

		public HdfsIterator() throws InstantiationException,
				IllegalAccessException {
			key = (Text) reader.getKeyClass().newInstance();
			value = (BytesWritable) reader.getValueClass().newInstance();
		}

		@Override
		public boolean hasNext() {
			if (hasNextCalled)
				return true;
			else {
				try {
					return reader.next(key, value);
				} catch (IOException e) {
					return false;
				}
			}
		}

		@Override
		public DataObject next() {
			System.out.println("Returning next DataObject: " + path);
			if (!hasNextCalled) {
				try {
					reader.next(key, value);
					hasNextCalled = false;
				} catch (IOException e) {
					return null;
				}				
			}
			return new HdfsDataObject(path, key.toString(), value.getBytes(), value.getLength());
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}

	}
}
