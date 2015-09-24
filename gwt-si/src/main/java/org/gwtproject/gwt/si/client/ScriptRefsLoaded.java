package org.gwtproject.gwt.si.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

public class ScriptRefsLoaded extends JavaScriptObject {

	public static class Entry extends JavaScriptObject {
		protected Entry() {

		}

		public static Entry newEntry() {
			Entry e = JavaScriptObject.createObject().cast();
			e.setLoaded(false);
			e.clearListeners();

			return e;
		}

		public final native boolean isLoaded()/*-{
			return this.loaded;
		}-*/;

		public final native void setLoaded(boolean loaded)/*-{
			this.loaded = loaded;
		}-*/;
		
		public final native void clearListeners()/*-{
			this.listeners = [];
		}-*/;
		
		public final void addListener(EventListener el) {
			getListeners().push(getFunction(el));
		}
		
		public final void fireListeners(Event e) {
			JsArray<JavaScriptObject> listeners = getListeners();
			for(int i = 0; i < listeners.length(); i++) {
				JavaScriptObject listener = listeners.get(i);
				callFunction(listener, e);
			}
		}

		private final native JsArray<JavaScriptObject> getListeners()/*-{
			return this.listeners;
		}-*/;

		private final native JavaScriptObject getFunction(EventListener listener)/*-{
			var l = listener;

			var f = function(e) {
				l.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(e);
			};

			return $entry(f);
		}-*/;
		
		private final native void callFunction(JavaScriptObject f, Event e)/*-{
			f(e);
		}-*/;

	}

	protected ScriptRefsLoaded() {

	}

	public static final native ScriptRefsLoaded get()/*-{
		if (!$wnd.__gwt_scriptRefsLoaded) {
			$wnd.__gwt_scriptRefsLoaded = {};
		}

		return $wnd.__gwt_scriptRefsLoaded;
	}-*/;

	public final native boolean hasScriptRef(String path)/*-{
		return typeof this[path] !== 'undefined';
	}-*/;

	public final native Entry getScriptRef(String path)/*-{
		return this[path];
	}-*/;

	public final native void setScriptRef(String path, Entry e)/*-{
		this[path] = e;
	}-*/;
}
