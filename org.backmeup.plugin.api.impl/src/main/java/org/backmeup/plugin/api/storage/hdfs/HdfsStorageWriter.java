package org.backmeup.plugin.api.storage.hdfs;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

public class HdfsStorageWriter extends StorageWriter {
	
	private FileSystem filesystem;
	
	private String outputPath;
	
	private SequenceFile.Writer output;
	
	public HdfsStorageWriter(FileSystem filesystem) {
		this.filesystem = filesystem;
	}

	@Override
	public void open(String path) throws StorageException {
		this.outputPath = path;
		try {
			this.output = openOutputFile();
		} catch (Exception e) {
			throw new StorageException(e);
		}
		
	}

	//TODO: Store metadata within Hdfs
	@Override
	public void addFile(InputStream is, String path, MetainfoContainer metadata)
			throws StorageException {
	  
        Text key = new Text(path);
		byte[] data;
		try {
			data = IOUtils.toByteArray(is);
	        BytesWritable value = new BytesWritable(data);
	        output.append(key, value);
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}
	
	public void addFile(InputStream is, String path)
      throws StorageException {
	  addFile(is, path, null);
	}

	@Override
	public void close() throws StorageException {
		try {
			output.close();
		} catch (IOException e) {
			throw new StorageException(e);
		}
		
	}

	private SequenceFile.Writer openOutputFile() throws Exception {
		Path thePath = new Path(outputPath);
		return SequenceFile.createWriter(filesystem, filesystem.getConf(),
				thePath, Text.class, BytesWritable.class,
				SequenceFile.CompressionType.BLOCK);
	}
	
}
