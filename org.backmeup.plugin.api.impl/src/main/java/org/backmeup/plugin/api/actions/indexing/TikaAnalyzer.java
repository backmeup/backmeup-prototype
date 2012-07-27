package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.backmeup.plugin.api.storage.DataObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TikaAnalyzer {
	
	private Tika tika = new Tika();
	
	public Map<String, String> analyze(DataObject dob) throws SAXException, IOException, TikaException {
		ByteArrayInputStream is = new ByteArrayInputStream(dob.getBytes());
		
		String mimeType = (is.markSupported()) ? tika.detect(is) : null;

		Metadata metadata = new Metadata();
		if (mimeType != null) 
			metadata.set(Metadata.CONTENT_TYPE, mimeType);

		ContentHandler handler = new BodyContentHandler();
		Parser parser = tika.getParser();
		parser.parse(is, handler, metadata, new ParseContext());

		// TODO I assume these should go directly into the dob's MetainfoContainer?
		Map<String, String> meta = new HashMap<String, String>();
		for (String name : metadata.names()) {
			String value = metadata.get(name);
			meta.put(name, value);
		}
		return meta;
	}

}
