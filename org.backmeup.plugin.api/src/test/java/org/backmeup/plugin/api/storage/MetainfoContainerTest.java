package org.backmeup.plugin.api.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import junit.framework.Assert;

import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.junit.Test;

public class MetainfoContainerTest {
	
	@Test
	public void testRoundtrip() {
		MetainfoContainer before = new MetainfoContainer();
		
		for (int i=0; i<3; i++) {
			Metainfo metainfo = new Metainfo();
			metainfo.setAttribute("key" + i, "value" + i);
			before.addMetainfo(metainfo);
		}
	
		// JSON is similar to '[{"key0":"value0"},{"key1":"value1"},{"key2":"value2"}]'
		String json = MetainfoContainer.toJSON(before);
		System.out.println("serialized: " + json);
		
		MetainfoContainer after = MetainfoContainer.fromJSON(json);
		List<Metainfo> metainfo = new ArrayList<Metainfo>();
		Iterator<Metainfo> it = after.iterator();
		while (it.hasNext())
			metainfo.add(it.next());
		
		Assert.assertEquals(3, metainfo.size());
		for (Metainfo entry : metainfo) {
			Properties props = entry.getAttributes();
			for (Entry<Object, Object> prop : props.entrySet()) {
				Assert.assertTrue(prop.getKey() instanceof String);
				Assert.assertTrue(prop.getValue() instanceof String);
				Assert.assertTrue(prop.getKey().toString().startsWith("key"));
				Assert.assertTrue(prop.getValue().toString().startsWith("value"));
			}
		}
	}

}
