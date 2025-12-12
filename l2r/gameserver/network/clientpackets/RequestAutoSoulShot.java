package l2r.gameserver.network.clientpackets;

import org.apache.commons.lang3.ArrayUtils;

import l2r.gameserver.handler.items.IItemHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Summon;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExAutoSoulShot;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

/**
 * format: chdd
 * @param decrypt
 */
public class RequestAutoSoulShot extends L2GameClientPacket
{
	private int _itemId;
	private boolean _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) || activeChar.isDead())
		{
			return;
		}
		
		ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
		
		if (item == null)
		{
			return;
		}
		
		// final ItemAction[] allow_action = new ItemAction[]{ItemAction.action_fishingshot, ItemAction.action_soulshot, ItemAction.action_spiritshot, ItemAction.action_summon_soulshot, ItemAction.action_summon_spiritshot};
		// if (!ArrayUtils.contains(allow_action, item.getTemplate().getActionType())) {
		// Log.audit("[RequestAutoSoulShot]:", " : used bug player name - " + activeChar.getName() + " ip - " + activeChar.getIP() + " login - " + activeChar.getAccountName());
		// return;
		// }
		
		if (_type)
		{
			if (ArrayUtils.contains(Summon.BEAST_SHOTS, _itemId) && (activeChar.getPet() == null))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATICUSE_FUNCTION);
				return;
			}
		}
		
		if (_type)
		{
			activeChar.addAutoSoulShot(_itemId);
			activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
			activeChar.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addString(item.getName()));
			IItemHandler handler = item.getTemplate().getHandler();
			handler.useItem(activeChar, item, false);
			return;
		}
		
		activeChar.removeAutoSoulShot(_itemId);
		activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
		activeChar.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addString(item.getName()));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}