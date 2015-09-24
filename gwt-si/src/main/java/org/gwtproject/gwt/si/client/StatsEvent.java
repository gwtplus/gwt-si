package org.gwtproject.gwt.si.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

class StatsEvent extends JavaScriptObject {
	protected StatsEvent() {

	}
	
	public static StatsEvent newStatsEvent(){
		return JavaScriptObject.createObject().cast();
	}
	
	public static void triggerStatsEvent(String type){
		StatsEvent e = newStatsEvent();
		
		e.setModuleName(GWT.getModuleName());
		e.setSubSystem("startup");
		e.setEvtGroup("loadExternalRefs");
		e.setMillis();
		e.setType(type);
		
		e.trigger();
	}

	public final native void setModuleName(String s)/*-{
		this.moduleName = s;
	}-*/;
	
	public final native void setSubSystem(String s)/*-{
		this.subSystem = s;
	}-*/;
	
	public final native void setEvtGroup(String s)/*-{
		this.evtGroup = s;
	}-*/;
	
	public final native void setMillis()/*-{
		this.millis = new Date().getTime();
	}-*/;
	
	public final native void setType(String type)/*-{
		this.type = type;
	}-*/;
	
	public final native void trigger()/*-{
		if($wnd.__gwtStatsEvent) {
			$wnd.__gwtStatsEvent(this);
		}
	}-*/;
}
