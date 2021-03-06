package org.backmeup.plugin.api.storage.hdfs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

public class HdfsUtil {
	
	private FileSystem hdfs;
	
	// TODO replace this with Log4j?
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public HdfsUtil() {
	}
	
	public void close() {
		try {
			hdfs.close();
		} catch (IOException e) {
			// Note: ERROR
			log.log(Level.SEVERE, "Unable to close HDFS: " + e);
		}
	}

	public boolean ifExists(Path source) throws IOException {

		boolean isExists = hdfs.exists(source);
		return isExists;
	}

	public String[] getHostnames() throws IOException {

		DistributedFileSystem dhdfs = (DistributedFileSystem) hdfs;
		DatanodeInfo[] dataNodeStats = dhdfs.getDataNodeStats();

		String[] names = new String[dataNodeStats.length];
		for (int i = 0; i < dataNodeStats.length; i++) {
			names[i] = dataNodeStats[i].getHostName();
			// Note: DEBUG
			log.log(Level.FINEST, (dataNodeStats[i].getHostName()));
		}
		return names;
	}

	public void getBlockLocations(String source) throws IOException {

		Path srcPath = new Path(source);

		// Check if the file already exists
		if (!(ifExists(srcPath))) {
			// Note: DEBUG
			log.log(Level.FINEST, "No such destination " + srcPath);
			return;
		}
		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1, source
				.length());

		FileStatus fileStatus = hdfs.getFileStatus(srcPath);

		BlockLocation[] blkLocations = hdfs.getFileBlockLocations(
				fileStatus, 0, fileStatus.getLen());
		int blkCount = blkLocations.length;

		// Note: DEBUG
		log.log(Level.FINEST, "File :" + filename + "stored at:");
		for (int i = 0; i < blkCount; i++) {
			String[] hosts = blkLocations[i].getHosts();
			System.out.format("Host %d: %s %n", i, hosts);
		}

	}

	public long getModificationTime(String source) throws IOException {

		Path srcPath = new Path(source);

		// Check if the file already exists
		if (!(hdfs.exists(srcPath))) {
			// Note: DEBUG
			log.log(Level.FINEST, "No such destination " + srcPath);
			return 0l;
		}
		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1, source
				.length());

		FileStatus fileStatus = hdfs.getFileStatus(srcPath);
		long modificationTime = fileStatus.getModificationTime();

		System.out.format("File %s; Modification time : %0.2f %n", filename,
				modificationTime);
		
		return modificationTime;

	}

	public void copyFromLocal(String source, String dest) throws IOException {

		Path srcPath = new Path(source);

		Path dstPath = new Path(dest);
		// Check if the file already exists
		if (!(hdfs.exists(dstPath))) {
			// Note: DEBUG
			log.log(Level.FINEST, "No such destination " + dstPath);
			return;
		}

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1, source
				.length());

		try {
			hdfs.copyFromLocalFile(srcPath, dstPath);
			// Note: DEBUG
			log.log(Level.FINEST, "File " + filename + "copied to " + dest);
		} catch (Exception e) {
			// Note: ERROR
			log.log(Level.SEVERE, "Exception caught! :" + e);
		} 
	}

	public void copyToLocal(String source, String dest) throws IOException {

		Path srcPath = new Path(source);

		Path dstPath = new Path(dest);
		// Check if the file already exists
		if (!(hdfs.exists(srcPath))) {
			// Note: DEBUG
			log.log(Level.FINEST, "No such destination " + srcPath);
			return;
		}

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1, source
				.length());

		try {
			hdfs.copyToLocalFile(srcPath, dstPath);
			// Note: DEBUG
			log.log(Level.FINEST, "File " + filename + "copied to " + dest);
		} catch (Exception e) {
			// Note: ERROR
			log.log(Level.SEVERE, "Exception caught! :" + e);
		} 
	}

	public void renameFile(String fromthis, String tothis) throws IOException {

		Path fromPath = new Path(fromthis);
		Path toPath = new Path(tothis);

		if (!(hdfs.exists(fromPath))) {
			// Note: DEBUG
			log.log(Level.FINEST, "No such destination " + fromPath);
			return;
		}

		if (hdfs.exists(toPath)) {
			// Note: DEBUG
			log.log(Level.FINEST, "Already exists! " + toPath);
			return;
		}

		try {
			boolean isRenamed = hdfs.rename(fromPath, toPath);
			if (isRenamed) {
				// Note: DEBUG
				log.log(Level.FINEST, "Renamed from " + fromthis + "to " + tothis);
			}
		} catch (Exception e) {
			// Note: DEBUG
			log.log(Level.FINEST, "Exception :" + e);
		}
		
	}

	public void addFile(String source, String dest) throws IOException {

		// Get the filename out of the file path
		String filename = source.substring(source.lastIndexOf('/') + 1, source
				.length());

		// Create the destination path including the filename.
		if (dest.charAt(dest.length() - 1) != '/') {
			dest = dest + "/" + filename;
		} else {
			dest = dest + filename;
		}

		// Check if the file already exists
		Path path = new Path(dest);
		if (hdfs.exists(path)) {
			// Note: DEBUG
			log.log(Level.FINEST, "File " + dest + " already exists");
			return;
		}

		// Create a new file and write data to it.
		FSDataOutputStream out = hdfs.create(path);
		InputStream in = new BufferedInputStream(new FileInputStream(new File(
				source)));

		byte[] b = new byte[1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}

		// Close all the file descriptors
		in.close();
		out.close();
		
	}

	public void addFile(InputStream ins, String dest) throws IOException {

		// Check if the file already exists
		Path path = new Path(dest);
		if (hdfs.exists(path)) {
			// Note: DEBUG
			log.log(Level.FINEST, "File " + dest + " already exists");
			return;
		}

		// Create a new file and write data to it.
		FSDataOutputStream out = hdfs.create(path);
		InputStream in = new BufferedInputStream(ins);

		byte[] b = new byte[1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}

		// Close all the file descriptors
		in.close();
		out.close();
		
	}

	public void readFile(String file) throws IOException {

		Path path = new Path(file);
		if (!hdfs.exists(path)) {
			// Note: DEBUG
			log.log(Level.FINEST, "File " + file + " does not exists");
			return;
		}

		FSDataInputStream in = hdfs.open(path);

		String filename = file.substring(file.lastIndexOf('/') + 1, file
				.length());

		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				new File(filename)));

		byte[] b = new byte[1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}

		in.close();
		out.close();
		
	}

	public void deleteFile(String file) throws IOException {
		Path path = new Path(file);

		if (!hdfs.exists(path)) {
			// Note: DEBUG
			log.log(Level.FINEST, "File " + file + " does not exists");
			return;
		}

		hdfs.delete(new Path(file), true);

	}

	public Path mkdir(String dir) throws IOException {

		Path path = new Path(dir);
		if (hdfs.exists(path)) {
			// Note: DEBUG
			log.log(Level.FINEST, "Dir " + dir + " already exists!");
			return null;
		}

		hdfs.mkdirs(path);
		return path;

	}

}
