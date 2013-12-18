package org.backmeup.plugin.api.actions.thumbnail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.actions.indexing.IndexUtils;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailAction implements Action {
	private static final Logger logger = LoggerFactory.getLogger(ThumbnailAction.class);
	//private static Configuration config = Configuration.getConfig();
	
	private static final String THUMBNAIL_TEMP_DIR = "thumbnails";
	private static final Integer THUMBNAIL_DIMENSIONS = 120;
	
	private static File TEMP_DIR; 
	//private static Integer THUMBNAIL_DIMENSIONS;
	
	private static List<String> UNSUPPORTED_TYPES = Arrays.asList("css", "html", "xml");
	
	static {
		try {
			//String path = config.getProperty("thumbnail.temp.dir");
			String path = THUMBNAIL_TEMP_DIR;
			if (!path.endsWith("/"))
				path = path + "/";
				
			TEMP_DIR = new File(path);
		} catch (Throwable t) {
			TEMP_DIR = new File("tmp/thumbnails/");
			logger.debug("Thumbnail rendering temp dir not set - defaulting to 'tmp/thumbnails'");
		}
		
		try {
			//THUMBNAIL_DIMENSIONS = Integer.valueOf(Integer.parseInt(config.getProperty("thumbnail.dimensions")));
		} catch (Throwable t) {
			logger.debug("Thumbnail dimensions not set - defaulting to 120px");
		}
		
		if (!TEMP_DIR.exists())
			TEMP_DIR.mkdirs();
	}
	
	
	/**
	 * The GraphicsMagick command we need to emulate is this:
	 * 
	 * gm convert -size 120x120 original.jpg -resize 120x120 +profile "*" thumbnail.jpg
	 * 
	 * @return the name of the thumbnail file
	 * @throws IM4JavaException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private String convert(File original) throws IOException, 
		InterruptedException, IM4JavaException {
		
		String thumbnailPath = original.getAbsolutePath() + "_thumb.jpg";
		
		IMOperation op = new IMOperation();
		op.size(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
		op.quality(80.0);
		op.resize(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
		op.p_profile("*");
		op.addImage(original.getAbsolutePath() + "[0]");
		op.addImage(thumbnailPath);
		new ConvertCmd(true).run(op);
		
		return thumbnailPath;
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
				
				boolean supported = true;
				for (String format : UNSUPPORTED_TYPES) {
					if (tempFilename.toLowerCase().endsWith(format))
							supported = false;
				}
				
				if (supported) {
					if (tempFilename.startsWith("/"))
						tempFilename = tempFilename.substring(1);
					
					tempFilename = System.currentTimeMillis() + "_" + tempFilename.replace("/", "$").replace(" ", "_").replace("#", "_");
					File folder = new File(TEMP_DIR, job.getId().toString());
					if (!folder.exists())
						folder.mkdirs();
						
					File tempFile = new File(folder, tempFilename);
					FileOutputStream fos = new FileOutputStream(tempFile);
					fos.write(dataobject.getBytes());
					fos.close();
					
					try {
						// Generate thumbnails using GraphicsMagick
						String thumbPath = convert(tempFile);
						Metainfo meta = new Metainfo();
						meta.setAttribute(IndexUtils.FIELD_THUMBNAIL_PATH, thumbPath);
						MetainfoContainer container = dataobject.getMetainfo();
						container.addMetainfo(meta);
						dataobject.setMetainfo(container);
					} catch (Throwable t) {
						logger.debug("Failed to render thumbnail for: " + dataobject.getPath());
						logger.debug(t.getClass().getName() + ": " + t.getMessage());
					}
				}
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
		
		progressor.progress("Thumbnail rendering complete");
	}

}
