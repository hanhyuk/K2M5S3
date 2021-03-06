package scripting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import launch.ChannelServer;

public class EventScriptManager extends AbstractScriptManager {
	private static final Logger logger = LoggerFactory.getLogger(EventScriptManager.class);
	
	private class EventEntry {

		public EventEntry(final String script, final Invocable iv, final EventManager em) {
			this.script = script;
			this.iv = iv;
			this.em = em;
		}

		public String script;
		public Invocable iv;
		public EventManager em;
	}

	private final Map<String, EventEntry> events = new LinkedHashMap<String, EventEntry>();
	private final AtomicInteger runningInstanceMapId = new AtomicInteger(0);

	public final int getNewInstanceMapId() {
		return runningInstanceMapId.addAndGet(1);
	}

	public EventScriptManager(final ChannelServer cserv, final String[] scripts) {
		super();
		for (final String script : scripts) {
			if (!script.equals("")) {
				final Invocable iv = getInvocable("event/" + script + ".js", null);

				if (iv != null) {
					events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
				}
			}
		}
	}

	public final EventManager getEventManager(final String event) {
		final EventEntry entry = events.get(event);
		if (entry == null) {
			return null;
		}
		return entry.em;
	}

	public final void init() {
		for (final EventEntry entry : events.values()) {
			try {
				((ScriptEngine) entry.iv).put("em", entry.em);
				entry.iv.invokeFunction("init", (Object) null);
			} catch (final ScriptException e) {
				logger.debug("{}", e);
			} catch (final NoSuchMethodException e) {
				logger.debug("{}", e);
			}
		}
	}

	public final void cancel() {
		for (final EventEntry entry : events.values()) {
			entry.em.cancel();
		}
	}
}
