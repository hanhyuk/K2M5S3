package scripting;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import server.quest.MapleQuest;

public class NPCScriptManager extends AbstractScriptManager {
	private static final Logger logger = LoggerFactory.getLogger(NPCScriptManager.class);

	private final Map<MapleClient, NPCConversationManager> cms = new WeakHashMap<MapleClient, NPCConversationManager>();
	private final Map<MapleClient, Invocable> scripts = new WeakHashMap<MapleClient, Invocable>();
	private static final NPCScriptManager instance = new NPCScriptManager();
	private static ReentrantLock lock = new ReentrantLock();

	public static final NPCScriptManager getInstance() {
		return instance;
	}

	public final void start(final MapleClient c, final int npc) {
		start(c, npc, null);
	}

	public final void start(final MapleClient c, final int npc, final String script) {
		lock.lock();
		try {
			if (!(cms.containsKey(c) && scripts.containsKey(c))) {
				Invocable iv;
				if (script == null) {
					iv = getInvocable("npc/" + npc + ".js", c);
				} else {
					iv = getInvocable("item/" + script + ".js", c);
				}
				if (iv == null && script != null) {
					iv = getInvocable("npc/" + script + ".js", c);
				}
				if (iv == null) {
					iv = getInvocable("npc/npcAutoWriter.js", c);
				}
				final ScriptEngine scriptengine = (ScriptEngine) iv;
				final NPCConversationManager cm = new NPCConversationManager(c, npc, -1, (byte) -1);

				cms.put(c, cm);
				scriptengine.put("cm", cm);

				c.getPlayer().setConversation(1);

				scripts.put(c, iv);
				try {
					iv.invokeFunction("start"); // Temporary until I've removed
												// all of start
				} catch (NoSuchMethodException nsme) {
					iv.invokeFunction("action", (byte) 1, (byte) 0, 0);
				}
			}
		} catch (Exception e) {
			logger.debug("Error executing NPC script, NPC ID : {} {}", npc, e);
			dispose(c);
		} finally {
			lock.unlock();
		}
	}

	public final void action(final MapleClient c, final byte mode, final byte type, final int selection) {
		if (mode != -1) {
			try {
				if (cms.get(c).pendingDisposal) {
					dispose(c);
				} else {
					scripts.get(c).invokeFunction("action", mode, type, selection);
				}
			} catch (final Exception e) {
				logger.debug("Error executing NPC script {}", e);
				dispose(c);
			}
		}
	}

	public final void startQuest(final MapleClient c, final int npc, final int quest) {
		if (!MapleQuest.getInstance(quest).canStart(c.getPlayer(), null)) {
			return;
		}
		lock.lock();
		try {
			if (!(cms.containsKey(c) && scripts.containsKey(c))) {
				final Invocable iv = getInvocable("quest/" + quest + ".js", c);
				final ScriptEngine scriptengine = (ScriptEngine) iv;
				if (iv == null) {
					if (MapleQuest.getInstance(quest).getMedalItem() > 0 && MapleQuest.getInstance(quest).getMedalItem() != 1142249) {
						c.getPlayer().gainMedalReward(MapleQuest.getInstance(quest).getMedalItem());
						MapleQuest.getInstance(quest).forceComplete(c.getPlayer(), quest);
						return;
					}
					return;
				}
				final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte) 0);
				cms.put(c, cm);
				scriptengine.put("qm", cm);

				c.getPlayer().setConversation(1);
				scripts.put(c, iv);
				iv.invokeFunction("start", (byte) 1, (byte) 0, 0); // start it
																	// off as
																	// something
			}
		} catch (final Exception e) {
			logger.debug("Error executing NPC script, NPC ID : {} {}", npc, e);
			dispose(c);
		} finally {
			lock.unlock();
		}
	}

	public final void startQuest(final MapleClient c, final byte mode, final byte type, final int selection) {
		try {
			if (cms.get(c).pendingDisposal) {
				dispose(c);
			} else {
				scripts.get(c).invokeFunction("start", mode, type, selection);
			}
		} catch (Exception e) {
			logger.debug("Error executing NPC script {}", e);
			dispose(c);
		}
	}

	public final void endQuest(final MapleClient c, final int npc, final int quest, final boolean customEnd) {
		if (!customEnd && !MapleQuest.getInstance(quest).canComplete(c.getPlayer(), null)) {
			return;
		}
		lock.lock();
		try {
			if (!(cms.containsKey(c) && scripts.containsKey(c))) {
				final Invocable iv = getInvocable("quest/" + quest + ".js", c);
				final ScriptEngine scriptengine = (ScriptEngine) iv;
				if (iv == null) {
					dispose(c);
					return;
				}
				final NPCConversationManager cm = new NPCConversationManager(c, npc, quest, (byte) 1);
				cms.put(c, cm);
				scriptengine.put("qm", cm);
				c.getPlayer().setConversation(1);
				scripts.put(c, iv);
				iv.invokeFunction("end", (byte) 1, (byte) 0, 0); // start it off
																	// as
																	// something
			}
		} catch (Exception e) {
			dispose(c);
		} finally {
			lock.unlock();
		}
	}

	public final void endQuest(final MapleClient c, final byte mode, final byte type, final int selection) {
		try {
			if (cms.get(c).pendingDisposal) {
				dispose(c);
			} else {
				scripts.get(c).invokeFunction("end", mode, type, selection);
			}
		} catch (Exception e) {
			logger.debug("{}", e);
			dispose(c);
		}
	}

	public final void dispose(final MapleClient c) {
		final NPCConversationManager npccm = cms.get(c);
		if (npccm != null) {
			cms.remove(npccm.getC());
			scripts.remove(npccm.getC());
			if (npccm.getType() == -1) {
				c.removeScriptEngine("Scripts/npc/" + npccm.getNpc() + ".js");
			} else {
				c.removeScriptEngine("Scripts/quest/" + npccm.getQuest() + ".js");
			}
		}
		if (c.getPlayer().getConversation() == 1) {
			c.getPlayer().setConversation(0);
		}
	}

	public final NPCConversationManager getCM(final MapleClient c) {
		return cms.get(c);
	}

	public final void clearScript() {
		cms.clear();
		scripts.clear();
	}
}
