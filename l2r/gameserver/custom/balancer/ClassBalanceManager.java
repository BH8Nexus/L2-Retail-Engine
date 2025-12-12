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

import l2r.gameserver.custom.balancer.ClassBalanceHolder.AttackType;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.MonsterInstance;

/**
 * @author DevAtlas
 */
public class ClassBalanceManager
{
	private static final Logger LOG = LoggerFactory.getLogger(ClassBalanceManager.class);
	
	private final Map<String, ClassBalanceHolder> classes;
	private int size = 0;
	private int olyBalanceSize = 0;
	private int id = 0;
	private String idName = "";
	private boolean edited = false;
	
	public ClassBalanceManager()
	{
		classes = new ConcurrentHashMap<>();
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
			classes.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File directory = new File("data/classbalance/");
			File file = new File("");
			if (!directory.isDirectory())
			{
				directory.mkdir();
			}
			else// if (directory.isDirectory())
			{
				for (File fl : directory.listFiles())
				{
					if (!fl.getName().contains("ClassBalance"))
					{
						continue; // Skip renamed files
					}
					idName = fl.getName().replace("ClassBalance-", "");
					idName = idName.replace(".xml", "");
					if (Integer.valueOf(idName) > id)
					{
						id = Integer.valueOf(idName);
					}
				}
				String info = "data/classbalance/ClassBalance-" + String.valueOf(id) + ".xml";
				file = new File(info);
				System.out.println("Class Balance file: " + file.getName());
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
						int classId = Integer.parseInt(d.getAttributes().getNamedItem("classId").getNodeValue());
						int targetClassId = Integer.parseInt(d.getAttributes().getNamedItem("targetClassId").getNodeValue());
						ClassBalanceHolder cbh = new ClassBalanceHolder(classId, targetClassId);
						for (Node set = d.getFirstChild(); set != null; set = set.getNextSibling())
						{
							if (set.getNodeName().equals("set"))
							{
								double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
								AttackType atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
								cbh.addNormalBalance(atkType, val);
								size++;
							}
							else if (set.getNodeName().equals("olyset"))
							{
								double val = Double.parseDouble(set.getAttributes().getNamedItem("val").getNodeValue());
								AttackType atkType = AttackType.valueOf(set.getAttributes().getNamedItem("type").getNodeValue());
								cbh.addOlyBalance(atkType, val);
								olyBalanceSize++;
							}
						}
						String key = classId + ";" + targetClassId;
						classes.put(key, cbh);
					}
				}
			}
			
			System.out.println(getClass().getSimpleName() + ": Successfully loaded " + size + " class balance edits.");
			System.out.println(getClass().getSimpleName() + ": Successfully loaded " + olyBalanceSize + " olympiad class balance edits.");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public Map<String, ClassBalanceHolder> getAllBalances()
	{
		Map<String, ClassBalanceHolder> _map2 = new TreeMap<>(new ClassComparator());
		_map2.putAll(classes);
		return _map2;
	}
	
	public List<ClassBalanceHolder> getClassBalances(int classId)
	{
		List<ClassBalanceHolder> _list = new ArrayList<>();
		for (Entry<String, ClassBalanceHolder> data : classes.entrySet())
		{
			if (Integer.valueOf(data.getKey().split(";")[0]) == classId)
			{
				_list.add(data.getValue());
			}
		}
		return _list;
	}
	
	public int getClassBalanceSize(int classId, boolean olysize)
	{
		int size = 0;
		
		for (ClassBalanceHolder data : getClassBalances(classId))
		{
			
			size += (!olysize) ? data.getNormalBalance().size() : data.getOlyBalance().size();
		}
		
		return size;
	}
	
	public ClassBalanceHolder getBalanceHolder(String key)
	{
		return classes.get(key);
	}
	
	private class ClassComparator implements Comparator<String>
	{
		public ClassComparator()
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
	
	public double getBalancedClass(AttackType type, Creature attacker, Creature victim)
	{
		if ((attacker instanceof Player) && (victim instanceof Player))
		{
			int classId = attacker.getPlayer().getClassId().getId();
			int targetClassId = victim.getPlayer().getClassId().getId();
			if (attacker.getPlayer().isInOlympiadMode() && victim.getPlayer().isInOlympiadMode())
			{
				if (attacker.getPlayer().getOlympiadGame() == victim.getPlayer().getOlympiadGame())
				{
					if (classes.containsKey(classId + ";" + targetClassId))
					{
						return classes.get(classId + ";" + targetClassId).getOlyBalanceValue(type);
					}
				}
				return (classes.containsKey(classId + ";-2") ? classes.get(classId + ";-2").getOlyBalanceValue(type) : 1);
			}
			
			if (classes.containsKey(classId + ";" + targetClassId))
			{
				return classes.get(classId + ";" + targetClassId).getBalanceValue(type);
			}
			return (classes.containsKey(classId + ";-2") ? classes.get(classId + ";-2").getBalanceValue(type) : 1);
		}
		else if ((attacker instanceof Player) && (victim instanceof MonsterInstance))
		{
			int classId = attacker.getPlayer().getClassId().getId();
			if (classes.containsKey(classId + ";-1"))
			{
				return classes.get(classId + ";-1").getBalanceValue(type);
			}
		}
		return 1;
	}
	
	public void removeClassBalance(String key, AttackType type, boolean isOly)
	{
		if (classes.containsKey(key))
		{
			if (!edited)
			{
				edited = true;
			}
			if (isOly)
			{
				classes.get(key).removeOlyBalance(type);
				olyBalanceSize--;
			}
			else
			{
				classes.get(key).remove(type);
				size--;
			}
		}
	}
	
	public void addClassBalance(String key, ClassBalanceHolder cbh, boolean isEdit)
	{
		if (!edited)
		{
			edited = true;
		}
		classes.put(key, cbh);
		if (!isEdit)
		{
			if (!cbh.getOlyBalance().isEmpty())
			{
				olyBalanceSize++;
			}
			else
			{
				size++;
			}
		}
	}
	
	public void rewriteToXml()
	{
		if (!edited)
		{
			System.out.println(getClass().getSimpleName() + ": Nothing to save!");
			return;
		}
		try
		{
			id++;
			File file = new File("data/classbalance/ClassBalance-" + String.valueOf(id) + ".xml");
			if (!file.exists())
			{
				file.createNewFile();
			}
			LOG.warn("Class balance save new file: " + file.getName());
			
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			out.write("<!-- Saved at (" + Calendar.getInstance().getTime().toString() + ") Powered By DevAtlas -->\n");
			out.write("<list>\n");
			for (ClassBalanceHolder cbh : classes.values())
			{
				
				if (cbh.getNormalBalance().isEmpty() && cbh.getOlyBalance().isEmpty())
				{
					continue;
				}
				String xml = "\t<balance ";
				xml += "classId=\"" + cbh.getActiveClass() + "\" ";
				xml += "targetClassId=\"" + cbh.getTargetClass() + "\"";
				xml += ">\n";
				
				for (Entry<AttackType, Double> info : cbh.getNormalBalance().entrySet())
				{
					xml += "\t\t<set type=\"" + info.getKey().toString() + "\" ";
					xml += "val=\"" + info.getValue() + "\"/>\n";
				}
				for (Entry<AttackType, Double> info : cbh.getOlyBalance().entrySet())
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
	
	public static final ClassBalanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public int getSize(boolean olysize)
	{
		return (olysize) ? olyBalanceSize : size;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassBalanceManager _instance = new ClassBalanceManager();
	}
	
}
