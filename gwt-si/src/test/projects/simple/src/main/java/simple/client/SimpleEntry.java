package simple.client;

import org.gwtproject.gwt.si.client.ScriptInjectorEntry;

public class SimpleEntry extends ScriptInjectorEntry {

	@Override
	protected void onScriptLoad() {
		doSth();
	}

	private static native void doSth()/*-{
		new $wnd.SomeScript().doSth();
	}-*/;

}
