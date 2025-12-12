package l2r.gameserver.network.clientpackets;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.xml.holder.SkillAcquireHolder;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.base.EnchantSkillLearn;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2r.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SkillTreeTable;
import l2r.gameserver.utils.Log;
import l2r.gameserver.utils.Util;

public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isBusy())
		{
			return;
		}
		
		if ((activeChar.getTransformation() != 0) || activeChar.isMounted() || Olympiad.isRegisteredInComp(activeChar) || activeChar.isInCombat())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillRouteChange.message1", activeChar));
			return;
		}
		
		if ((activeChar.getLevel() < 76) || (activeChar.getClassId().getLevel() < 4))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.RequestExEnchantSkillRouteChange.message2", activeChar));
			return;
		}
		
		EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
		if (sl == null)
		{
			return;
		}
		
		int slevel = activeChar.getSkillDisplayLevel(_skillId);
		if (slevel == -1)
		{
			return;
		}
		
		if ((slevel <= sl.getBaseLevel()) || ((slevel % 100) != (_skillLvl % 100)))
		{
			return;
		}
		
		int dispSkillLevel = activeChar.getSkillDisplayLevel(_skillId);
		if (((_skillLvl / 100L) == (dispSkillLevel / 100L)) || ((dispSkillLevel % 100) != (_skillLvl % 100)))
		{
			Util.handleIllegalPlayerAction(activeChar, "tried to use enchant root change bug", "RequestExEnchantSkillRouteChange[71]", 0);
			activeChar.sendPacket(Msg.SKILL_NOT_AVAILABLE_TO_BE_ENHANCED_CHECK_SKILL_S_LV_AND_CURRENT_PC_STATUS);
			return;
		}
		
		int[] cost = sl.getCost();
		int requiredSp = (cost[1] * sl.getCostMult()) / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
		int requiredAdena = (cost[0] * sl.getCostMult()) / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
		
		if (activeChar.getSp() < requiredSp)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if (activeChar.getAdena() < requiredAdena)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		if (Functions.getItemCount(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK) == 0)
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		Functions.removeItem(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK, 1, "");
		Functions.removeItem(activeChar, 57, requiredAdena, "");
		activeChar.addExpAndSp(0, -1 * requiredSp);
		
		int levelPenalty = Rnd.get(Math.min(4, _skillLvl % 100));
		
		if ((_skillLvl % 100) != (activeChar.getSkillDisplayLevel(_skillId) % 100))
		{
			activeChar.sendMessage("Incorrect enchant level.");
			return;
		}
		
		_skillLvl -= levelPenalty;
		if ((_skillLvl % 100) == 0)
		{
			_skillLvl = sl.getBaseLevel();
		}
		
		Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));
		
		if (!SkillAcquireHolder.getInstance().isSkillPossible(activeChar, skill))
		{
			activeChar.sendMessage("Skill cannot be enchanted from this current class, please switch to class it belong.");
			return;
		}
		
		if (skill != null)
		{
			activeChar.addSkill(skill, true);
		}
		
		if (levelPenalty == 0)
		{
			SystemMessage2 sm = new SystemMessage2(SystemMsg.S1S_AUCTION_HAS_ENDED);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage2 sm = new SystemMessage2(SystemMsg.S1S2S_AUCTION_HAS_ENDED);
			sm.addSkillName(_skillId, _skillLvl);
			sm.addInteger(levelPenalty);
			activeChar.sendPacket(sm);
		}
		
		Log.add(activeChar.getName() + "|Successfully changed route|" + _skillId + "|" + slevel + "|to+" + _skillLvl + "|" + levelPenalty, "enchant_skills");
		
		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)), new ExEnchantSkillResult(1));
		RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
		
		// In retail server there is a bug when you enchant a skill, its reuse gets reset if you try to use it from a macro.
		if (!Config.ALLOW_MACROS_ENCHANT_BUG)
		{
			TimeStamp oldSkillReuse = activeChar.getSkillReuses().stream().filter(ts -> ts.getId() == _skillId).findFirst().orElse(null);
			if (oldSkillReuse != null)
			{
				activeChar.disableSkill(skill, oldSkillReuse.getReuseCurrent());
			}
		}
	}
}