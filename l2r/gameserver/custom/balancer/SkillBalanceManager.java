/*
 * Copyright (C) 2004-2016 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.custom.balancer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import l2r.gameserver.custom.balancer.SkillBalanceHolder.SkillChangeType;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;

/**
 * @author DevAtlas
 */
public class SkillBalanceManager
{
	private static final Logger LOG = LoggerFactory.getLogger(SkillBalanceManager.class);
	
	private final Map<String, SkillBalanceHolder> changes;
	private int size = 0;
	private int olyBalanceSize = 0;
	private int id = 0;
	private String idName = "";
	private boolean edited = false;
	
	public SkillBalanceManager()
	{
		changes = new ConcurrentHashMap<>();
		load();
	}
	
	private void load()
	{
		try
		{
			if (!edited)
			{
				System.out.println(getClass().getSimpleName() + ": Initializing");
			}
			size = 0;
			olyBalanceSize = 0;
			edited = false;
			changes.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File directory = new File("data/skillbalance/");
			File file = new File("");
			if (!directory.isDirectory())
			{
				directory.mkdir();
			}
			else
			{
				for (File fl : directory.listFiles())
				{
					if (!fl.getName().contains("SkillBalance"))
					{
						continue; // Skip renamed files
					}
					idName = fl.getName().replace("SkillBalance-", "");
					idName = idName.replace(".xml", "");
					if (Integer.valueOf(idName) > id)
					{
						id = Integer.valueOf(idName);
					}
				}
				String info = "data/skillbalance/SkillBalance-" + String.valueOf(id) + ".xml";
				file = new File(info);
				System.out.println("Skill Balance file: " + file.getName());
			}
			Document doc = null;
			
			if (file.exists())
			{
				try
				{
					doc = factory.newDocumentBuilder().parse(file);
				}
				catch (Exception e)
				{
					LOG.warn("Could not parse " + file.getName() + " file: " + e.getMessage());
					e.printStackTrace();
				}
				
				if (doc == null)
				{
					LOG.warn("Can't load " + file.getName() + " file!");
					return;
				}
				
				Node n = doc.getFirstChild();
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equals("balance"))
					{
						try
						{
							int skillId = Integer.parseInt(d.getAttributes().getNamedItem("skillId").getNodeValue());
							// int skillLevel =
							// Integer.parseInt(d.getAttributes().getNamedItem("skillLevel").getNodeValue());
							int target = Integer.parseInt(d.getAttributes().getNamedItem("target").getNodeValue());
							SkillBalanceHolder cbh = new SkillBalanceHolder(skillId, target);
							for (Node set = d.getFirstChild(); set != null; set = set.getNextSibling())
							{
								if (set.getNodeName().equals("set"))
								{
									double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
									SkillChangeType atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
									cbh.addSkillBalance(atkType, val);
									size++;
								}
								else if (set.getNodeName().equals("olyset"))
								{
									double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
									SkillChangeType atkType = SkillChangeType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
									cbh.addOlySkillBalance(atkType, val);
									olyBalanceSize++;
								}
							}
							
							changes.put(skillId + ";" + target, cbh);
						}
						catch (Exception ex)
						{
							LOG.warn(getClass().getSimpleName() + ": Error while reading data: " + ex.getMessage());
						}
					}
				}
			}
			
			System.out.println(getClass().getSimpleName() + ": Successfully loaded " + size + " skill balance edits.");
			System.out.println(getClass().getSimpleName() + ": Successfully loaded " + olyBalanceSize + " olympiad skill balance edits.");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void removeSkillBalance(String key, SkillChangeType type, boolean isOly)
	{
		if (!edited)
		{
			edited = true;
		}
		if (changes.containsKey(key))
		{
			if (isOly)
			{
				changes.get(key).removeOly(type);
				olyBalanceSize--;
			}
			else
			{
				changes.get(key).remove(type);
				size--;
			}
		}
	}
	
	public void addSkillBalance(String skill, SkillBalanceHolder sbh, boolean isEdit)
	{
		if (!edited)
		{
			edited = true;
		}
		changes.put(skill, sbh);
		if (!isEdit)
		{
			if (!sbh.getOlyBalance().isEmpty())
			{
				olyBalanceSize++;
			}
			else
			{
				size++;
			}
		}
	}
	
	public Map<String, SkillBalanceHolder> getAllBalances()
	{
		Map<String, SkillBalanceHolder> _map2 = new TreeMap<>(new SkillComparator());
		_map2.putAll(changes);
		return _map2;
	}
	
	public List<SkillBalanceHolder> getSkillBalances(int skillId)
	{
		List<SkillBalanceHolder> _list = new ArrayList<>();
		for (Entry<String, SkillBalanceHolder> data : changes.entrySet())
		{
			if (Integer.valueOf(data.getKey().split(";")[0]) == skillId)
			{
				_list.add(data.getValue());
			}
		}
		return _list;
	}
	
	public int getSkillBalanceSize(int skillId, boolean olysize)
	{
		int size = 0;
		
		for (SkillBalanceHolder data : getSkillBalances(skillId))
		{
			
			size += (!olysize) ? data.getNormalBalance().size() : data.getOlyBalance().size();
		}
		
		return size;
	}
	
	public double getSkillValue(String sk, SkillChangeType sct, Creature victim)
	{
		if (changes.containsKey(sk) || changes.containsKey(sk.split(";")[0] + ";-2"))
		{
			if (!sk.split(";")[1].equals("-2") && !changes.containsKey(sk))
			{
				sk = sk.split(";")[0] + ";-2";
			}
			if ((victim != null) || (sct.isForceCheck()))
			{
				if (victim instanceof Player)
				{
					if (victim.getPlayer().isOlympiadGameStart() && (victim.getPlayer().getOlympiadGame() != null))
					{
						if (changes.containsKey(sk))
						{
							return changes.get(sk).getOlyBalanceValue(sct);
						}
					}
				}
				return changes.get(sk).getValue(sct);
			}
		}
		
		return 1.0;
		
	}
	
	public int getSize(boolean olysize)
	{
		return (olysize) ? olyBalanceSize : size;
	}
	
	public SkillBalanceHolder getSkillHolder(String key)
	{
		return changes.get(key);
	}
	
	public void rewriteToXml()
	{
		if (!edited)
		{
			System.out.println("Skill Balance Manager: Nothing to save!");
			return;
		}
		try
		{
			id++;
			File file = new File("data/skillbalance/SkillBalance-" + String.valueOf(id) + ".xml");
			if (!file.exists())
			{
				file.createNewFile();
			}
			System.out.println(getClass().getSimpleName() + " save new file: " + file.getName());
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			out.write("<!-- Saved at (" + Calendar.getInstance().getTime().toString() + ") Powered By DevAtlas -->\n");
			out.write("<list>\n");
			for (SkillBalanceHolder cbh : changes.values())
			{
				
				if (cbh.getNormalBalance().isEmpty() && cbh.getOlyBalance().isEmpty())
				{
					continue;
				}
				String xml = "\t<balance ";
				xml += "skillId=\"" + cbh.getSkillId() + "\" ";
				// xml += "skillLevel=\"" + cbh.getSkillLevel() + "\" ";
				xml += "target=\"" + cbh.getTarget() + "\">\n";
				
				for (Entry<SkillChangeType, Double> info : cbh.getNormalBalance().entrySet())
				{
					xml += "\t\t<set type=\"" + info.getKey().toString() + "\" ";
					xml += "val=\"" + info.getValue() + "\"/>\n";
				}
				for (Entry<SkillChangeType, Double> info : cbh.getOlyBalance().entrySet())
				{
					xml += "\t\t<olyset type=\"" + info.getKey().toString() + "\" ";
					xml += "val=\"" + info.getValue() + "\"/>\n";
				}
				xml += "\t</balance>\n";
				out.write(xml);
				
			}
			out.write("</list>");
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		load();
	}
	
	private class SkillComparator implements Comparator<String>
	{
		public SkillComparator()
		{
		}
		
		@Override
		public int compare(String l, String r)
		{
			int left = Integer.valueOf(l.split(";")[0]);
			int right = Integer.valueOf(r.split(";")[0]);
			if (left > right)
			{
				return 1;
			}
			if (left < right)
			{
				return -1;
			}
			if (Integer.valueOf(l.split(";")[1]) > Integer.valueOf(r.split(";")[1]))
			{
				return 1;
			}
			if (Integer.valueOf(r.split(";")[1]) > Integer.valueOf(l.split(";")[1]))
			{
				return -1;
			}
			
			Random x = new Random();
			
			return (x.nextInt(2) == 1) ? 1 : 1;
		}
	}
	
	public static final SkillBalanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillBalanceManager _instance = new SkillBalanceManager();
	}
}
