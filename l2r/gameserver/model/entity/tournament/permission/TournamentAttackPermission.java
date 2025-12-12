package l2r.gameserver.model.entity.tournament.permission;

import l2r.gameserver.data.StringHolder;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.tournament.BattleInstance;
import l2r.gameserver.model.entity.tournament.Team;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.IStaticPacket;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.permission.actor.AttackPermission;

public class TournamentAttackPermission implements AttackPermission
{
	private final BattleInstance battleInstance;
	
	public TournamentAttackPermission(BattleInstance battleInstance)
	{
		super();
		this.battleInstance = battleInstance;
	}
	
	@Override
	public boolean canAttack(Creature actor, Creature target, Skill skill, boolean force)
	{
		if (!battleInstance.isFightTime())
		{
			return false;
		}
		final Player pcAttacker = actor.getPlayer();
		if (pcAttacker == null)
		{
			return false;
		}
		final Team attackerTeam = battleInstance.getBattleRecord().getTeam(pcAttacker);
		if (attackerTeam == null)
		{
			return false;
		}
		final Player pcTarget = target.getPlayer();
		if (pcTarget == null)
		{
			return false;
		}
		final Team targetTeam = battleInstance.getBattleRecord().getTeam(pcTarget);
		return (targetTeam != null) && (attackerTeam.getId() != targetTeam.getId());
	}
	
	@Override
	public IStaticPacket getPermissionDeniedError(Creature actor, Creature target, Skill skill, boolean force)
	{
		if (actor.isPlayable())
		{
			return new SystemMessage(StringHolder.getNotNull(actor.getPlayer().getLanguage(), "Tournament.NotAllowed.AttackBetweenFights", new Object[0]));
		}
		return SystemMsg.INVALID_TARGET;
	}
}
