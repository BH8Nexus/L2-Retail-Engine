package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;

public class SkillUse extends Functions implements IVoicedCommandHandler
{
    private static final String[] _commandList = {};

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String args)
    {
        int skills = Integer.parseInt(args);

        Skill skill = SkillTable.getInstance().getInfo(skills, activeChar.getSkillLevel(Integer.valueOf(skills)));

        String sk = "/useskill " + skill.getName();
        Say2 cs = new Say2(activeChar.getObjectId(), ChatType.ALL, activeChar.getName(), sk);

        activeChar.setMacroSkill(skill);
        activeChar.sendPacket(cs);

        return true;
    }

    @Override
    public String[] getVoicedCommandList()
    {
        return _commandList;
    }
}