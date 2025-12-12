package l2r.gameserver.taskmanager.actionrunner.tasks;

import l2r.commons.lang.reference.HardReference;
import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Config;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.model.Creature;

public class NotifyAITask extends RunnableImpl
{
	private final CtrlEvent _evt;
	private final Object _agr0;
	private final Object _agr1;
	private final HardReference<? extends Creature> _charRef;
	
	public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1)
	{
		_charRef = cha.getRef();
		_evt = evt;
		_agr0 = agr0;
		_agr1 = agr1;
	}
	
	public NotifyAITask(Creature cha, CtrlEvent evt)
	{
		this(cha, evt, null, null);
	}
	
	@Override
	public void runImpl()
	{
		Creature character = _charRef.get();
		if ((character == null) || !character.hasAI() || !Config.ALLOW_NPC_AIS)
		{
			return;
		}
		
		character.getAI().notifyEvent(_evt, _agr0, _agr1);
	}
}