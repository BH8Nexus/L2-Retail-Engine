package l2r.gameserver.model.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.reward.RewardItem;

public enum Division
{
	NONE,
	BRONZE
	{
		@Override
		public final int getTitleColor()
		{
			// return 0xcd7f32;
			return 0x327FCD;
		}
		
		@Override
		public String getReservedTitle()
		{
			return "Bronze Division";
		}
		
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			if (player.getDivisionPoints() >= 500)
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 13, 13, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 16, 16, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 19, 19, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 22, 22, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 25, 25, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
		
		@Override
		public final Skill[] getBonusSkills()
		{
			return new Skill[] {};
		}
	},
	SILVER
	{
		@Override
		public final int getTitleColor()
		{
			// return 0xCCCCCC;
			return 0xAAAAAA;
		}
		
		@Override
		public String getReservedTitle()
		{
			return "Silver Division";
		}
		
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			if (player.getDivisionPoints() >= 1000)
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					list.add(new RewardItem(14720, 8, 8, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 13, 13, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 16, 16, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 19, 19, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 22, 22, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
	},
	GOLD
	{
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			if (player.getDivisionPoints() >= 2500)
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public final int getTitleColor()
		{
			// return 0xFFD700;
			return 0x00F7FF;
		}
		
		@Override
		public String getReservedTitle()
		{
			return "Gold Division";
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					list.add(new RewardItem(14720, 6, 6, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 8, 8, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 13, 13, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 16, 16, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 19, 19, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
	},
	PLATINUM
	{
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			if (player.getDivisionPoints() >= 5000)
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public final int getTitleColor()
		{
			// return 0xe5e4e2;
			return 0xFFFF99;
		}
		
		@Override
		public String getReservedTitle()
		{
			return "Platinum Division";
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					list.add(new RewardItem(14720, 4, 4, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 6, 6, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 8, 8, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 13, 13, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 16, 16, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
	},
	DIAMOND
	{
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			if (player.getDivisionPoints() >= 10000)
			{
				return true;
			}
			
			return false;
		}
		
		@Override
		public final int getTitleColor()
		{
			// return 0x7D1242;
			return 0xFFFF00;
		}
		
		@Override
		public String getReservedTitle()
		{
			return "Diamond Division";
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					list.add(new RewardItem(14720, 2, 2, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 4, 4, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 6, 6, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 8, 8, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 13, 13, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
	},
	CHALLENGER
	{
		@Override
		public final boolean isRequirementsMet(Player player)
		{
			return false;
		}
		
		@Override
		public final int getTitleColor()
		{
			return 0x55fa4a;
		}
		
		@Override
		public final List<RewardItem> calculatePvpReward(Player victim)
		{
			List<RewardItem> list = new ArrayList<>(1);
			switch (victim.getDivision())
			{
				case BRONZE:
					// list.add(new RewardItem(14720, 0, 0, 1000000)); // Event - Apiga
					break;
				case SILVER:
					list.add(new RewardItem(14720, 2, 2, 1000000)); // Event - Apiga
					break;
				case GOLD:
					list.add(new RewardItem(14720, 4, 4, 1000000)); // Event - Apiga
					break;
				case PLATINUM:
					list.add(new RewardItem(14720, 6, 6, 1000000)); // Event - Apiga
					break;
				case DIAMOND:
					list.add(new RewardItem(14720, 8, 8, 1000000)); // Event - Apiga
					break;
				case CHALLENGER:
					list.add(new RewardItem(14720, 10, 10, 1000000)); // Event - Apiga
					break;
			default:
				break;
			}
			return list;
		}
	};
	
	public int getTitleColor()
	{
		return 0xffffff;
	}
	
	public String getReservedTitle()
	{
		return null;
	}
	
	public boolean isRequirementsMet(Player player)
	{
		return true;
	}
	
	public List<RewardItem> calculatePvpReward(Player victim)
	{
		return Collections.emptyList();
	}
	
	public Skill[] getBonusSkills()
	{
		return new Skill[] {};
	}
	
	public static void update(Player player)
	{
		Division division = Division.NONE;
		for (Division div : values()) // Should start from the lowest to the highest.
		{
			if (div.isRequirementsMet(player))
			{
				division = div;
			}
		}
		
		// Set the top possible division.
		if (player.getDivision() != division)
		{
			player.setDivision(division);
			player.setTitleColor(division.getTitleColor());
			
			if (division.getReservedTitle() != null)
			{
				player.setTitle(division.getReservedTitle());
			}
			else if ((division == NONE) && Arrays.stream(Division.values()).anyMatch(div -> (div.getReservedTitle() != null) && div.getReservedTitle().equals(player.getTitle())))
			{
				player.setTitle(""); // There is a reserved title to be removed
			}
			
			player.broadcastCharInfo();
			
		}
	}
}