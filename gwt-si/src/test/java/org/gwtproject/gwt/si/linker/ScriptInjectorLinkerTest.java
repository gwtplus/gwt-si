package org.gwtproject.gwt.si.linker;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;

public class ScriptInjectorLinkerTest {

	@Test
	public void testGenerateScriptRefsFile() {
		ScriptInjectorLinker l = new ScriptInjectorLinker();
		TreeLogger logger = Mockito.mock(TreeLogger.class);
		LinkerContext context = Mockito.mock(LinkerContext.class);
		
		List<String> list = new ArrayList<>();
		
		String expected = l.generateScriptRefsFile(logger, context, list);
		assertEquals(expected, "{ \"scriptRefs\": [\n]}");
		
		list.add("script.js");
		
		expected = l.generateScriptRefsFile(logger, context, list);
		assertEquals(expected, "{ \"scriptRefs\": [\n\t\"script.js\"\n]}");
		
		list.add("dir/script.js");
		
		expected = l.generateScriptRefsFile(logger, context, list);
		assertEquals(expected, "{ \"scriptRefs\": [\n\t\"script.js\",\n\t\"dir/script.js\"\n]}");
	}
}
