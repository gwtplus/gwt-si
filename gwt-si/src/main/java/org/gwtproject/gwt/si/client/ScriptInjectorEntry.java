package org.gwtproject.gwt.si.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.gwtproject.gwt.si.client.ScriptRefsLoaded.Entry;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.core.client.ScriptInjector.FromUrl;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;

public abstract class ScriptInjectorEntry implements EntryPoint {

	protected ScriptRefsLoaded mScriptRefsLoaded;
	protected ScriptsLoaded mScriptsLoaded;

	@Override
	public void onModuleLoad() {
		onBeforeScriptLoad();

		mScriptRefsLoaded = ScriptRefsLoaded.get();
		mScriptsLoaded = ScriptsLoaded.get();

		maybeFetchScriptRefsFile();
	}

	/**
	 * Name of the file, if null, will be ignored
	 * 
	 * @return
	 */
	protected String getScriptRefsFileName() {
		return "__gwt_scriptRefs.json";
	}

	protected boolean preloadWithAjax() {
		return false;
	}

	protected boolean runScriptLoadAsync() {
		return true;
	}

	private void maybeFetchScriptRefsFile() {
		if (getScriptRefsFileName() == null) {
			onScriptLoad0();
		} else if (mScriptRefsLoaded.hasScriptRef(getFilePath())) {
			Entry sr = mScriptRefsLoaded.getScriptRef(getFilePath());
			if (sr.isLoaded()) {
				// already loaded,
				onScriptLoad0();
			} else {
				// still loading
				sr.addListener(new EventListener() {

					@Override
					public void onBrowserEvent(Event event) {
						onScriptLoad0();
					}
				});
			}
		} else {
			fetchScriptRefsFile();
		}
	}

	private String getFilePath() {
		return GWT.getModuleBaseForStaticFiles() + getScriptRefsFileName();
	}

	private void fetchScriptRefsFile() {
		final String path = getFilePath();

		mScriptRefsLoaded.setScriptRef(path, Entry.newEntry());

		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, path);
		rb.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
				int status = response.getStatusCode();

				if (status == Response.SC_OK) {
					String text = response.getText();

					parseScriptsFile0(text);
				} else {
					onScriptError(new Exception("Wrong status code: " + status));
				}
			}

			@Override
			public void onError(Request request, Throwable exception) {
				onScriptError(new Exception(exception));
			}
		});

		try {
			rb.send();
		} catch (RequestException e) {
			onScriptError(e);
		}
	}

	private void parseScriptsFile0(String text) {
		List<String> scripts = parseScriptsFile(text);

		loadScripts(new LinkedList<>(scripts), scripts.size());
	}

	protected List<String> parseScriptsFile(String text) {
		List<String> scripts = new LinkedList<>();

		ScriptsHolder sh = JsonUtils.safeEval(text);
		JsArrayString arr = sh.getScriptRefs();
		for (int i = 0; i < arr.length(); i++) {
			String p = arr.get(i);
			String fullPath = GWT.getModuleBaseForStaticFiles() + p;
			scripts.add(fullPath);
		}

		return scripts;
	}

	private void loadScripts(final Queue<String> scripts, final int total) {
		if (scripts.isEmpty()) {
			Entry e = mScriptRefsLoaded.getScriptRef(getFilePath());
			e.setLoaded(true);
			// let know other entry points waiting for scripts from
			// the same file
			e.fireListeners(null);

			onScriptLoad0();
			return;
		}

		final String script = scripts.peek();

		// load only missing scripts
		if (mScriptsLoaded.hasScript(script)) {
			boolean loaded = mScriptsLoaded.getScript(script);
			if (loaded) {
				scripts.poll();

				loadScripts(scripts, total);
			} else {
				// still loading
				// wait few ms and try again
				new Timer() {
					@Override
					public void run() {
						loadScripts(scripts, total);
					}
				}.schedule(100);
			}

			return;
		}

		mScriptsLoaded.setScript(script, false);

		FromUrl fu = ScriptInjector.fromUrl(script);
		fu.setWindow(ScriptInjector.TOP_WINDOW);
		fu.setCallback(new Callback<Void, Exception>() {

			@Override
			public void onSuccess(Void result) {
				scripts.poll();

				mScriptsLoaded.setScript(script, true);
				onScriptProgress(total - scripts.size(), total);
				// TODO: support __gwt_statsEvent

				loadScripts(scripts, total);
			}

			@Override
			public void onFailure(Exception reason) {
				onScriptError(reason);
			}
		});

		fu.inject();
	}

	private void onScriptLoad0() {
		if (runScriptLoadAsync()) {
			GWT.runAsync(new RunAsyncCallback() {

				@Override
				public void onSuccess() {
					onScriptLoad();
				}

				@Override
				public void onFailure(Throwable reason) {
					onScriptError(new Exception(reason));
				}
			});
		} else {
			onScriptLoad();
		}
	}

	/**
	 * Called as a first thing in {@link #onModuleLoad()} Place for
	 * initialization that doesn't depend on external JavaScript libraries
	 */
	protected void onBeforeScriptLoad() {
		
	}

	/**
	 * Information about the progress of the loading process
	 * Reported only to single entry point - the one which performs fetching
	 * In case of multiply entry points in the same gwt module, use
	 * common method which is called from all of them.
	 * @param current
	 * @param total
	 */
	protected void onScriptProgress(int current, int total){
		
	}
	
	/**
	 * Called when all JavaScript dependencies have been injected
	 */
	protected abstract void onScriptLoad();

	/**
	 * Called when there was an error while loading JavaScript dependencies
	 * 
	 * @param e
	 */
	protected void onScriptError(Exception e) {
		
	}
}

// $stats = $wnd_0.__gwtStatsEvent?function(a){ return
// $wnd_0.__gwtStatsEvent(a); } : null;
// $stats && $stats({moduleName:'Simple', sessionId:$sessionId_0,
// subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new
// Date).getTime(), type:'begin'});
// $stats && $stats({moduleName:'Simple', sessionId:$sessionId_0,
// subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new
// Date).getTime(), type:'end'});