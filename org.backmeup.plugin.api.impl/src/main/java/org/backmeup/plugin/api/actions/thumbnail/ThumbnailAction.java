package org.backmeup.plugin.api.actions.thumbnail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

public class ThumbnailAction implements Action {
	
	// TODO configure via application properties
	private static File tempDir = new File("test/temp"); 
	private static Integer THUMBNAIL_DIMENSIONS = Integer.valueOf(120);
	
	static {
		if (!tempDir.exists())
			tempDir.mkdirs();
	}
	
	
	/**
	 * The GraphicsMagick command we need to emulate is this:
	 * 
	 * gm convert -size 120x120 original.jpg -resize 120x120 +profile "*" thumbnail.jpg
	 * 
	 * @return
	 * @throws IM4JavaException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void convert(String original, String thumbnail) throws IOException, 
		InterruptedException, IM4JavaException {
		
		IMOperation op = new IMOperation();
		op.size(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
		op.quality(75.0);
		op.resize(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
		op.p_profile("*");
		op.addImage(original);
		op.addImage(thumbnail);
		new ConvertCmd(true).run(op);
	}

	@Override
	public void doAction(Properties parameters, Storage storage, BackupJob job,
			Progressable progressor) throws ActionException {
		
		progressor.progress("Starting thumbnail rendering");
		
		try {
			Iterator<DataObject> dobs = storage.getDataObjects();
			while (dobs.hasNext()) {
				DataObject dataobject = dobs.next();
				progressor.progress("Processing " + dataobject.getPath());
				
				// Write file to temp dir
				String tempFilename = dataobject.getPath();
				if (tempFilename.startsWith("/"))
					tempFilename = tempFilename.substring(1);
				
				tempFilename.replace("/", "$");
				File tempFile = new File(tempDir, tempFilename);
				FileOutputStream fos = new FileOutputStream(tempFile);
				fos.write(dataobject.getBytes());
				fos.close();
				
				try {
					// Generate thumbnails using GraphicsMagick
					convert(tempFile.getAbsolutePath(), tempFile.getAbsolutePath() + "_thumb.jpg");
				} catch (Throwable t) {
					System.out.println("Failed to render thumbnail for: " + dataobject.getPath());
				}
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
		
		progressor.progress("Thumbnail rendering complete");
	}

}
