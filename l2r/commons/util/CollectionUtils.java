package l2r.commons.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import gnu.trove.list.TIntList;

/**
 * @author bloodshed <a href="http://l2nextgen.ru/">L2NextGen</a>
 * @email rkx.bloodshed@gmail.com
 * @date 17.02.2013
 * @time 4:19:53
 */
@SuppressWarnings(
{
	"unchecked",
	"rawtypes"
})
public final class CollectionUtils
{
	private CollectionUtils()
	{
	}
	
	/**
	 * Returns an immutable list containing only the specified object. The returned list is serializable.
	 * @param o the sole object to be stored in the returned list.
	 * @return an immutable list containing only the specified object.
	 * @since 1.3
	 */
	public final static <T> FastAccessList<T> singletonList(final T o)
	{
		return new SingletonGArrayList<>(o);
	}
	
	/**
	 * @serial include
	 */
	private static final class SingletonGArrayList<E> extends GArray<E> implements RandomAccess
	{
		private final E element;
		
		SingletonGArrayList(final E obj)
		{
			element = obj;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return singletonIterator(element);
		}
		
		@Override
		public int size()
		{
			return 1;
		}
		
		@Override
		public final boolean isEmpty()
		{
			return element == null;
		}
		
		@Override
		public boolean contains(Object obj)
		{
			return Objects.equals(obj, element);
		}
		
		@Override
		public E get(int index)
		{
			if (index != 0)
			{
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
			}
			
			return element;
		}
		
		@Override
		public final E getUnsafe(final int index)
		{
			if (index != 0)
			{
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
			}
			
			return element;
		}
	}
	
	static <E> Iterator<E> singletonIterator(final E e)
	{
		return new Iterator<E>()
		{
			private boolean hasNext = true;
			
			@Override
			public boolean hasNext()
			{
				return hasNext;
			}
			
			@Override
			public E next()
			{
				if (hasNext)
				{
					hasNext = false;
					return e;
				}
				throw new NoSuchElementException();
			}
			
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private static class EmptyIterator<E> implements Iterator<E>
	{
		static final EmptyIterator INSTANCE = new EmptyIterator();
		
		@Override
		public boolean hasNext()
		{
			return false;
		}
		
		@Override
		public E next()
		{
			throw new NoSuchElementException();
		}
		
		@Override
		public void remove()
		{
			//
		}
	}
	
	private static final class EmptyListIterator<E> extends EmptyIterator<E> implements ListIterator<E>
	{
		static final EmptyListIterator INSTANCE = new EmptyListIterator();
		
		@Override
		public boolean hasPrevious()
		{
			return false;
		}
		
		@Override
		public E previous()
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int nextIndex()
		{
			return 0;
		}
		
		@Override
		public int previousIndex()
		{
			return -1;
		}
		
		@Override
		public void add(Object obj)
		{
			//
		}
		
		@Override
		public void set(Object obj)
		{
			//
		}
	}
	
	private static class EmptyCollection<E> implements Collection<E>
	{
		static final EmptyCollection INSTANCE = new EmptyCollection();
		
		@Override
		public boolean add(E e)
		{
			return false;
		}
		
		@Override
		public boolean addAll(Collection<? extends E> c)
		{
			return false;
		}
		
		@Override
		public void clear()
		{
			// do nothing at all
		}
		
		@Override
		public boolean contains(Object o)
		{
			return false;
		}
		
		@Override
		public boolean containsAll(Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return emptyIterator();
		}
		
		@Override
		public boolean remove(Object o)
		{
			return false;
		}
		
		@Override
		public boolean removeAll(Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean retainAll(Collection<?> c)
		{
			return false;
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public Object[] toArray()
		{
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public Object[] toArray(final Object[] a)
		{
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public final String toString()
		{
			return "[]";
		}
	}
	
	private static final class EmptySet<E> extends EmptyCollection<E> implements Set<E>
	{
		static final EmptySet INSTANCE = new EmptySet();
		
		@Override
		public Object[] toArray(final Object[] a)
		{
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
	}
	
	private static final class EmptyList<E> extends EmptyCollection<E> implements List<E>
	{
		static final EmptyList INSTANCE = new EmptyList();
		
		@Override
		public void add(int index, Object element)
		{
			
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends E> c)
		{
			return false;
		}
		
		@Override
		public E get(int index)
		{
			return null;
		}
		
		@Override
		public int indexOf(Object o)
		{
			return -1;
		}
		
		@Override
		public int lastIndexOf(Object o)
		{
			return -1;
		}
		
		@Override
		public ListIterator<E> listIterator()
		{
			return emptyListIterator();
		}
		
		@Override
		public ListIterator<E> listIterator(int index)
		{
			return emptyListIterator();
		}
		
		@Override
		public E remove(int index)
		{
			return null;
		}
		
		@Override
		public Object set(int index, Object element)
		{
			return null;
		}
		
		@Override
		public List<E> subList(int fromIndex, int toIndex)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object[] toArray(final Object[] a)
		{
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
	}
	
	private static final class EmptyMap implements Map<Object, Object>
	{
		static final EmptyMap INSTANCE = new EmptyMap();
		
		@Override
		public void clear()
		{
			// do nothing at all
		}
		
		@Override
		public boolean containsKey(Object key)
		{
			return false;
		}
		
		@Override
		public boolean containsValue(Object value)
		{
			return false;
		}
		
		@Override
		public Set<Map.Entry<Object, Object>> entrySet()
		{
			return emptySet();
		}
		
		@Override
		public Object get(Object key)
		{
			return null;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Set<Object> keySet()
		{
			return emptySet();
		}
		
		@Override
		public Object put(Object key, Object value)
		{
			return null;
		}
		
		@Override
		public void putAll(Map<? extends Object, ? extends Object> m)
		{
			
		}
		
		@Override
		public Object remove(Object key)
		{
			return null;
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public Collection<Object> values()
		{
			return emptyCollection();
		}
		
		@Override
		public String toString()
		{
			return "{}";
		}
	}
	
	private static final class EmptyGArray<E> extends GArray<E>
	{
		static final EmptyGArray INSTANCE = new EmptyGArray();
		
		@Override
		public final Iterator<E> iterator()
		{
			return emptyIterator();
		}
		
		@Override
		public ListIterator<E> listIterator()
		{
			return emptyListIterator();
		}
		
		@Override
		public ListIterator<E> listIterator(int index)
		{
			return emptyListIterator();
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public void ensureCapacity(final int newSize)
		{
		}
		
		@Override
		public boolean contains(final Object obj)
		{
			return false;
		}
		
		@Override
		public E get(final int index)
		{
			return null;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public Object[] toArray()
		{
			return ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public <T> T[] toArray(T[] a)
		{
			return (T[]) ArrayUtils.EMPTY_OBJECT_ARRAY;
		}
		
		@Override
		public boolean add(final E e)
		{
			return false;
		}
		
		@Override
		public boolean remove(final Object o)
		{
			return false;
		}
		
		@Override
		public boolean containsAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean addAll(final Collection<? extends E> c)
		{
			return false;
		}
		
		@Override
		public boolean removeAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public boolean retainAll(final Collection<?> c)
		{
			return false;
		}
		
		@Override
		public void clear()
		{
		}
		
		@Override
		public final boolean forEach(final INgObjectProcedure<E> procedure)
		{
			return true;
		}
		
		@Override
		public final void forEach(final INgVoidProcedure<E> procedure)
		{
		}
		
		@Override
		public boolean retainValues(final INgObjectProcedure<E> procedure)
		{
			return false;
		}
		
		@Override
		public boolean retainValues(final INgObjectProcedure<E> procedure, final boolean compact)
		{
			return compact;
		}
	}
	
	private static final class EmptyQueue<E> extends AbstractQueue<E> implements Serializable, Queue<E>
	{
		static final EmptyQueue INSTANCE = new EmptyQueue();
		
		@Override
		public Iterator<E> iterator()
		{
			return emptyIterator();
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public boolean contains(final Object o)
		{
			return false;
		}
		
		@Override
		public boolean offer(final Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public E poll()
		{
			return null;
		}
		
		@Override
		public E peek()
		{
			return null;
		}
		
		@Override
		public boolean add(final Object e)
		{
			return false;
		}
		
		@Override
		public boolean addAll(final Collection<? extends E> c)
		{
			return false;
		}
		
		@Override
		public E remove()
		{
			return null;
		}
		
		private Object readResolve()
		{
			return emptyQueue();
		}
		
		private static final long serialVersionUID = 0L;
	}
	
	private static final class EmptyConcurrentQueue<E> extends ConcurrentLinkedQueue<E>
	{
		public static final EmptyConcurrentQueue INSTANCE = new EmptyConcurrentQueue();
		
		@Override
		public Iterator<E> iterator()
		{
			return emptyIterator();
		}
		
		@Override
		public int size()
		{
			return 0;
		}
		
		@Override
		public boolean isEmpty()
		{
			return true;
		}
		
		@Override
		public boolean contains(final Object o)
		{
			return false;
		}
		
		@Override
		public boolean offer(final Object e)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public E poll()
		{
			return null;
		}
		
		@Override
		public E peek()
		{
			return null;
		}
		
		@Override
		public boolean add(final Object e)
		{
			return false;
		}
		
		@Override
		public boolean addAll(final Collection<? extends E> c)
		{
			return false;
		}
		
		@Override
		public E remove()
		{
			return null;
		}
		
		private Object readResolve()
		{
			return emptyQueue();
		}
		
		private static final long serialVersionUID = 0L;
	}
	
	public static <T> ListIterator<T> emptyListIterator()
	{
		return EmptyListIterator.INSTANCE;
	}
	
	public static <T> Iterator<T> emptyIterator()
	{
		return EmptyIterator.INSTANCE;
	}
	
	public static <T> Collection<T> emptyCollection()
	{
		return EmptyCollection.INSTANCE;
	}
	
	public static <T> Set<T> emptySet()
	{
		return EmptySet.INSTANCE;
	}
	
	public static <T> List<T> emptyList()
	{
		return EmptyList.INSTANCE;
	}
	
	public static <K, V> Map<K, V> emptyMap()
	{
		return (Map<K, V>) EmptyMap.INSTANCE;
	}
	
	public static final <E> GArray<E> emptyGArray()
	{
		return EmptyGArray.INSTANCE;
	}
	
	public static <T> Queue<T> emptyQueue()
	{
		return EmptyQueue.INSTANCE;
	}
	
	public static <V> ConcurrentLinkedQueue<V> emptyConcurrentQueue()
	{
		return EmptyConcurrentQueue.INSTANCE;
	}
	
	public static <T> Iterable<T> filteredIterable(Class<T> clazz, Iterable<? super T> iterable)
	{
		return filteredIterable(clazz, iterable, null);
	}
	
	public static <T> Iterable<T> filteredIterable(Class<T> clazz, Iterable<? super T> iterable, Filter<T> filter)
	{
		return new FilteredIterable<>(clazz, iterable, filter);
	}
	
	public static <T> Iterator<T> filteredIterator(Class<T> clazz, Iterable<? super T> iterable)
	{
		return filteredIterator(clazz, iterable, null);
	}
	
	public static <T> Iterator<T> filteredIterator(Class<T> clazz, Iterable<? super T> iterable, Filter<T> filter)
	{
		return new FilteredIterator<>(clazz, iterable, filter);
	}
	
	public interface Filter<E>
	{
		public boolean accept(E element);
	}
	
	private static final class FilteredIterable<E> implements Iterable<E>
	{
		private final Iterable<? super E> _iterable;
		private final Filter<E> _filter;
		private final Class<E> _clazz;
		
		private FilteredIterable(Class<E> clazz, Iterable<? super E> iterable, Filter<E> filter)
		{
			_iterable = iterable;
			_filter = filter;
			_clazz = clazz;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return filteredIterator(_clazz, _iterable, _filter);
		}
	}
	
	private static final class FilteredIterator<E> implements Iterator<E>
	{
		private final Iterator<? super E> _iterator;
		private final Filter<E> _filter;
		private final Class<E> _clazz;
		
		private E _next;
		
		private FilteredIterator(Class<E> clazz, Iterable<? super E> iterable, Filter<E> filter)
		{
			_iterator = iterable.iterator();
			_filter = filter;
			_clazz = clazz;
			
			step();
		}
		
		@Override
		public boolean hasNext()
		{
			return _next != null;
		}
		
		@Override
		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			final E next = _next;
			
			step();
			
			return next;
		}
		
		private void step()
		{
			while (_iterator.hasNext())
			{
				final Object next = _iterator.next();
				
				if ((next == null) || !_clazz.isInstance(next))
				{
					continue;
				}
				
				if ((_filter == null) || _filter.accept((E) next))
				{
					_next = (E) next;
					return;
				}
			}
			
			_next = null;
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static <S, T> Iterable<T> convertingIterable(Iterable<? extends S> iterable, Converter<S, T> converter)
	{
		return new ConvertingIterable<>(iterable, converter);
	}
	
	public static <S, T> Iterator<T> convertingIterator(Iterable<? extends S> iterable, Converter<S, T> converter)
	{
		return new ConvertingIterator<>(iterable, converter);
	}
	
	public interface Converter<S, T>
	{
		public T convert(S src);
	}
	
	private static final class ConvertingIterable<S, T> implements Iterable<T>
	{
		private final Iterable<? extends S> _iterable;
		private final Converter<S, T> _converter;
		
		private ConvertingIterable(Iterable<? extends S> iterable, Converter<S, T> converter)
		{
			_iterable = iterable;
			_converter = converter;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return convertingIterator(_iterable, _converter);
		}
	}
	
	private static final class ConvertingIterator<S, T> implements Iterator<T>
	{
		private final Iterator<? extends S> _iterator;
		private final Converter<S, T> _converter;
		
		private T _next;
		
		private ConvertingIterator(Iterable<? extends S> iterable, Converter<S, T> converter)
		{
			_iterator = iterable.iterator();
			_converter = converter;
			
			step();
		}
		
		@Override
		public boolean hasNext()
		{
			return _next != null;
		}
		
		@Override
		public T next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			final T next = _next;
			
			step();
			
			return next;
		}
		
		private void step()
		{
			while (_iterator.hasNext())
			{
				final S src = _iterator.next();
				
				if (src == null)
				{
					continue;
				}
				
				final T next = _converter.convert(src);
				
				if (next != null)
				{
					_next = next;
					return;
				}
			}
			
			_next = null;
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2);
	}
	
	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2, Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterable<>(iterable1, iterable2, iterable3);
	}
	
	public static <T> Iterable<T> concatenatedIterable(Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterable<>(iterables);
	}
	
	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2);
	}
	
	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T> iterable1, Iterable<? extends T> iterable2, Iterable<? extends T> iterable3)
	{
		return new ConcatenatedIterator<>(iterable1, iterable2, iterable3);
	}
	
	public static <T> Iterator<T> concatenatedIterator(Iterable<? extends T>... iterables)
	{
		return new ConcatenatedIterator<>(iterables);
	}
	
	private static final class ConcatenatedIterable<E> implements Iterable<E>
	{
		private final Iterable<? extends E>[] _iterables;
		
		private ConcatenatedIterable(Iterable<? extends E>... iterables)
		{
			_iterables = iterables;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return concatenatedIterator(_iterables);
		}
	}
	
	private static final class ConcatenatedIterator<E> implements Iterator<E>
	{
		private final Iterable<? extends E>[] _iterables;
		
		private Iterator<? extends E> _iterator;
		private int _index = -1;
		
		private ConcatenatedIterator(Iterable<? extends E>... iterables)
		{
			_iterables = iterables;
			
			validateIterator();
		}
		
		@Override
		public boolean hasNext()
		{
			validateIterator();
			
			return (_iterator != null) && _iterator.hasNext();
		}
		
		@Override
		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			return _iterator.next();
		}
		
		private void validateIterator()
		{
			while ((_iterator == null) || !_iterator.hasNext())
			{
				_index++;
				
				if (_index >= _iterables.length)
				{
					return;
				}
				
				_iterator = _iterables[_index].iterator();
			}
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static <T> T[] toArray(Collection<? extends T> c, Class<T> clazz)
	{
		return c.toArray((T[]) Array.newInstance(clazz, c.size()));
	}
	
	public static <T> T[] toArraySynchronized(Collection<? extends T> c, Class<T> clazz)
	{
		synchronized (c)
		{
			return c.toArray((T[]) Array.newInstance(clazz, c.size()));
		}
	}
	
	/**
	 * Swaps the elements at the specified positions in the specified collection. (If the specified positions are equal, invoking this method leaves the collection unchanged.)
	 * @param array
	 */
	public static final void shuffle(final FastAccessList collection)
	{
		if (collection.isEmpty())
		{
			return;
		}
		
		int size = collection.size();
		for (int i = size; i > 1; i--)
		{
			swap(collection, i - 1, Rnd.nextInt(i));
		}
	}
	
	/**
	 * Swaps the two specified elements in the specified collection.
	 */
	private static void swap(FastAccessList collection, int i, int j)
	{
		Object tmpi = collection.getUnsafe(i);
		Object tmpj = collection.getUnsafe(j);
		collection.setUnsafeVoid(i, tmpj);
		collection.setUnsafeVoid(j, tmpi);
	}
	
	/**
	 * Копия сортировки {@link java.util.Arrays}
	 * @param collection
	 * @param c
	 */
	public static <T> void sort(final FastAccessList<T> collection, final Comparator<? super T> c)
	{
		if (collection.isEmpty())
		{
			return;
		}
		
		final T[] a = collection.toNativeArray();
		Arrays.sort(a, c);
		for (int j = 0; j < a.length; j++)
		{
			collection.setUnsafeVoid(j, a[j]);
		}
	}
	
	public static <T> void sort(final FastAccessList<T> collection)
	{
		if (collection.isEmpty())
		{
			return;
		}
		
		final T[] a = collection.toNativeArray();
		Arrays.sort(a);
		for (int j = 0; j < a.length; j++)
		{
			collection.setUnsafeVoid(j, a[j]);
		}
	}
	
	/**
	 * copy from {@link java.util.AbstractList}
	 * @param collection
	 * @param <E>
	 * @return hash
	 */
	public static <E> int hashCode(final Collection<E> collection)
	{
		int hashCode = 1;
		final Iterator<E> i = collection.iterator();
		while (i.hasNext())
		{
			final E obj = i.next();
			hashCode = (31 * hashCode) + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	
	public static <E> E getRandom(final List<E> collection)
	{
		if (collection.isEmpty())
		{
			return null;
		}
		
		return collection.get(Rnd.get(collection.size()));
	}
	
	public static <E> E getRandom(final FastAccessList<E> list)
	{
		if (list.isEmpty())
		{
			return null;
		}
		
		return list.getUnsafe(Rnd.get(list.size()));
	}
	
	public static int getRandom(final TIntList list)
	{
		if (list.isEmpty())
		{
			return 0;
		}
		
		return list.get(Rnd.get(list.size()));
	}
}
