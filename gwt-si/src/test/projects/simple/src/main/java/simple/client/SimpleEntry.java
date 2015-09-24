package simple.client;

import org.gwtproject.gwt.si.client.ScriptInjectorEntry;

import com.google.gwt.user.client.Window;

public class SimpleEntry extends ScriptInjectorEntry {

	@Override
	protected void onScriptLoad() {
		doSth();
	}

	@Override
	protected void onScriptError(Exception e) {
		Window.alert(e.getMessage());
	}

	private static native void doSth()/*-{
		new $wnd.SomeScript().doSth();
	}-*/;

}
