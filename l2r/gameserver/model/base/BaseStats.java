package l2r.gameserver.model.base;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import l2r.gameserver.Config;
import l2r.gameserver.model.Creature;

public enum BaseStats
{
	STR
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getSTR();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : STRbonus[actor.getSTR()];
		}
		
		@Override
		public final double calcChanceMod(Creature actor)
		{
			return Math.min(2. - Math.sqrt(calcBonus(actor)), 1.);
		}
	},
	INT
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getINT();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : INTbonus[actor.getINT()];
		}
	},
	DEX
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getDEX();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : DEXbonus[actor.getDEX()];
		}
	},
	WIT
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getWIT();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : WITbonus[actor.getWIT()];
		}
	},
	CON
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getCON();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : CONbonus[actor.getCON()];
		}
	},
	MEN
	{
		@Override
		public final int getStat(Creature actor)
		{
			return actor == null ? 1 : actor.getMEN();
		}
		
		@Override
		public final double calcBonus(Creature actor)
		{
			return actor == null ? 1. : MENbonus[actor.getMEN()];
		}
	},
	NONE;
	
	public static final BaseStats[] VALUES = values();
	
	protected static final Logger _log = LoggerFactory.getLogger(BaseStats.class);
	
	private static final int MAX_STAT_VALUE = 100;
	
	public static final double[] STRbonus = new double[MAX_STAT_VALUE];
	public static final double[] INTbonus = new double[MAX_STAT_VALUE];
	public static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	public static final double[] WITbonus = new double[MAX_STAT_VALUE];
	public static final double[] CONbonus = new double[MAX_STAT_VALUE];
	public static final double[] MENbonus = new double[MAX_STAT_VALUE];
	
	public static final int[] ACCURACY_EVASION_LEVEL_BONUS =
	{
		0,
		1,
		2,
		3,
		4,
		5,
		6,
		7,
		8,
		9,
		10, // 1-10
		11,
		12,
		13,
		14,
		15,
		16,
		17,
		18,
		18,
		20, // 11-20
		21,
		22,
		23,
		24,
		25,
		26,
		27,
		28,
		29,
		30, // 21-30
		31,
		32,
		33,
		34,
		35,
		36,
		37,
		38,
		39,
		40, // 31-40
		41,
		42,
		43,
		44,
		45,
		46,
		47,
		48,
		49,
		50, // 41-50
		51,
		52,
		53,
		54,
		55,
		56,
		57,
		58,
		59,
		60, // 51-60
		61,
		62,
		63,
		64,
		65,
		66,
		67,
		68,
		69,
		71, // 61-70
		73,
		75,
		77,
		79,
		81,
		83,
		85,
		89,
		91,
		94, // 71-80
		96,
		98,
		100,
		103,
		105,
		107,
		109,
		112,
		114,
		116, // 81-90
		118,
		120,
		122,
		125,
		127,
		129,
		132,
		134,
		136,
		138 // 91-100
	};
	
	public int getStat(Creature actor)
	{
		return 1;
	}
	
	public double calcBonus(Creature actor)
	{
		return 1.;
	}
	
	public double calcChanceMod(Creature actor)
	{
		return 2. - Math.sqrt(calcBonus(actor));
	}
	
	public static final BaseStats valueOfXml(String name)
	{
		name = name.intern();
		for (BaseStats s : VALUES)
		{
			if (s.toString().equalsIgnoreCase(name))
			{
				if (s == NONE)
				{
					return null;
				}
				
				return s;
			}
		}
		
		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}
	
	static
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/xml/attribute_bonus.xml");
		Document doc = null;
		
		try
		{
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch (SAXException e)
		{
			_log.error("", e);
		}
		catch (IOException e)
		{
			_log.error("", e);
		}
		catch (ParserConfigurationException e)
		{
			_log.error("", e);
		}
		
		int i;
		double val;
		
		if (doc != null)
		{
			for (Node z = doc.getFirstChild(); z != null; z = z.getNextSibling())
			{
				for (Node n = z.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if (n.getNodeName().equalsIgnoreCase("str_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								STRbonus[i] = (100 + val) / 100;
							}
						}
					}
					if (n.getNodeName().equalsIgnoreCase("int_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								INTbonus[i] = (100 + val) / 100;
							}
						}
					}
					if (n.getNodeName().equalsIgnoreCase("con_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								CONbonus[i] = (100 + val) / 100;
							}
						}
					}
					if (n.getNodeName().equalsIgnoreCase("men_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								MENbonus[i] = (100 + val) / 100;
							}
						}
					}
					if (n.getNodeName().equalsIgnoreCase("dex_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								DEXbonus[i] = (100 + val) / 100;
							}
						}
					}
					if (n.getNodeName().equalsIgnoreCase("wit_bonus"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							String node = d.getNodeName();
							if (node.equalsIgnoreCase("set"))
							{
								i = Integer.valueOf(d.getAttributes().getNamedItem("attribute").getNodeValue());
								val = Integer.valueOf(d.getAttributes().getNamedItem("val").getNodeValue());
								WITbonus[i] = (100 + val) / 100;
							}
						}
					}
				}
			}
		}
	}
}