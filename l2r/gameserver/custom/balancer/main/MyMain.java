/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.custom.balancer.main;

import l2r.gameserver.GameServer;
import l2r.gameserver.custom.balancer.ClassBalanceGui;
import l2r.gameserver.custom.balancer.ClassBalanceManager;
import l2r.gameserver.custom.balancer.SkillBalanceGui;
import l2r.gameserver.custom.balancer.SkillBalanceManager;
import l2r.gameserver.handler.admincommands.AdminCommandHandler;
import l2r.gameserver.handler.bbs.CommunityBoardManager;

/**
 * @author DevAtlas
 */
public class MyMain
{
	// private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	protected MyMain()
	{
		load();
	}
	
	private static void load()
	{
		
		GameServer.printSection("Balance System Configs");
		AdminCommandHandler.getInstance().registerAdminCommandHandler(new AtlasCustomAdminCommands());
		
		CommunityBoardManager.getInstance().registerHandler(ClassBalanceGui.getInstance());
		CommunityBoardManager.getInstance().registerHandler(SkillBalanceGui.getInstance());
		ClassBalanceManager.getInstance();
		SkillBalanceManager.getInstance();
	}
	
	public static MyMain getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MyMain _instance = new MyMain();
	}
	
}
