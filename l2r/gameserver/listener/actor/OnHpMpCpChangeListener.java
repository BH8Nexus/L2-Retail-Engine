package l2r.gameserver.listener.actor;

import l2r.gameserver.listener.PlayerListener;
import l2r.gameserver.model.Creature;

public interface OnHpMpCpChangeListener extends PlayerListener
{
	void onHpChange(Creature actor, double oldHp, double newHp);
	
	void onMpChange(Creature actor, double oldMp, double newMp);
	
	void onCpChange(Creature actor, double oldCp, double newCp);
}
