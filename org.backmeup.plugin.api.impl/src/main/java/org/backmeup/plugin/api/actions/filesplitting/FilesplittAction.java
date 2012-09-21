package org.backmeup.plugin.api.actions.filesplitting;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;

public class FilesplittAction implements Action
{
	private static final String START_FILESPLITT_PROCESS = "Starting filesplitting process";
	private static final String FILESPLITT_SORT = "Sorting files";
	private static final String FILESPLITT_SPLITT = "Splitting files to container";
	private static final String FILESPLITT_PROCESS_COMPLETE = "Filesplitting complete";
	private static final String MOVE_FILES_TO_TMP = "Moving all files to tmp folder";
	
	private static String PATH_SEPARATOR = "/";
	
	private static final long CONTAINER_SIZE = 10 * 1024 * 1024; // 10 MiB

	@Override
	public void doAction (Properties parameters, Storage storage, BackupJob job, Progressable progressor) throws ActionException
	{
		progressor.progress (START_FILESPLITT_PROCESS);
		
		try
		{
			PriorityQueue<DataObject> sorted = new PriorityQueue<DataObject> (storage.getDataObjectCount(), new Comparator<DataObject> ()
			{
				@Override
				public int compare (DataObject do1, DataObject do2)
				{
					Date date1 = null;
					Date date2 = null;
					
					for (Metainfo info : do1.getMetainfo ())
					{
						if ( (date1 = info.getModified ()) != null)
						{
							break;
						}
					}

					for (Metainfo info : do2.getMetainfo ())
					{
						if ( (date2 = info.getModified ()) != null)
						{
							break;
						}
					}

					if ( (date1 == null) && (date2 == null))
					{
						return 0;
					}
					else if ( (date1 == null) && (date2 != null))
					{
						return -1;
					}
					else if ( (date1 != null) && (date2 == null))
					{
						return +1;
					}

					return date1.compareTo (date2);
				}
			});


			// TODO remove this workflow later
			progressor.progress (MOVE_FILES_TO_TMP);
			Iterator<DataObject> dataobjects = storage.getDataObjects ();
			
			String tmp_dir = RandomStringUtils.randomAlphanumeric (16);
			while (storage.existsPath (tmp_dir) == true)
			{
				tmp_dir = RandomStringUtils.randomAlphanumeric (16);
			}
			
			while (dataobjects.hasNext () == true)
			{
				DataObject daob = dataobjects.next ();
				storage.move (daob.getPath (), tmp_dir + PATH_SEPARATOR + daob.getPath ());
			}
			dataobjects = null;
			
			
			progressor.progress (FILESPLITT_SORT);
			dataobjects = storage.getDataObjects ();
			while (dataobjects.hasNext () == true)
			{
				DataObject daob = dataobjects.next ();
				sorted.add (daob);
			}
			
			FileContainers fcs = new FileContainers (CONTAINER_SIZE, false);

			progressor.progress (FILESPLITT_SPLITT);
			
			DataObject daob = sorted.poll ();
			while (daob != null)
			{
				fcs.addData (daob);
				daob = sorted.poll ();
			}

			parameters.setProperty ("org.backmeup.filesplitting.containercount", fcs.getContainers ().size () + "");
			
			int container = 0;
			for (FileContainer fc : fcs.getContainers ())
			{
				parameters.setProperty ("org.backmeup.filesplitting.container." + container + ".size", fc.getContainersize () + "");
				parameters.setProperty ("org.backmeup.filesplitting.container." + container + ".name", fc.getContainerpath ());
				container++;
				
				if (storage.existsPath (fc.getContainerpath ()) == true)
				{
					// TODO handle the problem and remove the workarround
				}
				
				for (int i = 0; i < fc.getContainerElementCount (); i++)
				{
					// TODO remove the tmp folder with new storage interface (existFolder)
					storage.move (fc.getContainerElementOldPath (i), fc.getContainerElementNewPath (i).replaceAll (tmp_dir + PATH_SEPARATOR, ""));
				}
			}
		}
		catch (Exception e)
		{
			throw new ActionException (e);
		}
		
		progressor.progress (FILESPLITT_PROCESS_COMPLETE);
	}
}
