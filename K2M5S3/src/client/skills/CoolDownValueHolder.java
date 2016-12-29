package client.skills;

import java.util.concurrent.ScheduledFuture;

public class CoolDownValueHolder {

	public int skillId;
	public long startTime;
	public long length;
	public ScheduledFuture<?> timer;

	public CoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
		super();
		this.skillId = skillId;
		this.startTime = startTime;
		this.length = length;
		this.timer = timer;
	}
}
