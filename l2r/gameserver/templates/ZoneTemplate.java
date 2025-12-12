package l2r.gameserver.templates;

import l2r.commons.collections.MultiValueSet;
import l2r.commons.configuration.ExProperties;
import l2r.gameserver.Config;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.Territory;
import l2r.gameserver.model.Zone.ZoneTarget;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.model.base.Race;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZoneTemplate {
	private final String _name;
	private final ZoneType _type;

	private final Territory _territory;

	private final boolean _isEnabled;

	private final List<Location> _restartPoints;
	private final List<Location> _PKrestartPoints;
	private final long _restartTime;

	private final int _enteringMessageId;
	private final int _leavingMessageId;

	private final Race _affectRace;
	private final ZoneTarget _target;

	private final Skill _skill;
	private final int _skillProb;
	private final int _initialDelay;
	private final int _unitTick;
	private final int _randomTick;

	private final int _damageMessageId;

	private final int _damageOnHP;

	private final int _damageOnMP;

	private final double _moveBonus;

	private final double _regenBonusHP;

	private final double _regenBonusMP;

	private final int _eventId;

	private final String[] _blockedActions;

	private final int _index;
	private final int _taxById;

	private final StatsSet _params;

	private final boolean _isEpicPvP;

	private final int _dualBox;


	@SuppressWarnings("unchecked")
	public ZoneTemplate(StatsSet set) {
		_name = set.getString("name");
		_type = ZoneType.valueOf(set.getString("type"));
		_territory = (Territory) set.get("territory");

		_enteringMessageId = set.getInteger("entering_message_no", 0);
		_leavingMessageId = set.getInteger("leaving_message_no", 0);

		_target = ZoneTarget.valueOf(set.getString("target", "pc"));
		_affectRace = set.getString("affect_race", "all").equals("all") ? null : Race.valueOf(set.getString("affect_race"));

		//Π—ΠΎΠ½Π° Ρ� Ρ�Ρ„Ρ„ΠµΠΊΡ‚ΠΎΠΌ
		String s = set.getString("skill_name", null);
		Skill skill = null;
		if (s != null) {
			String[] sk = s.split("[\\s,;]+");
			skill = SkillTable.getInstance().getInfo(Integer.parseInt(sk[0]), Integer.parseInt(sk[1]));
		}
		_skill = skill;
		_skillProb = set.getInteger("skill_prob", 100);
		_initialDelay = set.getInteger("initial_delay", 1);
		_unitTick = set.getInteger("unit_tick", 1);
		_randomTick = set.getInteger("random_time", 0);

		//Π—ΠΎΠ½Π° Ρ� Π±ΠΎΠ½ΡƒΡ�Π°ΠΌΠΈ
		_moveBonus = set.getDouble("move_bonus", 0.);
		_regenBonusHP = set.getDouble("hp_regen_bonus", 0.);
		_regenBonusMP = set.getDouble("mp_regen_bonus", 0.);

		//Π—ΠΎΠ½Π° Ρ� Π΄Π°ΠΌΠ°Π³ΠΎΠΌ
		_damageOnHP = set.getInteger("damage_on_hp", 0);
		_damageOnMP = set.getInteger("damage_on_mp", 0);
		_damageMessageId = set.getInteger("message_no", 0);

		_eventId = set.getInteger("eventId", 0);

		_isEnabled = set.getBool("enabled", true);

		_restartPoints = (List<Location>) set.get("restart_points");
		_PKrestartPoints = (List<Location>) set.get("PKrestart_points");
		_restartTime = set.getLong("restart_time", 0L);

		s = (String) set.get("blocked_actions");
		if (s != null)
			_blockedActions = s.split(ExProperties.defaultDelimiter);
		else
			_blockedActions = null;

		_isEpicPvP = set.getBool("epicPvP", false);
		_dualBox = set.getInteger("DualBox", 0);

		_index = set.getInteger("index", 0);
		_taxById = set.getInteger("taxById", 0);

		_params = set;
	}

	public boolean isEnabled() {
		return _isEnabled;
	}

	public String getName() {
		return _name;
	}

	public ZoneType getType() {
		return _type;
	}

	public Territory getTerritory() {
		return _territory;
	}

	public int getEnteringMessageId() {
		return _enteringMessageId;
	}

	public int getLeavingMessageId() {
		return _leavingMessageId;
	}

	public Skill getZoneSkill() {
		return _skill;
	}

	public int getSkillProb() {
		return _skillProb;
	}

	public int getInitialDelay() {
		return _initialDelay;
	}

	public int getUnitTick() {
		return _unitTick;
	}

	public int getRandomTick() {
		return _randomTick;
	}

	public ZoneTarget getZoneTarget() {
		return _target;
	}

	public Race getAffectRace() {
		return _affectRace;
	}

	public String[] getBlockedActions() {
		return _blockedActions;
	}

	/**
	 * Π�ΠΎΠΌΠµΡ€ Ρ�ΠΈΡ�Ρ‚ΠµΠΌΠ½ΠΎΠ³ΠΎ Π²ΠΎΠΎΠ±Ρ‰ΠµΠ½ΠΈΡ� ΠΊΠΎΡ‚ΠΎΡ€ΠΎΠµ Π±ΡƒΠ΄ΠµΡ‚ ΠΎΡ‚ΠΎΡ�Π»Π°Π½ΠΎ ΠΈΠ³Ρ€ΠΎΠΊΡƒ ΠΏΡ€ΠΈ Π½Π°Π½ΠµΡ�ΠµΠ½ΠΈΠΈ ΡƒΡ€ΠΎΠ½Π° Π·ΠΎΠ½ΠΎΠΉ
	 *
	 * @return SystemMessage ID
	 */
	public int getDamageMessageId() {
		return _damageMessageId;
	}

	/**
	 * Π΅ΠΊΠΎΠ»Ρ�ΠΊΠΎ ΡƒΡ€ΠΎΠ½Π° Π·ΠΎΠ½Π° Π½Π°Π½ΠµΡ�ΠµΡ‚ ΠΏΠΎ Ρ…ΠΏ
	 *
	 * @return ΠΊΠΎΠ»ΠΈΡ‡ΠµΡ�Ρ‚Π²ΠΎ ΡƒΡ€ΠΎΠ½Π°
	 */
	public int getDamageOnHP() {
		return _damageOnHP;
	}

	/**
	 * Π΅ΠΊΠΎΠ»Ρ�ΠΊΠΎ ΡƒΡ€ΠΎΠ½Π° Π·ΠΎΠ½Π° Π½Π°Π½ΠµΡ�ΠµΡ‚ ΠΏΠΎ ΠΌΠΏ
	 *
	 * @return ΠΊΠΎΠ»ΠΈΡ‡ΠµΡ�Ρ‚Π²ΠΎ ΡƒΡ€ΠΎΠ½Π°
	 */
	public int getDamageOnMP() {
		return _damageOnMP;
	}

	/**
	 * @return Π‘ΠΎΠ½ΡƒΡ� ΠΊ Ρ�ΠΊΠΎΡ€ΠΎΡ�Ρ‚ΠΈ Π΄Π²ΠΈΠ¶ΠµΠ½ΠΈΡ� Π² Π·ΠΎΠ½Πµ
	 */
	public double getMoveBonus() {
		return _moveBonus;
	}

	/**
	 * Π’ΠΎΠ·Π²Ρ€Π°Ρ‰Π°ΠµΡ‚ Π±ΠΎΠ½ΡƒΡ� Ρ€ΠµΠ³ΠµΠ½ΠµΡ€Π°Ρ†ΠΈΠΈ Ρ…ΠΏ Π² Ρ�Ρ‚ΠΎΠΉ Π·ΠΎΠ½Πµ
	 *
	 * @return Π‘ΠΎΠ½ΡƒΡ� Ρ€ΠµΠ³ΠµΠ½Π°Ρ€Π°Ρ†ΠΈΠΈ Ρ…ΠΏ Π² Ρ�Ρ‚ΠΎΠΉ Π·ΠΎΠ½Πµ
	 */
	public double getRegenBonusHP() {
		return _regenBonusHP;
	}

	/**
	 * Π’ΠΎΠ·Π²Ρ€Π°Ρ‰Π°ΠµΡ‚ Π±ΠΎΠ½ΡƒΡ� Ρ€ΠµΠ³ΠµΠ½ΠµΡ€Π°Ρ†ΠΈΠΈ ΠΌΠΏ Π² Ρ�Ρ‚ΠΎΠΉ Π·ΠΎΠ½Πµ
	 *
	 * @return Π‘ΠΎΠ½ΡƒΡ� Ρ€ΠµΠ³ΠµΠ½Π°Ρ€Π°Ρ†ΠΈΠΈ ΠΌΠΏ Π² Ρ�Ρ‚ΠΎΠΉ Π·ΠΎΠ½Πµ
	 */
	public double getRegenBonusMP() {
		return _regenBonusMP;
	}

	public long getRestartTime() {
		return _restartTime;
	}

	public List<Location> getRestartPoints() {
		return _restartPoints;
	}

	public List<Location> getPKRestartPoints() {
		return _PKrestartPoints;
	}

	public int getIndex() {
		return _index;
	}

	public int getTaxById() {
		return _taxById;
	}

	public int getEventId() {
		return _eventId;
	}

	public int getDualBox()
	{
		return _dualBox;
	}

	public boolean isEpicPvP() {
		return _isEpicPvP;
	}

	public MultiValueSet<String> getParams() {
		return _params.clone();
	}

	public int _active_boxes = -1;

	public List<String> active_boxes_characters = new ArrayList<>();


	public boolean checkMultiBox(Player pl) {

		boolean output = true;

		int boxes_number = 0; // this one
		final List<String> active_boxes = new ArrayList<>();

		if (pl.getClient() != null && pl.getClient().getConnection() != null && !pl.getClient().getConnection().isClosed() && pl.getClient().getConnection().getSocket().getInetAddress() != null) {

			final String thisip = pl.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
			final Collection<Player> allPlayers = GameObjectsStorage.getAllPlayers();
			for (final Player player : allPlayers) {
				if (player != null) {
					if (player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && player.getClient().getConnection().getSocket().getInetAddress() != null && !player.getName().equals(this.getName())) {

						final String ip = player.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && player != null) {
							if (!Config.ALLOW_DUALBOX) {

								output = false;
								break;

							}

							if (boxes_number + 1 > Config.ALLOWED_BOXES) { // actual count+actual player one
								output = false;
								break;
							}
							boxes_number++;
							active_boxes.add(player.getName());
						}
					}
				}
			}
		}

		if (output) {
			_active_boxes = boxes_number + 1; // current number of boxes+this one
			if (!active_boxes.contains(this.getName())) {
				active_boxes.add(this.getName());

				this.active_boxes_characters = active_boxes;
			}
			refreshOtherBoxes(pl);
		}

		return output;
	}

	public void refreshOtherBoxes(Player pl) {

		if (pl.getClient() != null && pl.getClient().getConnection() != null && !pl.getClient().getConnection().isClosed() && pl.getClient().getConnection().getSocket().getInetAddress() != null) {

			final String thisip = pl.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
			final Collection<Player> allPlayers = GameObjectsStorage.getAllPlayers();
			final Player[] players = allPlayers.toArray(new Player[allPlayers.size()]);

			for (Player player : players) {
				if (player != null) {
					if (player.getClient() != null && player.getClient().getConnection() != null && !player.getClient().getConnection().isClosed() && !player.getName().equals(this.getName())) {

						final String ip = player.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
						if (thisip.equals(ip) && player != null) {
							_active_boxes = _active_boxes;
							active_boxes_characters = active_boxes_characters;

						}
					}
				}
			}
		}

	}
}

//	public boolean getCheckMultiBox(Player pl)
//	{
//		boolean output = true;
//
//		int boxes_number = 1;
//
//
//		if (pl.getClient() != null && ! pl.getClient().getConnection().isClosed())
//		{
//			String thisip = pl.getClient().getConnection().getSocket().getInetAddress().getHostAddress();//getClient().getIpAddr();
//			Collection<Player> allPlayers = GameObjectsStorage.getAllPlayers();
//			Player[] players = allPlayers.toArray(new Player[allPlayers.size()]);
//
//
//			for (Player player : players)
//			{
//				if (player != null)
//				{
//					if (player.getClient() != null && !player.getClient().getConnection().isClosed())
//					{
//						String ip = player.getClient().getConnection().getSocket().getInetAddress().getHostAddress();
//						if (thisip.equals(ip) &&  player!= null)
//						{
//							if (!Config.ALLOW_DUALBOX)
//							{
//								output = false;
//								break;
//							}
//							if (boxes_number > _dualBox)
//							{
//								output = true;
//								break;
//							}
//							boxes_number++;
//							active_boxes_char.add(getName());
//						}
//					 }
//				}
//			}
//
//		}
//		if (output)
//		{
//			_active_boxes = boxes_number;
//		}
//		if (!_active_boxes)
//		{
//
//		}
//		System.out.println("NUBMER OFF PLAYERS" + boxes_number);
//		return output;
//	}
//}
