# gwt-si
GWT Script Injector

Custom Linker and EntryPoint implementation that takes care of loading scripts referenced in gwt.xml files. Useful when using xsiframe linker.

Notes
 * linker gets all the script references, by default removes them from ArtifactSet and places them in json file so no need for 'xsiframe.failIfScriptTag'
 * entry point reads the file and loads them into the web page using ScriptInjector class. Adheres to __gwt_scriptsLoaded contract. Triggers __gwtStatsEvents. For now ajax preload is not supported.

Getting started:
 * import and extend 'ScriptInjectorEntry' instead of implementing 'EntryPoint'
 * change 'onModuleLoad' into 'onScriptLoad'
 * optionally move some GWT only initialization independent from 3rd party scripts to onBeforeScriptLoad (like setting UEH)
 * optionally extend other methods to configure loading process or to get notified about progress / errors

This project is experimental. Works in FF and Chrome. Not tested with IE/Edge, Safari or mobile browsers.
