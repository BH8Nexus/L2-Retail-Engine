package l2r.gameserver.handler.voicecommands;

import java.util.HashMap;
import java.util.Map;

import l2r.commons.data.xml.AbstractHolder;
import l2r.gameserver.Config;
import l2r.gameserver.handler.voicecommands.impl.ACP;
import l2r.gameserver.handler.voicecommands.impl.CFlag;
import l2r.gameserver.handler.voicecommands.impl.CWHPrivileges;
import l2r.gameserver.handler.voicecommands.impl.CastleInfo;
import l2r.gameserver.handler.voicecommands.impl.Cfg;
import l2r.gameserver.handler.voicecommands.impl.DonateVC;
import l2r.gameserver.handler.voicecommands.impl.DressMe;
import l2r.gameserver.handler.voicecommands.impl.FacebookVoice;
import l2r.gameserver.handler.voicecommands.impl.Hellbound;
import l2r.gameserver.handler.voicecommands.impl.Offline;
import l2r.gameserver.handler.voicecommands.impl.Offlinebuff;
import l2r.gameserver.handler.voicecommands.impl.Online;
import l2r.gameserver.handler.voicecommands.impl.PFlag;
import l2r.gameserver.handler.voicecommands.impl.RandomCommands;
import l2r.gameserver.handler.voicecommands.impl.RegStriderRace;
import l2r.gameserver.handler.voicecommands.impl.Repair;
import l2r.gameserver.handler.voicecommands.impl.SevenRb;
import l2r.gameserver.handler.voicecommands.impl.SkillUse;
import l2r.gameserver.handler.voicecommands.impl.Vote;
import l2r.gameserver.handler.voicecommands.impl.Wedding;

public class VoicedCommandHandler extends AbstractHolder
{
	private static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	
	public static VoicedCommandHandler getInstance()
	{
		return _instance;
	}
	
	private final Map<String, IVoicedCommandHandler> _datatable = new HashMap<>();
	
	private VoicedCommandHandler()
	{
//		registerVoicedCommandHandler(new Security()); // Original security features - hwid, ip lock
		registerVoicedCommandHandler(new Online()); // .online
		registerVoicedCommandHandler(new PFlag()); // .pflag
		registerVoicedCommandHandler(new CFlag()); // .cflag
		registerVoicedCommandHandler(new DonateVC());
		registerVoicedCommandHandler(new RegStriderRace());
		registerVoicedCommandHandler(new DressMe());
		registerVoicedCommandHandler(new ACP());
		registerVoicedCommandHandler(new SkillUse());
		registerVoicedCommandHandler(new FacebookVoice());
		registerVoicedCommandHandler(new Vote());
		registerVoicedCommandHandler(new SevenRb());
		
		if (Config.ENABLE_HELLBOUND_COMMAND)
		{
			registerVoicedCommandHandler(new Hellbound());
		}
		if (Config.ENABLE_CFG_COMMAND)
		{
			registerVoicedCommandHandler(new Cfg());
		}
		if (Config.ENABLE_CLAN_COMMAND)
		{
			registerVoicedCommandHandler(new CWHPrivileges());
		}
		if (Config.ENABLE_OFFLINE_COMMAND)
		{
			registerVoicedCommandHandler(new Offline());
		}
		if (Config.ENABLE_REPAIR_COMMAND)
		{
			registerVoicedCommandHandler(new Repair());
		}
		if (Config.ENABLE_WEDDING_COMMAND)
		{
			registerVoicedCommandHandler(new Wedding());
		}
		if (Config.ENABLE_DEBUG_COMMAND)
		{
			// registerVoicedCommandHandler(new Debug()); // What the fuck is this command? TODO: check.
		}
		if (Config.ENABLE_RANDOM_COMMANDS)
		{
			registerVoicedCommandHandler(new RandomCommands());
		}
		if (Config.ENABLE_CASTLEINFO_COMMAND)
		{
			registerVoicedCommandHandler(new CastleInfo());
		}
		
		registerVoicedCommandHandler(new Offlinebuff());
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String element : ids)
		{
			_datatable.put(element, handler);
		}
	}
	
	public void removeVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String element : ids)
		{
			_datatable.remove(element);
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		
		return _datatable.get(command);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	@Override
	public void clear()
	{
		_datatable.clear();
	}
}
