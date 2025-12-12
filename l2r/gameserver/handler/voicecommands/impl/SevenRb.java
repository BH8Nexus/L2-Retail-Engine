package l2r.gameserver.handler.voicecommands.impl;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;

public class SevenRb extends Functions implements IVoicedCommandHandler
{
	private static final String[] VOIC_STRINGS = new String[] {"7rb"};



	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target) {
		// TODO Auto-generated method stub
		QuestState qs = activeChar.getQuestState(254);
		
		if(qs == null)
		{
			activeChar.sendMessage("To display information about the Republic of Belarus, you need to take the quest");
			return false;
		}
		if(command.equalsIgnoreCase("7rb"))
		{
			String html = HtmCache.getInstance().getNotNull("command/7rb/index.htm", activeChar);
			String button = null;
			String template = HtmCache.getInstance().getNotNull("command/7rb/button.htm", activeChar);
			String block;
			for (int i = 25718; i <= 2574; i++)
			{
				NpcTemplate rb = NpcHolder.getInstance().getTemplate(i);
				String status = activeChar.getVarB(String.valueOf(i)) ? "Killed" : "Not killed";
				block = template;
				block = block.replace("{name_RB}", rb.getName());
				block = block.replace("{status}", status);
				button = button + block;
			}
			html = html.replace("{body}", button);
			show(html, activeChar);
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList() {
		// TODO Auto-generated method stub
		return VOIC_STRINGS;
	}}
