package l2r.commons.util;

import java.util.List;
import java.util.RandomAccess;

/**
 * @author bloodshed <a href="http://l2nextgen.ru/">L2NextGen</a>
 * @email rkx.bloodshed@gmail.com
 * @date 11.04.13
 * @time 22:04
 */
public interface FastAccessList<E> extends List<E>, RandomAccess
{
	public E getUnsafe(final int index);
	
	public void addLastUnsafe(final E value);
	
	public void setUnsafeVoid(final int index, final E value);
	
	public boolean forEach(final INgObjectProcedure<E> procedure);
	
	public void forEach(final INgVoidProcedure<E> procedure);
	
	public <E> E[] toNativeArray();
}
