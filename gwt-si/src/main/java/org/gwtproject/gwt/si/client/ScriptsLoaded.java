package org.gwtproject.gwt.si.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ScriptsLoaded extends JavaScriptObject {

	protected ScriptsLoaded() {

	}

	public static final native ScriptsLoaded get()/*-{
		if (!$wnd.__gwt_scriptsLoaded) {
			$wnd.__gwt_scriptsLoaded = {};
		}
		
		return $wnd.__gwt_scriptsLoaded;
	}-*/;

	public final native boolean hasScript(String path)/*-{
		return typeof this[path] !== 'undefined';
	}-*/;

	public final native boolean getScript(String path)/*-{
		return this[path];
	}-*/;

	public final native void setScript(String path, boolean loaded)/*-{
		this[path] = loaded;
	}-*/;
}
