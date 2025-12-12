package l2r.gameserver.permission.actor;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.permission.CharPermission;

public interface UseSkillPermission extends CharPermission
{
	boolean canUseSkill(Creature p0, Creature p1, Skill p2);

	void sendPermissionDeniedError(Creature p0, Creature p1, Skill p2);
}
