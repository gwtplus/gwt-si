package org.gwtproject.gwt.si.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ScriptsHolder extends JavaScriptObject {
	
	protected ScriptsHolder() {
		
	}
	
	public final native JsArrayString getScriptRefs()/*-{
		return this.scriptRefs;
	}-*/;
}
