package org.gwtproject.gwt.si.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

class ScriptRefsLoaded extends JavaScriptObject {

	static class Entry extends JavaScriptObject {
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

		public final void addListener(Listener el) {
			getListeners().push(getFunction(el));
		}

		public final void fireListeners(Event e) {
			JsArray<JavaScriptObject> listeners = getListeners();
			for (int i = 0; i < listeners.length(); i++) {
				JavaScriptObject listener = listeners.get(i);
				callFunction(listener, e);
			}
		}

		private final native JsArray<JavaScriptObject> getListeners()/*-{
			return this.listeners;
		}-*/;

		private final native JavaScriptObject getFunction(Listener listener)/*-{
			var l = listener;

			var f = function(e) {
				l.@org.gwtproject.gwt.si.client.ScriptRefsLoaded.Listener::onEvent(Lorg/gwtproject/gwt/si/client/ScriptRefsLoaded$Event;)(e);
			};

			return $entry(f);
		}-*/;

		private final native void callFunction(JavaScriptObject f, Event e)/*-{
			f(e);
		}-*/;
	}

	static interface Listener {
		void onEvent(Event e);
	}

	static class Event extends JavaScriptObject {
		protected Event() {

		}

		public final native String getType()/*-{
			return this.type;
		}-*/;

		public final native void setType(String type)/*-{
			this.type = type;
		}-*/;
	}

	static class ProgressEvent extends Event {

		public static final String TYPE = "progress";

		protected ProgressEvent() {

		}

		public static ProgressEvent newProgressEvent(int done, int total) {
			ProgressEvent e = JavaScriptObject.createObject().cast();
			e.setType(TYPE);
			e.setDone(done);
			e.setTotal(total);

			return e;
		}

		public final native int getTotal()/*-{
			return this.total;
		}-*/;

		public final native void setTotal(int total)/*-{
			this.total = total;
		}-*/;

		public final native int getDone()/*-{
			return this.done;
		}-*/;

		public final native void setDone(int done)/*-{
			this.total = done;
		}-*/;
	}

	static class LoadEvent extends Event {

		public static final String TYPE = "load";

		protected LoadEvent() {

		}

		public static LoadEvent newLoadEvent() {
			LoadEvent e = JavaScriptObject.createObject().cast();
			e.setType(TYPE);

			return e;
		}
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
