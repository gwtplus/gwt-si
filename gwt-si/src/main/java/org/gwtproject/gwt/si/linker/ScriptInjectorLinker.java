package org.gwtproject.gwt.si.linker;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.ConfigurationProperty;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.ScriptReference;
import com.google.gwt.core.ext.linker.Shardable;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.linker.CrossSiteIframeLinker;

/**
 * Linker useful mostly in tandem with {@link CrossSiteIframeLinker}. Generates
 * a json file with script references from module gwt.xml files which then can
 * be loaded using {@link ScriptInjector}
 * 
 * @see ScriptInjectorEntry
 */
@Shardable
@LinkerOrder(Order.PRE)
public class ScriptInjectorLinker extends AbstractLinker {

	/**
	 * A configuration property that can be used to modify the name of the file
	 * which will contain the list of all required script tags
	 */
	private static final String PROPERTY_SCRIPT_REFS_FILE_NAME = "si.scriptRefsFileName";

	/**
	 * A configuration property that can be used to control if the original
	 * script artifacts should be removed from ArtifactSet. Allows to get rid of
	 * warning about unsupported script tags in {@link CrossSiteIframeLinker}
	 */
	private static final String PROPERTY_REMOVE_SCRIPT_REFS = "si.removeScriptRefs";

	protected boolean mRemoveScriptRefs = true;
	protected String mScriptRefsFileName;

	@Override
	public String getDescription() {
		return "Script-Injector";
	}

	@Override
	public ArtifactSet link(TreeLogger logger, LinkerContext context,
			ArtifactSet artifacts, boolean onePermutation)
			throws UnableToCompleteException {

		if (onePermutation) {
			return artifacts;
		}

		for (ConfigurationProperty prop : context.getConfigurationProperties()) {
			if (prop.getName().equalsIgnoreCase(PROPERTY_SCRIPT_REFS_FILE_NAME)) {
				mScriptRefsFileName = prop.getValues().get(0);
			}

			if (prop.getName().equalsIgnoreCase(PROPERTY_REMOVE_SCRIPT_REFS)) {
				if (prop.getValues().get(0).equalsIgnoreCase("false")) {
					mRemoveScriptRefs = false;
				}
			}
		}

		ArtifactSet toReturn = new ArtifactSet(artifacts);
		logger.log(Type.INFO, "Emitting " + mScriptRefsFileName + " file");

		SortedSet<ScriptReference> scripts = artifacts
				.find(ScriptReference.class);

		List<String> list = new ArrayList<>();

		for (ScriptReference script : scripts) {
			list.add(script.getSrc());
		}
		
		if (mRemoveScriptRefs) {
			toReturn.removeAll(scripts);
		}

		String fileContent = generateScriptRefsFile(logger, context, list);

		SyntheticArtifact s = emitString(logger, fileContent,
				mScriptRefsFileName);
		toReturn.add(s);

		return toReturn;
	}

	/**
	 * Generates json file with list of scripts to load
	 * 
	 * @param logger
	 * @param context
	 * @param list
	 * @return
	 */
	protected String generateScriptRefsFile(TreeLogger logger,
			LinkerContext context, List<String> list) {
		StringBuilder sb = new StringBuilder();

		sb.append("{ \"scriptRefs\": [\n");

		int size = list.size();
		for (int i = 0; i < size; i++) {

			sb.append("\t\"").append(list.get(i)).append("\"");

			if (i < size - 1) {
				sb.append(",");
			}

			sb.append("\n");
		}

		sb.append("]}");

		return sb.toString();
	}
}
