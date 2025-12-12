package l2r.gameserver.data.xml;

import l2r.gameserver.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.StringHolder;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.ExtractableItemsData;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.data.xml.holder.ProductHolder;
import l2r.gameserver.data.xml.holder.RecipeHolder;
import l2r.gameserver.data.xml.parser.AirshipDockParser;
import l2r.gameserver.data.xml.parser.ArmorSetsParser;
import l2r.gameserver.data.xml.parser.CharTemplateParser;
import l2r.gameserver.data.xml.parser.CubicParser;
import l2r.gameserver.data.xml.parser.DomainParser;
import l2r.gameserver.data.xml.parser.DonationParse;
import l2r.gameserver.data.xml.parser.DoorParser;
import l2r.gameserver.data.xml.parser.DressArmorParser;
import l2r.gameserver.data.xml.parser.DressCloakParser;
import l2r.gameserver.data.xml.parser.DressShieldParser;
import l2r.gameserver.data.xml.parser.DressWeaponParser;
import l2r.gameserver.data.xml.parser.EnchantItemParser;
import l2r.gameserver.data.xml.parser.EventParser;
import l2r.gameserver.data.xml.parser.ExchangeItemParser;
import l2r.gameserver.data.xml.parser.FacebookCommentsParser;
import l2r.gameserver.data.xml.parser.FightClubMapParser;
import l2r.gameserver.data.xml.parser.FoundationParser;
import l2r.gameserver.data.xml.parser.HennaParser;
import l2r.gameserver.data.xml.parser.HuntingZoneParser;
import l2r.gameserver.data.xml.parser.InstantZoneParser;
import l2r.gameserver.data.xml.parser.ItemParser;
import l2r.gameserver.data.xml.parser.KarmaData;
import l2r.gameserver.data.xml.parser.NpcParser;
import l2r.gameserver.data.xml.parser.OptionDataParser;
import l2r.gameserver.data.xml.parser.PetDataTemplateParser;
import l2r.gameserver.data.xml.parser.PetitionGroupParser;
import l2r.gameserver.data.xml.parser.PlayerXpPercentLostData;
import l2r.gameserver.data.xml.parser.PremiumParser;
import l2r.gameserver.data.xml.parser.ResidenceParser;
import l2r.gameserver.data.xml.parser.RestartPointParser;
import l2r.gameserver.data.xml.parser.SkillAcquireParser;
import l2r.gameserver.data.xml.parser.SoulCrystalParser;
import l2r.gameserver.data.xml.parser.SpawnParser;
import l2r.gameserver.data.xml.parser.StaticObjectParser;
import l2r.gameserver.data.xml.parser.TournamentMapParser;
import l2r.gameserver.data.xml.parser.ZoneParser;
import l2r.gameserver.instancemanager.ReflectionManager;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.tables.SpawnTable;

public abstract class Parsers
{
	public static void parseAll()
	{
		HtmCache.getInstance().reload();
		StringHolder.getInstance().load();
		//
		SkillTable.getInstance().load(); // - SkillParser.getInstance();
		
		if (!Config.DONTLOADOPTIONDATA)
		{
			OptionDataParser.getInstance().load();
		}
		ItemParser.getInstance().load();
		//
		NpcParser.getInstance().load();
		
		PetDataTemplateParser.getInstance().load();
		DomainParser.getInstance().load();
		RestartPointParser.getInstance().load();
		
		StaticObjectParser.getInstance().load();
		DoorParser.getInstance().load();
		ZoneParser.getInstance().load();
		SpawnTable.getInstance();
		SpawnParser.getInstance().load();
		InstantZoneParser.getInstance().load();
		ReflectionManager.getInstance();
		//
		AirshipDockParser.getInstance().load();
		SkillAcquireParser.getInstance().load();
		//
		CharTemplateParser.getInstance().load();
		//
		ResidenceParser.getInstance().load();
		EventParser.getInstance().load();
		FightClubMapParser.getInstance().load();
		// support(cubic & agathion)
		CubicParser.getInstance().load();
		//
		BuyListHolder.getInstance();
		RecipeHolder.getInstance();
		if (!Config.DONTLOADMULTISELLS)
		{
			MultiSellHolder.getInstance();
		}
		ProductHolder.getInstance();
		// AgathionParser.getInstance();
		// item support
		HennaParser.getInstance().load();
		EnchantItemParser.getInstance().load();
		SoulCrystalParser.getInstance().load();
		ArmorSetsParser.getInstance().load();
		
		// etc
		PetitionGroupParser.getInstance().load();
		HuntingZoneParser.getInstance().load();
		PlayerXpPercentLostData.getInstance().load();
		KarmaData.getInstance().load();
		ExtractableItemsData.getInstance().load();
		
		// New
		TournamentMapParser.getInstance().load();
		FacebookCommentsParser.getInstance().load();
		// Premium
		PremiumParser.getInstance().load();
		// ExtractableSkillsData.getInstance().load();
		DressArmorParser.getInstance().load();
		DressCloakParser.getInstance().load();
		DressShieldParser.getInstance().load();
		DressWeaponParser.getInstance().load();
		
		// donation
		DonationParse.getInstance().load();
		FoundationParser.getInstance().load();
		ExchangeItemParser.getInstance().load();
		
	}
}
