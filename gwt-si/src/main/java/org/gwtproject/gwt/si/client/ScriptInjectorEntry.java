package org.gwtproject.gwt.si.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.gwtproject.gwt.si.client.StatsEvent.*;

import org.gwtproject.gwt.si.client.ScriptRefsLoaded.Entry;
import org.gwtproject.gwt.si.client.ScriptRefsLoaded.Event;
import org.gwtproject.gwt.si.client.ScriptRefsLoaded.Listener;
import org.gwtproject.gwt.si.client.ScriptRefsLoaded.LoadEvent;
import org.gwtproject.gwt.si.client.ScriptRefsLoaded.ProgressEvent;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
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

	/**
	 * Make use of browser cache & multiply connections - first pre-load all
	 * scripts at once and then add them one by one to DOM
	 * 
	 * Not very useful with SDM since code server adds 'Pragma: no-cache'
	 * 
	 * @return
	 */
	protected boolean preloadWithAjax() {
		return false;
	}

	/**
	 * If {@link #preloadWithAjax()} returns true and this method too, then
	 * scripts won't be included using urls but as strings
	 * 
	 * @return
	 * @see ScriptInjector.FromString
	 */
	protected boolean includeAsString() {
		return false;
	}

	/**
	 * Create a split point when calling {@link #onScriptLoad()}
	 * 
	 * @return
	 */
	protected boolean runScriptLoadAsync() {
		return false;
	}

	/**
	 * If true script tag will be removed DOM after it has been loaded.
	 * 
	 * @return
	 */
	protected boolean removeTag() {
		return false;
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
				// still loading, listen for events
				sr.addListener(new Listener() {

					@Override
					public void onEvent(Event event) {
						switch (event.getType()) {

						case ProgressEvent.TYPE:
							ProgressEvent pe = event.cast();
							onScriptProgress(pe.getDone(), pe.getTotal());
							break;
						case LoadEvent.TYPE:
							onScriptLoad0();
							break;
						}
					}
				});
			}
		} else {
			// take care of loading the scripts, create a lock
			mScriptRefsLoaded.setScriptRef(getFilePath(), Entry.newEntry());

			// defer so loading of the onModuleLoad returns
			new Timer() {
				@Override
				public void run() {
					fetchScriptRefsFile();
				}
			}.schedule(0);
		}
	}

	private String getFilePath() {
		return GWT.getModuleBaseForStaticFiles() + getScriptRefsFileName();
	}

	private void fetchScriptRefsFile() {
		triggerStatsEvent("begin");

		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
				getFilePath());
		rb.setCallback(new RequestCallback() {

			@Override
			public void onResponseReceived(Request request, Response response) {
				int status = response.getStatusCode();

				if (status == Response.SC_OK) {
					String text = response.getText();

					parseScriptsFile0(text);
				} else {
					onScriptError(new Exception("Unable to fetch "
							+ getScriptRefsFileName() + " (HTTP Status: "
							+ status + ")"));
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

			triggerStatsEvent("end");

			onScriptLoad0();

			// let know other entry points waiting for scripts from
			// the same file
			LoadEvent ev = LoadEvent.newLoadEvent();
			e.fireListeners(ev);

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
		fu.setRemoveTag(removeTag());
		fu.setCallback(new Callback<Void, Exception>() {

			@Override
			public void onSuccess(Void result) {
				scripts.poll();

				mScriptsLoaded.setScript(script, true);

				int done = total - scripts.size();

				onScriptProgress(done, total);
				triggerStatsEvent("progress(" + done + "," + total + ")");

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
	 * 
	 * @param done
	 * @param total
	 */
	protected void onScriptProgress(int done, int total) {

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
		UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
		if (ueh != null) {
			ueh.onUncaughtException(e);
		}
	}
}
