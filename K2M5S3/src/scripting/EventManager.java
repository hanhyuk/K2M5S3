package scripting;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import community.MapleParty;
import community.MapleSquadLegacy;
import constants.ServerConstants;
import launch.ChannelServer;
import packet.creators.MainPacketCreator;
import server.life.MapleLifeProvider;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleWorldMapProvider;
import tools.Timer.EventTimer;

public class EventManager {
	private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
	
	private Invocable iv;
	private ChannelServer cserv;
	private WeakHashMap<String, EventInstanceManager> instances = new WeakHashMap<String, EventInstanceManager>();
	private Properties props = new Properties();
	private String name;

	public EventManager(ChannelServer cserv, Invocable iv, String name) {
		this.iv = iv;
		this.cserv = cserv;
		this.name = name;
	}

	public void cancel() {
		try {
			iv.invokeFunction("cancelSchedule", (Object) null);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	public void schedule(final String methodName, long delay) {
		EventTimer.getInstance().schedule(new Runnable() {

			public void run() {
				try {
					iv.invokeFunction(methodName, (Object) null);
				} catch (ScriptException ex) {
					logger.debug("method Name : {}, {}", methodName, ex);
				} catch (NoSuchMethodException ex) {
					logger.debug("method Name : {}, {}", methodName, ex);
				}
			}
		}, delay);
	}

	public void schedule(final String methodName, long delay, final EventInstanceManager eim) {
		EventTimer.getInstance().schedule(new Runnable() {

			public void run() {
				try {
					iv.invokeFunction(methodName, eim);
				} catch (ScriptException ex) {
					logger.debug("method Name : {}, {}", methodName, ex);
				} catch (NoSuchMethodException ex) {
					logger.debug("method Name : {}, {}", methodName, ex);
				}
			}
		}, delay);
	}

	public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
		return EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {

			public void run() {
				try {
					iv.invokeFunction(methodName, (Object) null);
				} catch (ScriptException e) {
					logger.debug("{}", e);
				} catch (NoSuchMethodException e) {
					logger.debug("{}", e);
				}
			}
		}, timestamp);
	}

	public ChannelServer getChannelServer() {
		return cserv;
	}

	public EventInstanceManager getInstance(String name) {
		return instances.get(name);
	}

	public Collection<EventInstanceManager> getInstances() {
		return Collections.unmodifiableCollection(instances.values());
	}

	public EventInstanceManager newInstance(String name) {
		EventInstanceManager ret = new EventInstanceManager(this, name, new MapleWorldMapProvider());
		instances.put(name, ret);
		return ret;
	}

	public void disposeInstance(String name) {
		instances.remove(name);
	}

	public Invocable getIv() {
		return iv;
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public String getName() {
		return name;
	}

	public void startInstance() {
		try {
			iv.invokeFunction("setup", (Object) null);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	public void startInstance_Solo(String mapid, MapleCharacter chr) {
		try {
			EventInstanceManager eim = (EventInstanceManager) iv.invokeFunction("setup", (Object) mapid);
			eim.registerPlayer(chr);
		} catch (Exception ex) {
			ex.printStackTrace();
			// FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Event name : " +
			// name + ", method Name : setup:\n" + ex);
		}
	}

	public void startInstance(MapleCharacter character) {
		try {
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
			eim.registerPlayer(character);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	// PQ method: starts a PQ
	public void startInstance(MapleParty party, MapleMap map) {
		try {
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
			eim.registerParty(party, map);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	// PQ method: ready a PQ _ Give Parameters
	public EventInstanceManager readyInstance() {
		try {
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
			return eim;
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
		return null;
	}

	// non-PQ method for starting instance
	public void startInstance(EventInstanceManager eim, String leader) {
		try {
			iv.invokeFunction("setup", eim);
			eim.setProperty("leader", leader);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	public void startInstance(MapleSquadLegacy squad, MapleMap map) {
		try {
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", squad.getLeader().getId()));
			eim.registerSquad(squad, map);
		} catch (ScriptException e) {
			logger.debug("{}", e);
		} catch (NoSuchMethodException e) {
			logger.debug("{}", e);
		}
	}

	public void warpAllPlayer(int from, int to) {
		final MapleMap tomap = cserv.getMapFactory().getMap(to);
		for (MapleMapObject mmo : cserv.getMapFactory().getMap(from).getAllPlayer()) {
			((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
		}
	}

	public MapleWorldMapProvider getMapFactory(int world) {
		return cserv.getMapFactory(world);
	}

	public OverrideMonsterStats newMonsterStats() {
		return new OverrideMonsterStats();
	}

	public MapleMonster getMonster(final int id) {
		return MapleLifeProvider.getMonster(id);
	}

	public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
		if (!weather) {
			cserv.broadcastPacket(MainPacketCreator.serverNotice(type, msg));
		} else {
			for (Entry<Integer, MapleMap> map : cserv.getMapFactory().getMaps().entrySet()) {
				final MapleMap load = map.getValue();
				if (load.getCharactersSize() > 0) {
					load.startMapEffect(msg, type);
				}
			}
		}
	}
}
