package l2r.gameserver.handler.voicecommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2r.commons.dbutils.DbUtils;
import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

public class Vote implements IVoicedCommandHandler
{
	protected static Logger LOG = LoggerFactory.getLogger(Vote.class);
	
	private static final String CREATE_VOTEDATA = "INSERT INTO accounts_vote (vote_ip, last_hop_vote, last_top_vote,last_net_vote) values(?,?,?,?)";
	private static final String RESTORE_VOTEDATA = "SELECT last_hop_vote, last_top_vote,last_net_vote FROM accounts_vote WHERE vote_ip=?";
	
	private static String[] _voicedCommands =
	{
		"vote"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final Player player, final String target)
	{
		if (player == null)
		{
			return false;
		}
		
		if (command.equalsIgnoreCase("vote"))
		{
			showHtm(player);
		}
		return true;
	}
	
	public static void showHtm(Player player)
	{
		
		restoreVotedData(player, player.getIP());
		
		final NpcHtmlMessage htm = new NpcHtmlMessage(5);
		
		htm.setFile("mods/vote.htm");
		
		htm.replace("%name%", player.getName());
		htm.replace("%hopTime%", player.getVoteCountdownHop());
		
		if (player.eligibleToVoteHop())
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Hopzone", "Vote Time  0"));
		}
		else
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Hopzone", String.format("Vote Time " + " " +  player.getVoteCountdownHop())));
		}
		
		if (player.eligibleToVoteTop())
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Topzone", "Vote Time  0"));
		}
		else
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Topzone", String.format("Vote Time " + " " +  player.getVoteCountdownTop())));
		}
		
		if (player.eligibleToVoteNet())
		{

			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Netword", "Vote Time  0"));
		}
		else
		{
			player.sendPacket(new Say2(0, ChatType.COMMANDCHANNEL_ALL, "Netword", String.format("Vote Time " + " " + player.getVoteCountdownNet())));
		}
		player.sendPacket(htm);
		return;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
	private static void createVotedDB(Player player)
	{
		String ip = player.getIP();
		Connection con = null;
		PreparedStatement statement = null;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CREATE_VOTEDATA);
			statement.setString(1, ip);
			statement.setLong(2, 0);
			statement.setLong(3, 0);
			statement.setLong(4, 0);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			// if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.warn("VoteCommand: Could not insert ip data: " + e);
			
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public static void restoreVotedData(Player player, String ip)
	{
		boolean sucess = false;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_VOTEDATA);
			statement.setString(1, ip);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				sucess = true;
				
				player.setLastHopVote(rset.getLong("last_hop_vote"));
				player.setLastTopVote(rset.getLong("last_top_vote"));
				player.setLastBraVote(rset.getLong("last_net_vote"));
				
				if (rset.getLong("last_hop_vote") <= System.currentTimeMillis())
				{
					rset.getLong("last_hop_vote");
				}
				else
				{
					player.setLastHopVote(rset.getLong("last_hop_vote"));
				}
				
				if (rset.getLong("last_top_vote") <= System.currentTimeMillis())
				{
					rset.getLong("last_top_vote");
				}
				else
				{
					player.setLastTopVote(rset.getLong("last_top_vote"));
				}
				
				if (rset.getLong("last_net_vote") <= System.currentTimeMillis())
				{
					rset.getLong("last_net_vote");
				}
				else
				{
					player.setLastNetVote(rset.getLong("last_net_vote"));
				}
				
			}
			
		}
		catch (Exception e)
		{
			// if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOG.warn("VoteCommand: Could not restore voted data for:" + ip + "." + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		
		if (sucess == false)
		{
			createVotedDB(player);
		}
	}
	
	// TODO giveReward
	public static void giveReward(Player player)
	{
		player.sendMessage("Thank you! You've got reward for the vote.");
		player.sendPacket(new ExShowScreenMessage("Thank you! You've got reward for the vote.", 4000));
		PlaySound playSound = new PlaySound("ItemSound.quest_fanfare_1");
		player.sendPacket(playSound);
		player.broadcastCharInfo();
		
		int itemcount = Rnd.get(Config.VOTE_REWARD_ITEM_COUNT_MIN, Config.VOTE_REWARD_ITEM_COUNT_MAX);
		ItemInstance newitem = player.getInventory().addItem(Config.VOTE_REWARD_ITEM_ID, itemcount, "");
		// ItemInstance newitem = player.getInventory().addItem("VoteItem", Config.VOTE_REWARD_ITEM_ID, itemcount, player, null,"");
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addNewItem(newitem);
		// playerIU.addItem(newitem);
		player.sendPacket(playerIU);
		
		if (itemcount > 1)
		{
			SystemMessage sm = new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S2_S1S);
			sm.addItemName(Config.VOTE_REWARD_ITEM_ID);
			sm.addNumber(itemcount);
			player.sendPacket(sm);
			
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMsg.YOU_HAVE_EARNED_S1);
			sm.addItemName(Config.VOTE_REWARD_ITEM_ID);
			player.sendPacket(sm);
			
		}
		showHtm(player);
		return;
	}
}