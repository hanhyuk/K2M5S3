package scripting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import server.maps.MaplePortal;

public class PortalScriptManager {
	private static final Logger logger = LoggerFactory.getLogger(PortalScriptManager.class);
	
	private static final PortalScriptManager instance = new PortalScriptManager();
	private final Map<String, PortalScript> scripts = new HashMap<String, PortalScript>();
	private final static ScriptEngineFactory sef = new ScriptEngineManager().getEngineByName("nashorn").getFactory();

	public final static PortalScriptManager getInstance() {
		return instance;
	}

	private final PortalScript getPortalScript(final String scriptName) {
		if (scripts.containsKey(scriptName)) {
			return scripts.get(scriptName);
		}

		final File scriptFile = new File("Scripts/portal/" + scriptName + ".js");
		if (!scriptFile.exists()) {
			scripts.put(scriptName, null);
			return null;
		}

		FileReader fr = null;
		final ScriptEngine portal = sef.getScriptEngine();
		try {
			fr = new FileReader(scriptFile);
			CompiledScript compiled = ((Compilable) portal).compile(fr);
			compiled.eval();
		} catch (final ScriptException e) {
			logger.debug("{}", e);
		} catch (final IOException e) {
			logger.debug("{}", e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (final IOException e) {
					logger.debug("{}", e);
				}
			}
		}
		final PortalScript script = ((Invocable) portal).getInterface(PortalScript.class);
		scripts.put(scriptName, script);
		return script;
	}

	public final void executePortalScript(final MaplePortal portal, final MapleClient c) {
		final PortalScript script = getPortalScript(portal.getScriptName());

		if (script != null) {
			script.enter(new PortalPlayerInteraction(c, portal));
		} else {
			logger.debug("Unhandled portal script {}", portal.getScriptName());
		}
	}

	public final void clearScripts() {
		scripts.clear();
	}
}
