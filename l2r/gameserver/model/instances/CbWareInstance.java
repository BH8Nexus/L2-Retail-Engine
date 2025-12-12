package l2r.gameserver.model.instances;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.templates.npc.NpcTemplate;

public class CbWareInstance extends NpcInstance
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public CbWareInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		// TODO Auto-generated constructor stub
	}
	
	
    @Override
    public void showChatWindow(Player player, int val, Object... arg)
    {
        String html = HtmCache.getInstance().getNotNull("CommunityBoard/Main/warehouse.htm", player);
        ShowBoard.separateAndSend(html, player);
    }

}
