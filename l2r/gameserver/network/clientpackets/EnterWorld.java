package l2r.gameserver.network.clientpackets;

import java.util.Calendar;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import L2jGuard.L2JGuard;
//import L2jGuard.network.GuardManager;
import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.dao.MailDAO;
import l2r.gameserver.dao.OfflineBuffersTable;
import l2r.gameserver.data.StringHolder;
import l2r.gameserver.data.xml.holder.ResidenceHolder;
import l2r.gameserver.handler.voicecommands.impl.Vote;
import l2r.gameserver.instancemanager.AutoHuntingManager;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.PlayerMessageStack;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ServerVariables;
import l2r.gameserver.instancemanager.VoteManager;
import l2r.gameserver.instancemanager.games.DonationBonusDay;
import l2r.gameserver.listener.actor.player.OnAnswerListener;
import l2r.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.L2Event;
import l2r.gameserver.model.entity.SevenSigns;
import l2r.gameserver.model.entity.CCPHelpers.CCPSecondaryPassword;
import l2r.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2r.gameserver.model.entity.residence.ClanHall;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.model.pledge.SubUnit;
import l2r.gameserver.model.pledge.UnitMember;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.GameClient;
import l2r.gameserver.network.clientpackets.security.AbstractEnterWorldPacket;
import l2r.gameserver.network.serverpackets.ChangeWaitType;
import l2r.gameserver.network.serverpackets.ClientSetTime;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.network.serverpackets.EtcStatusUpdate;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.ExBR_PremiumState;
import l2r.gameserver.network.serverpackets.ExBasicActionList;
import l2r.gameserver.network.serverpackets.ExGoodsInventoryChangedNotify;
import l2r.gameserver.network.serverpackets.ExMPCCOpen;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.ExNotifyPremiumItem;
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2r.gameserver.network.serverpackets.ExReceiveShowPostFriend;
import l2r.gameserver.network.serverpackets.ExSetCompassZoneCode;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.ExStorageMaxCount;
import l2r.gameserver.network.serverpackets.HennaInfo;
import l2r.gameserver.network.serverpackets.L2FriendList;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySpelled;
import l2r.gameserver.network.serverpackets.PetInfo;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2r.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import l2r.gameserver.network.serverpackets.PledgeSkillList;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import l2r.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2r.gameserver.network.serverpackets.QuestList;
import l2r.gameserver.network.serverpackets.RecipeShopMsg;
import l2r.gameserver.network.serverpackets.RelationChanged;
import l2r.gameserver.network.serverpackets.Ride;
import l2r.gameserver.network.serverpackets.SSQInfo;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.ShortCutInit;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.network.serverpackets.SkillList;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.randoms.CharacterEmails;
import l2r.gameserver.randoms.CharacterIntro;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.tables.AdminTable;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.utils.AccountEmail;
import l2r.gameserver.utils.GameStats;
import l2r.gameserver.utils.TimeUtils;
import l2r.gameserver.utils.TradeHelper;
import l2r.gameserver.utils.Util;

public class EnterWorld extends AbstractEnterWorldPacket // L2GameClientPacket
{
	
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);
	
	@Override
	protected void readImpl()
	{
		// readS(); - client always sends the String "narcasse"
	}
	
//	private static final int MAX_CONNECTED_PER_IP = 11;//Config.DUAL_BOX_IP_CLIENT;
//	
//	private static boolean checkMultipleIPAddress(final Player activeChar)
//	{
//		return MAX_CONNECTED_PER_IP > GameObjectsStorage.getAllPlayers().stream().filter(s -> s.getIP().equalsIgnoreCase(activeChar.getIP())).count();
//	}
	
	@Override
	protected void runImpl()
	{
		long lastAccess = 0;
		final GameClient client = getClient();
		Player activeChar = client.getActiveChar();
		
		// Flood Protection
		if (!checkEnterWorldTime(activeChar))
		{
			return;
		}
		
		//if (activeChar == null)
		if ((activeChar == null) || (Config.AUTH_SERVER_GM_ONLY && !activeChar.isGM())) //|| !checkMultipleIPAddress(activeChar))
		{
			client.closeNow(false);
			return;
		}
		
		GameStats.incrementPlayerEnterGame();
		
		boolean first = activeChar.entering;
		
		if (first)
		{
			activeChar.setOnlineStatus(true);
			
			if (activeChar.isGM())
			{
				if (Config.GM_LOGIN_INVIS && AdminTable.getInstance().hasAccess("admin_invis", activeChar.getAccessLevel()))
				{
					activeChar.setInvisibleType(InvisibleType.NORMAL);
				}
				
				activeChar.setIsInvul(Config.GM_LOGIN_INVUL && AdminTable.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()));
				activeChar.setIsImmortal(Config.GM_LOGIN_IMMORTAL && AdminTable.getInstance().hasAccess("admin_immortal", activeChar.getAccessLevel()));
				activeChar.setMessageRefusal(Config.GM_LOGIN_SILENCE && AdminTable.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()));
				activeChar.setTradeRefusal(Config.GM_LOGIN_TRADEOFF && AdminTable.getInstance().hasAccess("admin_tradeoff", activeChar.getAccessLevel()));
				
				if (Config.HIDE_GM_STATUS)
				{
					AdminTable.getInstance().addGm(activeChar, false);
				}
				else if (AdminTable.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
				{
					AdminTable.getInstance().addGm(activeChar, true);
				}
			}
			
			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.spawnMe();
			activeChar.setPendingOlyEnd(false);

			//activeChar.getInventory().restoreCursedWeapon(); FIXME 
			
			if (L2Event.isParticipant(activeChar))
			{
				L2Event.restorePlayerEventStatus(activeChar);
			}
			
			if (activeChar.isInStoreMode() && !activeChar.isInBuffStore())
			{
				if (!TradeHelper.checksIfCanOpenStore(activeChar, activeChar.getPrivateStoreType()))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
			}
			
			// If its in a buff store, remove it on login
			else if (activeChar.isInBuffStore())
			{
				activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
				activeChar.broadcastCharInfo();
			}
			
			activeChar.setRunning();
			activeChar.standUp();
			activeChar.startTimers();
		}
		else if (activeChar.isTeleporting())
		{
			activeChar.onTeleported();
		}
		
		boolean isPremium = activeChar.hasBonus();
		activeChar.sendPacket(new ExBR_PremiumState(activeChar, isPremium));
		if (!isPremium)
		{
			activeChar.stopBonusTask(false);
		}
		activeChar.getMacroses().sendUpdate();
		activeChar.sendPacket(new SSQInfo(), new HennaInfo(activeChar));
		activeChar.sendItemList(false);
		activeChar.sendPacket(new ShortCutInit(activeChar), new SkillList(activeChar), new SkillCoolTime(activeChar));
		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		
		// Config New char is Hero
		if (Config.NEW_CHAR_IS_HERO)
		{
			activeChar.setHero(true);
		}
		
		// Config New char is NOBLE
		if (Config.NEW_CHAR_IS_NOBLE)
		{
			activeChar.setNoble(true, true);
		}
		
		Announcements.getInstance().showAnnouncements(activeChar);
		
		if (first)
		{
			activeChar.getListeners().onEnter();
		}
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		
		if (first && (activeChar.getCreateTime() > 0))
		{
			Calendar create = Calendar.getInstance();
			create.setTimeInMillis(activeChar.getCreateTime());
			Calendar now = Calendar.getInstance();
			
			int day = create.get(Calendar.DAY_OF_MONTH);
			if ((create.get(Calendar.MONTH) == Calendar.FEBRUARY) && (day == 29))
			{
				day = 28;
			}
			
			int myBirthdayReceiveYear = activeChar.getVarInt(Player.MY_BIRTHDAY_RECEIVE_YEAR, 0);
			if ((create.get(Calendar.MONTH) == now.get(Calendar.MONTH)) && (create.get(Calendar.DAY_OF_MONTH) == day))
			{
				if (((myBirthdayReceiveYear == 0) && (create.get(Calendar.YEAR) != now.get(Calendar.YEAR))) || ((myBirthdayReceiveYear > 0) && (myBirthdayReceiveYear != now.get(Calendar.YEAR))))
				{
					Mail mail = new Mail();
					mail.setSenderId(1);
					mail.setSenderName(StringHolder.getInstance().getNotNull(activeChar, "l2r.gameserver.network.clientpackets.EnterWorld.BirthdayNpc"));
					mail.setReceiverId(activeChar.getObjectId());
					mail.setReceiverName(activeChar.getName());
					mail.setTopic(StringHolder.getInstance().getNotNull(activeChar, "l2r.gameserver.network.clientpackets.EnterWorld.BirthdayTitle"));
					mail.setBody(StringHolder.getInstance().getNotNull(activeChar, "l2r.gameserver.network.clientpackets.EnterWorld.BirthdayText"));
					
					ItemInstance item = new ItemInstance(21169);
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(1L);
					item.save();
					
					mail.addAttachment(item);
					mail.setUnread(true);
					mail.setType(Mail.SenderType.BIRTHDAY);
					mail.setExpireTime((720 * 3600) + (int) (System.currentTimeMillis() / 1000L));
					mail.save();
					
					activeChar.setVar(Player.MY_BIRTHDAY_RECEIVE_YEAR, String.valueOf(now.get(Calendar.YEAR)), -1);
				}
			}
		}
		
		// // Pop-up identification window when player login IF he has password set
		// if (Config.SECURITY_ENABLED && Config.SECURITY_ON_STARTUP_WHEN_SECURED && activeChar.getSecurity())
		// {
		// IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("security");
		//
		// if (vch != null)
		// {
		// vch.useVoicedCommand("security", activeChar, null);
		// }
		// }
		// else // Show the clan notice to player.
		// {
		// activeChar.showClanNotice();
		// activeChar.sendMessage("Your character is not protected against item theft. To protect it, use .security");
		// }
		
		/*
		 * New code protection Max ehcnat items.
		 */
		if (Config.MAX_ITEM_ENCHANT_KICK > 0)
		{
			for (ItemInstance i : activeChar.getInventory().getItems())
			{
				if (!activeChar.isGM())
				{
					if (i.isEquipable())
					{
						if (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
						{
							activeChar.getInventory().destroyItem(i, 1, null);
							activeChar.sendMessage("You have over enchanted items you will be kicked from server!");
							activeChar.sendMessage("Respect our server rules.");
							sendPacket(new ExShowScreenMessage(" You have an over enchanted item, you will be kicked from server! ", 6000));
							Util.handleIllegalPlayerAction(activeChar, "EnterWordl [Overenchanted]", "Player \" + activeChar.getName() + \" has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);
							// Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has Overenchanted item! Kicked! ", null, Config.DEFAULT_PUNISH);
							_log.warn("#### ATTENTION ####");
							_log.warn(i + " item has been removed from " + activeChar);
						}
					}
				}
			}
		}
		
		/*
		 * lets disable it... // Backup to set default name color every time on login. if (activeChar.getNameColor() != 0xFFFFFF && (activeChar.getKarma() == 0 || activeChar.getRecomHave() == 0) && !activeChar.isGM()) activeChar.setNameColor(0xFFFFFF);
		 */
		
		if ((activeChar.getTitleColor() != Player.DEFAULT_TITLE_COLOR) && !activeChar.isGM())
		{
			activeChar.setTitleColor(Player.DEFAULT_TITLE_COLOR);
		}
		
		if (activeChar.getClan() != null)
		{
			notifyClanMembers(activeChar);
			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeShowInfoUpdate(activeChar.getClan()), new PledgeSkillList(activeChar.getClan()));
		}
		
		// engage and notify Partner
		if (first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}
		
		if (Config.ENABLE_AUTO_HUNTING_REPORT)
		{
			AutoHuntingManager.getInstance().onEnter(activeChar);
		}
		
		if (first)
		{
			activeChar.getFriendList().notifyFriends(true);
			loadTutorial(activeChar);
			activeChar.restoreDisableSkills();
			
			if (activeChar.getVar("Para") != null)
			{
				if (!activeChar.isBlocked())
				{
					activeChar.block();
				}
				activeChar.startAbnormalEffect(AbnormalEffect.HOLD_1);
				activeChar.abortAttack(true, false);
				activeChar.abortCast(true, false);
				activeChar.sendPacket(new Say2(activeChar.getObjectId(), ChatType.TELL, "Paralyze", "You are paralyzed for " + (activeChar.getVarTimeToExpire("Para") / 60000L) + " more minutes!"));
			}
			
			if (Config.ALLOW_MAIL_OPTION)
			{
				AccountEmail.checkEmail(activeChar);
			}
		}
		
		sendPacket(new L2FriendList(activeChar), new ExStorageMaxCount(activeChar), new QuestList(activeChar), new ExBasicActionList(activeChar), new EtcStatusUpdate(activeChar));
		
		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		}
		
		if (!first)
		{
			if (activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				Skill castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if ((castingSkill != null) && (castingTarget != null) && castingTarget.isCreature() && (activeChar.getAnimationEndTime() > 0))
				{
					sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0));
				}
			}
			
			if (activeChar.isInBoat())
			{
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));
			}
			
			if (activeChar.isMoving || activeChar.isFollow)
			{
				sendPacket(activeChar.movePacket());
			}
			
			if (activeChar.getMountNpcId() != 0)
			{
				sendPacket(new Ride(activeChar));
			}
			
			if (activeChar.isFishing())
			{
				activeChar.stopFishing();
			}
		}
		
		activeChar.entering = false;
		activeChar.sendUserInfo(true);
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new ChangeWaitType(activeChar, ChangeWaitType.WT_SITTING));
		}
		if (activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			if (activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreMsgBuy(activeChar));
			}
			else if ((activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_SELL) || (activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE))
			{
				sendPacket(new PrivateStoreMsgSell(activeChar));
			}
			else if (activeChar.getPrivateStoreType() == Player.STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopMsg(activeChar));
			}
		}
		
		if (activeChar.isDead())
		{
			sendPacket(new Die(activeChar));
		}
		
		activeChar.unsetVar("offline");
		// activeChar.unsetVar("offlinebuffer");
		// activeChar.unsetVar("offlinebuffrestoretitle");
		// activeChar.unsetVar("offlinebufferprice");
		// activeChar.unsetVar("offlinebuffertitle");
		
		activeChar.sendActionFailed();
		
		if (first && activeChar.isGM() && Config.SAVE_GM_EFFECTS)
		{
			// silence
			if (activeChar.getVarB("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
			}
			// invul
			if (activeChar.getVarB("gm_invul"))
			{
				activeChar.setIsInvul(true);
				activeChar.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
				activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.clientpackets.EnterWorld.message1", activeChar).addString(activeChar.getName()));
			}
			// gmspeed
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if ((var_gmspeed >= 1) && (var_gmspeed <= 4))
				{
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, var_gmspeed), activeChar, true);
				}
			}
			catch (Exception E)
			{
			}
		}
		
		if (first && activeChar.isInJail())
		{
			long period = activeChar.getVarTimeToExpire("jailed");
			if (period == -1)
			{
				activeChar.sendPacket(new Say2(0, ChatType.TELL, "Administration", " You are jailed forever !"));
			}
			else
			{
				period /= 1000; // to seconds
				period /= 60; // to minutes
				
				activeChar.sendPacket(new Say2(0, ChatType.TELL, "Administration", "Sit left " + TimeUtils.minutesToFullString((int) period)));
			}
		}
		
		PlayerMessageStack.getInstance().CheckMessages(activeChar);
		
		sendPacket(ClientSetTime.STATIC, new ExSetCompassZoneCode(activeChar));
		
		Pair<Integer, OnAnswerListener> entry = activeChar.getAskListener(false);
		if ((entry != null) && (entry.getValue() instanceof ReviveAnswerListener))
		{
			sendPacket(new ConfirmDlg(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));
		}
		
		// Safe check to remove event items on login.
		if (activeChar.getVar("ineEvent") != null)
		{
			activeChar.teleToClosestTown();
			activeChar.unsetVar("ineEvent");
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().showUsageTime(activeChar, activeChar.getCursedWeaponEquippedId());
		}
		
		if (first)
		{
			if (Config.BUFF_STORE_ENABLED)
			{
				OfflineBuffersTable.getInstance().onLogin(activeChar);
			}
		}
		
		//if (!first)
		
		if(first)
		{
			activeChar.sendUserInfo(); //Display right in clan
		}
		else
		{
			if (activeChar.isInObserverMode())
			{
				if (activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
				{
					activeChar.returnFromObserverMode();
				}
				else if (activeChar.getOlympiadObserveGame() != null)
				{
					activeChar.leaveOlympiadObserverMode(true);
				}
				else
				{
					activeChar.leaveObserverMode();
				}
			}
			else if (activeChar.isVisible())
			{
				World.showObjectsToPlayer(activeChar);
			}
			
			if (activeChar.getPet() != null)
			{
				sendPacket(new PetInfo(activeChar.getPet()));
			}
			
			if (activeChar.isInParty())
			{
				Summon member_pet;
				// sends new member party window for all members
				// we do all actions before adding member to a list, this speeds things up a little
				sendPacket(new PartySmallWindowAll(activeChar.getParty(), activeChar));
				
				for (Player member : activeChar.getParty())
				{
					if (member != activeChar)
					{
						sendPacket(new PartySpelled(member, true));
						if ((member_pet = member.getPet()) != null)
						{
							sendPacket(new PartySpelled(member_pet, true));
						}
						
						sendPacket(RelationChanged.update(activeChar, member, activeChar));
					}
				}
				
				if (activeChar.getParty().isInCommandChannel())
				{
					sendPacket(ExMPCCOpen.STATIC);
				}
			}
			
			for (int shotId : activeChar.getAutoSoulShot())
			{
				sendPacket(new ExAutoSoulShot(shotId, true));
			}
			
			for (Effect e : activeChar.getEffectList().getAllFirstEffects())
			{
				if (e.getSkill().isToggle())
				{
					sendPacket(new MagicSkillLaunched(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar));
				}
			}
			
			activeChar.broadcastCharInfo();
		}
		
		activeChar.updateEffectIcons();
		activeChar.updateStats();
		
		if (Config.ALT_PCBANG_POINTS_ENABLED)
		{
			activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
		}
		
		if (!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(Config.GOODS_INVENTORY_ENABLED ? ExGoodsInventoryChangedNotify.STATIC : ExNotifyPremiumItem.STATIC);
		}
		
		if (activeChar.getVarB("HeroPeriod") && Config.SERVICES_HERO_SELL_ENABLED)
		{
			activeChar.setHero(activeChar);
		}
		
		// Backup case, if hero weap and hero status was not removed. This is only if some one buy hero status.
		if (activeChar.getVarB("HeroPeriod") && (activeChar.getVarLong("HeroPeriod") <= System.currentTimeMillis()))
		{
			activeChar.setHero(false);
			ItemInstance[] arr = activeChar.getInventory().getItems();
			int len = arr.length;
			for (int i = 0; i < len; i++)
			{
				ItemInstance _item = arr[i];
				if (_item.isHeroWeapon())
				{
					activeChar.getInventory().destroyItem(_item, 1, "");
				}
			}
			activeChar.updatePledgeClass();
			activeChar.broadcastUserInfo(true);
			Hero.deleteHero(activeChar.getObjectId());
			Hero.removeSkills(activeChar);
			activeChar.unsetVar("HeroPeriod");
		}
		
		// mysql.set("UPDATE characters SET lastip=? WHERE obj_Id=?", activeChar.getIP(), activeChar.getHWID(), activeChar.getObjectId());
		
		// activeChar.delOlympiadIpHWID(); // Clean Oly ip, hwid for player if crash inside olympiad.
		activeChar.sendVoteSystemInfo();
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
		activeChar.getNevitSystem().onEnterWorld();

		checkNewMail(activeChar);
		
		
		if (Config.ENABLE_POLL_SYSTEM)
		{
			if (VoteManager.getInstance().pollisActive() && VoteManager.getInstance().canVote(activeChar.getHWID()))
			{
				ExShowScreenMessage sm = new ExShowScreenMessage("There is active poll, type .poll to participate", 7000, ScreenMessageAlign.TOP_LEFT, true);
				activeChar.sendPacket(sm);
				
				activeChar.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "PollSystem", "There is active poll started by the server Administrator.");
				activeChar.sendChatMessage(0, ChatType.CRITICAL_ANNOUNCE.ordinal(), "PollSystem", "To participate please type .poll");
			}
			
		}
		
		// Synerge - Show the premium htm and message
		if (Config.ENTER_WORLD_SHOW_HTML_PREMIUM_BUY)
		{
			
			 if ((activeChar.getClan() == null) && (activeChar.getClient().getBonus() < 1))
			 {
			 activeChar.sendPacket(new NpcHtmlMessage(5).setFile("advertise.htm").replace("%playername%", activeChar.getName()));
			 }
			if ((activeChar.getClient() != null) && (activeChar.getClient().getBonus() < 1))
			{
				String msg = "You don't have Premium Account, you can buy it from Community Board.";
				activeChar.sendPacket(new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, false));
				activeChar.sendMessage(msg);
			}
		}
		
		
		long _lastNotVote;
		final long currentDate = System.currentTimeMillis();
		if (Config.POP_UP_VOTE_MENU && (activeChar.getClient() != null))
		{
			_lastNotVote = currentDate;
			announceNotVote();
		}
		
		if (Config.POP_UP_VOTE_MENU && (activeChar.getClient() != null))
		{
			try
			{
				// String playerIP = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
				String playerIP = activeChar.getIP();
				Vote.restoreVotedData(activeChar, playerIP);
			}
			catch (Exception e)
			{
				
				_log.warn("EnterWorld: Could not restore vote data for player: " + activeChar.getName() + " IP: " + activeChar.getIP() + "." + e);
			}
			
			if (activeChar.eligibleToVoteHop() || activeChar.eligibleToVoteTop() || activeChar.eligibleToVoteNet() || activeChar.eligibleToVoteBra())
			{
				Vote.showHtm(activeChar);
			}
		}
		
		// If the player skip some part and relog, on login to start from the beggining...
		// It will replace his old values...
		// Email validtion will be last to avoid some exploit.
		if (Config.ENABLE_CHARACTER_INTRO)
		{
			CharacterIntro.checkAndSendIntro(activeChar);
		}
		
		if (Config.ENABLE_EMAIL_VALIDATION)
		{
			CharacterEmails.verifyEmail(activeChar);
		}
		
		if (Config.AUTO_SHOTS_ON_LOGIN)
		{
			verifyAndLoadShots(activeChar);
		}
		
		if (ServerVariables.getBool("DonationBonusActive", true))
		{
			activeChar.sendChatMessage(0, ChatType.TRADE.ordinal(), "BonusDay", "All donations made next: " + DonationBonusDay.getInstance().getTimeLeft() + " will recive " + ServerVariables.getInt("DonationBonusPercent") + "% extra coins!");
		}
		
		String lastAccessDate = TimeUtils.convertDateToString(lastAccess * 1000);
		
		String ip = activeChar.getVar("LastIP");
		if ((ip != null) && !ip.isEmpty() && (activeChar.getIP() != null))
		{
			if (!activeChar.getIP().equalsIgnoreCase(ip))
			{
				activeChar.sendPacket(new Say2(activeChar.getObjectId(), ChatType.CRITICAL_ANNOUNCE, "SYS", "You are logging in from another IP. Last access: " + lastAccessDate));
				
				if (Config.ALLOW_MAIL_OPTION)
				{
					AccountEmail.verifyEmail(activeChar, null); // Send an e-mail verification html to this character so he can play only when he verifies his e-mail.
				}
				else
				{
					activeChar.setVar("LastIP", activeChar.getIP()); // Handled in verifyEmail if the above is ran. It is used to not abuse character relog to escape the verifyEmail.
				}
			}
		}
		else
		{
			// IP is null or empty, must populate the var for the next time.
			activeChar.setVar("LastIP", activeChar.getIP());
		}
		
		String hwid = activeChar.getVar("LastHWID");
		if ((hwid != null) && !hwid.isEmpty() && (activeChar.getHWID() != null))
		{
			if (!activeChar.getHWID().equalsIgnoreCase(hwid))
			{
				activeChar.sendChatMessage(activeChar.getObjectId(), ChatType.TELL.ordinal(), "System", (activeChar.isLangRus() ? "WARNING: This character was last logged from another computer on  " + lastAccessDate : "WARNING: This character was last logged from another computer on " + lastAccessDate));
				activeChar.setVar("LastHWID", activeChar.getHWID());
			}
		}
		else
		{
			// HWID is null or empty, must populate the var for the next time.
			if (activeChar.getHWID() != null)
			{
				activeChar.setVar("LastHWID", activeChar.getHWID());
			}
		}
	}
	
	private static void notifyClanMembers(Player activeChar)
	{
		Clan clan = activeChar.getClan();
		SubUnit subUnit = activeChar.getSubUnit();
		if ((clan == null) || (subUnit == null))
		{
			return;
		}
		
		UnitMember member = subUnit.getUnitMember(activeChar.getObjectId());
		if (member == null)
		{
			return;
		}
		
		member.setPlayerInstance(activeChar, false);
		
		int sponsor = activeChar.getSponsor();
		int apprentice = activeChar.getApprentice();
		L2GameServerPacket msg = new SystemMessage2(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(activeChar);
		PledgeShowMemberListUpdate memberUpdate = new PledgeShowMemberListUpdate(activeChar);
		for (Player clanMember : clan.getOnlineMembers(activeChar.getObjectId()))
		{
			clanMember.sendPacket(memberUpdate);
			if (clanMember.getObjectId() == sponsor)
			{
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(activeChar));
			}
			else if (clanMember.getObjectId() == apprentice)
			{
				clanMember.sendPacket(new SystemMessage2(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(activeChar));
			}
			else
			{
				clanMember.sendPacket(msg);
			}
		}
		
		if (!activeChar.isClanLeader())
		{
			return;
		}
		
		ClanHall clanHall = clan.getHasHideout() > 0 ? ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout()) : null;
		if ((clanHall == null) || (clanHall.getAuctionLength() != 0))
		{
			return;
		}
		
		if (clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
		{
			return;
		}
		
		if (clan.getWarehouse().getCountOf(ItemTemplate.ITEM_ID_ADENA) < clanHall.getRentalFee())
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addLong(clanHall.getRentalFee()));
		}
	}
	
	public static void loadTutorial(Player player)
	{
		Quest q = QuestManager.getQuest(255);
		if (q != null)
		{
			if (CCPSecondaryPassword.hasPassword(player))
			{
				player.processQuestEvent(q.getName(), "CheckPass", null, false);
			}
			else
			{
				player.processQuestEvent(q.getName(), "ProposePass", null, false);
			}
			/*
			 * else if (player.getLevel() == 1 || Rnd.get(10) == 1) { player.processQuestEvent(q.getName(), "ProposePass", null, false); } else { player.processQuestEvent(q.getName(), "UC", null, false); }
			 */
			player.processQuestEvent(q.getName(), "OpenClassMaster", null, false);
			player.processQuestEvent(q.getName(), "ShowChangeLog", null, false);
		}
	}
	
	private void checkNewMail(Player activeChar)
	{
		for (Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
		{
			if (mail.isUnread())
			{
				sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
		}
	}
	
	private static final String NOT_CONNECTED_MSG_ADDRESS = "Votesystem";
	
	private static void announceNotVote()
	{
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if (player.getFacebookProfile() == null)
			{
				player.sendPacket(new Say2(0, ChatType.HERO_VOICE, "VoteSystem", StringHolder.getNotNull(player, NOT_CONNECTED_MSG_ADDRESS, player.getName())));
			}
		}
	}
	
	/**
	 * This method will get the correct soulshot/spirishot and activate it for the current weapon if it's over the minimum.
	 * @param activeChar
	 * @author Zoey76
	 */
	public static void verifyAndLoadShots(Player activeChar)
	{
		int soulId = -1;
		int spiritId = -1;
		int bspiritId = -1;

		if (!activeChar.isDead() && activeChar.getActiveWeaponItem() != null)
		{
			switch (activeChar.getActiveWeaponItem().getCrystalType())
			{
				case NONE:
					soulId = 1835;
					spiritId = 2509;
					bspiritId = 3947;
					break;
				case D:
					soulId = 1463;
					spiritId = 2510;
					bspiritId = 3948;
					break;
				case C:
					soulId = 1464;
					spiritId = 2511;
					bspiritId = 3949;
					break;
				case B:
					soulId = 1465;
					spiritId = 2512;
					bspiritId = 3950;
					break;
				case A:
					soulId = 1466;
					spiritId = 2513;
					bspiritId = 3951;
					break;
				case S:
				case S80:
				case S84:
					soulId = 1467;
					spiritId = 2514;
					bspiritId = 3952;
					break;
			}

			//Soulshots.
			if ((soulId > -1) && activeChar.getInventory().getCountOf(soulId) > 100)
			{
				activeChar.addAutoSoulShot(soulId);
				activeChar.sendPacket(new ExAutoSoulShot(soulId, true));
				//Message
				L2GameServerPacket msg = new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addItemName(soulId);
				activeChar.sendPacket(msg);
			}

			//Blessed Spirishots first, then Spirishots.
			if ((bspiritId > -1) && activeChar.getInventory().getCountOf(bspiritId) > 100)
			{
				activeChar.addAutoSoulShot(bspiritId);
				activeChar.sendPacket(new ExAutoSoulShot(bspiritId, true));
				//Message
				L2GameServerPacket msg = new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addItemName(bspiritId);
				activeChar.sendPacket(msg);
			}
			else if ((spiritId > -1) && activeChar.getInventory().getCountOf(spiritId) > 100)
			{
				activeChar.addAutoSoulShot(spiritId);
				activeChar.sendPacket(new ExAutoSoulShot(spiritId, true));
				//Message
				L2GameServerPacket msg = new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addItemName(spiritId);
				activeChar.sendPacket(msg);
			}

			activeChar.autoShot();
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}