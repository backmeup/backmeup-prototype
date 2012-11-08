package org.backmeup.plugin.api.storage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.backmeup.plugin.api.MetainfoContainer;

public abstract class DataObject {

	public abstract byte[] getBytes() throws IOException;

	public abstract long getLength();
	
	public abstract String getPath();
	
	public abstract MetainfoContainer getMetainfo();
	
	public abstract void setMetainfo(MetainfoContainer meta);
	
	public String getMD5Hash() throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] md5 = digest.digest(getBytes());
			BigInteger bigInt = new BigInteger(1, md5);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			// Should never happen
			throw new RuntimeException(e);
		}
	}
	
}
