// package l2r.gameserver.model;
//
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.concurrent.locks.Lock;
// import java.util.concurrent.locks.ReentrantLock;
//
// import l2r.gameserver.data.xml.holder.NpcHolder;
// import l2r.gameserver.idfactory.IdFactory;
// import l2r.gameserver.model.instances.MinionInstance;
// import l2r.gameserver.model.instances.MonsterInstance;
// import l2r.gameserver.templates.npc.MinionData;
//
// public class MinionList
// {
// private final Set<MinionData> _minionData;
// private final Set<MinionInstance> _minions;
// private final Lock lock;
// private final MonsterInstance _master;
//
// public MinionList(MonsterInstance master)
// {
// _master = master;
// _minions = new HashSet<>();
// _minionData = new HashSet<>();
// _minionData.addAll(_master.getTemplate().getMinionData());
// lock = new ReentrantLock();
// }
//
// /**
// * Add a template for the minion
// * @param m
// */
// public boolean addMinion(MinionData m)
// {
// lock.lock();
// try
// {
// return _minionData.add(m);
// }
// finally
// {
// lock.unlock();
// }
// }
//
// /**
// * Add minion
// * @param m
// * @return true, If successfully added
// */
// public boolean addMinion(MinionInstance m)
// {
// lock.lock();
// try
// {
// return _minions.add(m);
// }
// finally
// {
// lock.unlock();
// }
// }
//
// /**
// * @return Are there live minions
// */
// public boolean hasAliveMinions()
// {
// lock.lock();
// try
// {
// for (MinionInstance m : _minions)
// {
// if (m.isVisible() && !m.isDead())
// {
// return true;
// }
// }
// }
// finally
// {
// lock.unlock();
// }
// return false;
// }
//
// public boolean hasMinions()
// {
// return _minionData.size() > 0;
// }
//
// /**
// * Returns the list of live minions
// * @return List of live minions
// */
// public List<MinionInstance> getAliveMinions()
// {
// List<MinionInstance> result = new ArrayList<>(_minions.size());
// lock.lock();
// try
// {
// for (MinionInstance m : _minions)
// {
// if (m.isVisible() && !m.isDead())
// {
// result.add(m);
// }
// }
// }
// finally
// {
// lock.unlock();
// }
// return result;
// }
//
// /**
// * Spavnit all the missing minions
// */
// public void spawnMinions()
// {
// lock.lock();
// try
// {
// int minionCount;
// int minionId;
// for (MinionData minion : _minionData)
// {
// minionId = minion.getMinionId();
// minionCount = minion.getAmount();
//
// for (MinionInstance m : _minions)
// {
// if (m.getNpcId() == minionId)
// {
// minionCount--;
// }
// if (m.isDead() || !m.isVisible())
// {
// m.refreshID();
// m.stopDecay();
// _master.spawnMinion(m);
// _master.setIsLeader(true);
// }
// }
//
// for (int i = 0; i < minionCount; i++)
// {
// MinionInstance m = new MinionInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(minionId));
// m.setLeader(_master);
// _master.spawnMinion(m);
// _master.setIsLeader(true);
// _minions.add(m);
// }
// }
// }
// finally
// {
// lock.unlock();
// }
// }
//
// /**
// * Spavnit all minions
// */
// public void unspawnMinions()
// {
// lock.lock();
// try
// {
// for (MinionInstance m : _minions)
// {
// m.decayMe();
// }
// }
// finally
// {
// lock.unlock();
// }
// }
//
// /**
// * Removes minions and cleans the list
// */
// public void deleteMinions()
// {
// lock.lock();
// try
// {
// for (MinionInstance m : _minions)
// {
// m.deleteMe();
// }
// _minions.clear();
// }
// finally
// {
// lock.unlock();
// }
// }
// }