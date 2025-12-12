package l2r.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2361753521055171236L;
	private final Set<GlobalEvent> _events = new HashSet<>(2);
	
	@SuppressWarnings("unchecked")
	public <E extends GlobalEvent> E getEvent(Class<E> eventClass)
	{
		for (GlobalEvent e : _events)
		{
			if (e.getClass() == eventClass)
			{
				return (E) e;
			}
			if (eventClass.isAssignableFrom(e.getClass()))
			{
				return (E) e;
			}
		}
		
		return null;
	}
	
	public void addEvent(GlobalEvent event)
	{
		_events.add(event);
	}
	
	public void removeEvent(GlobalEvent event)
	{
		_events.remove(event);
	}
	
	public Set<GlobalEvent> getEvents()
	{
		return _events;
	}
}
