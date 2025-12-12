package l2r.commons.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Полный аналог ArrayList, но намного быстрее удаляет. <br />
 * Вместо сдвига всего массива перемещает последний элемент списка на место удаленного. <br />
 * Побочный эффект - размер массива при удалении элемента не меняется, но можно использовать clear. <br />
 * <br />
 * Базовая версия.
 * @see l2n.commons.list.GCArray - конкуррентнобезопасная версия
 * @see l2n.commons.list.GSArray - синхронизированная версия
 * @see l2n.commons.list.GCSArray - конкуррентнобезопасная синхронизированная версия
 */
@SuppressWarnings("unchecked")
public class GArray<E> extends AbstractList<E> implements FastAccessList<E>
{
	private static final int L = 1 << 3;
	
	protected transient E[] _elementData;
	protected transient int _size = 0;
	
	public GArray(final int initialCapacity)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		_elementData = (E[]) new Object[initialCapacity];
	}
	
	public GArray()
	{
		this(L);
	}
	
	/**
	 * @param gArray
	 * @param fromIndex
	 * @param toIndex
	 */
	private GArray(final GArray<E> collection, final int fromIndex, final int toIndex)
	{
		if (fromIndex < 0)
		{
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		}
		if (toIndex > collection.size())
		{
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		}
		if (fromIndex > toIndex)
		{
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
		
		_size = toIndex - fromIndex;
		_elementData = (E[]) new Object[_size];
		if (_size > 0)
		{
			System.arraycopy(collection._elementData, fromIndex, _elementData, 0, _size);
		}
	}
	
	public int getCapacity()
	{
		return _elementData.length;
	}
	
	/**
	 * Расширить внутренний массив так, чтобы он смог разместить как минимум <b>newSize</b> элементов
	 * @param newSize минимальная размерность нового массива
	 */
	public void ensureCapacity(final int minCapacity)
	{
		final int oldCapacity = _elementData.length;
		if (minCapacity > oldCapacity)
		{
			int newCapacity = ((oldCapacity * 3) / 2) + 1;
			if (newCapacity < minCapacity)
			{
				newCapacity = minCapacity;
			}
			_elementData = Arrays.copyOf(_elementData, newCapacity);
		}
	}
	
	/**
	 * Сохраняет только те значения, выполнение над которомы процедура вернёт true. В конце испольнения массив элементов перестраивается.
	 * @param procedure
	 * @return
	 */
	public boolean retainValues(final INgObjectProcedure<E> procedure)
	{
		return retainValues(procedure, true);
	}
	
	public boolean retainValues(final INgObjectProcedure<E> procedure, final boolean compact)
	{
		if (_size < 1)
		{
			return false;
		}
		
		boolean modified = false;
		
		final int size = _size;
		for (int i = size; i-- > 0;)
		{
			if (!procedure.execute(_elementData[i]))
			{
				// переносим последний элемент массива на место удаляемого
				_elementData[i] = _elementData[_size - 1];
				// удаляем последний
				_elementData[--_size] = null;
				modified = true;
			}
		}
		
		// пересоздаём массив
		if (compact && modified)
		{
			_elementData = Arrays.copyOf(_elementData, _size);
		}
		
		return modified;
	}
	
	@Override
	public boolean forEach(final INgObjectProcedure<E> procedure)
	{
		for (int i = _size; i-- > 0;)
		{
			if (!procedure.execute(_elementData[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void forEach(final INgVoidProcedure<E> procedure)
	{
		for (int i = _size; i-- > 0;)
		{
			procedure.execute(_elementData[i]);
		}
	}
	
	@Override
	public int size()
	{
		return _size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return _size == 0;
	}
	
	@Override
	public <T> T[] toNativeArray()
	{
		final T[] arr = (T[]) new Object[_size];
		if (_size > 0)
		{
			System.arraycopy(_elementData, 0, arr, 0, _size);
		}
		return arr;
	}
	
	@Override
	public Object[] toArray()
	{
		final Object[] arr = new Object[_size];
		if (_size > 0)
		{
			System.arraycopy(_elementData, 0, arr, 0, _size);
		}
		return arr;
	}
	
	@Override
	public <T> T[] toArray(final T[] a)
	{
		final T[] arr = a.length >= _size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), _size);
		if (_size > 0)
		{
			System.arraycopy(_elementData, 0, arr, 0, _size);
		}
		if (arr.length > _size)
		{
			arr[_size] = null;
		}
		return arr;
	}
	
	/**
	 * Returns the value at the specified position in this collection.
	 */
	@Override
	public E get(final int index)
	{
		RangeCheck(index);
		return getUnsafe(index);
	}
	
	@Override
	public E getUnsafe(final int index)
	{
		return _elementData[index];
	}
	
	/**
	 * Returns the first value of this collection.
	 */
	public E getFirst()
	{
		return get(0);
	}
	
	/**
	 * Returns the last value of this collection.
	 */
	public E getLast()
	{
		return get(_size - 1);
	}
	
	@Override
	public boolean add(final E value)
	{
		ensureCapacity(_size + 1);
		addLastUnsafe(value);
		return true;
	}
	
	@Override
	public final void addLastUnsafe(final E value)
	{
		_elementData[_size++] = value;
	}
	
	/**
	 * Inserts the specified element at the specified position in this list. Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 * @throws IndexOutOfBoundsException {@inheritDoc}
	 */
	@Override
	public void add(final int index, final E element)
	{
		if ((index > _size) || (index < 0))
		{
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
		}
		
		ensureCapacity(_size + 1); // Increments modCount!!
		System.arraycopy(_elementData, index, _elementData, index + 1, _size - index);
		_elementData[index] = element;
		_size++;
	}
	
	@Override
	public boolean remove(final Object o)
	{
		if (_size == 0)
		{
			return false;
		}
		
		if (o == null)
		{
			for (int index = 0; index < _size; index++)
			{
				if (_elementData[index] == null)
				{
					fastRemove(index);
					return true;
				}
			}
		}
		else
		{
			for (int index = 0; index < _size; index++)
			{
				if (o.equals(_elementData[index]))
				{
					fastRemove(index);
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Private remove method that skips bounds checking and does not return the value removed.
	 */
	private void fastRemove(final int index)
	{
		final int numMoved = _size - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(_elementData, index + 1, _elementData, index, numMoved);
		}
		_elementData[--_size] = null; // Let gc do its work
	}
	
	@Override
	public E remove(final int index)
	{
		RangeCheck(index);
		
		final E oldValue = getUnsafe(index);
		removeUnsafeVoid(index);
		return oldValue;
	}
	
	public final void removeUnsafeVoid(final int index)
	{
		if (index < --_size)
		{
			System.arraycopy(_elementData, index + 1, _elementData, index, _size - index);
		}
		_elementData[_size] = null; // Let gc do its work
	}
	
	public E removeFirst()
	{
		return _size > 0 ? remove(0) : null;
	}
	
	public E removeLast()
	{
		if (_size > 0)
		{
			_size--;
			final E old = _elementData[_size];
			_elementData[_size] = null;
			return old;
		}
		return null;
	}
	
	@Override
	public E set(final int index, final E element)
	{
		RangeCheck(index);
		final E oldValue = _elementData[index];
		_elementData[index] = element;
		return oldValue;
	}
	
	@Override
	public final void setUnsafeVoid(final int index, final E value)
	{
		_elementData[index] = value;
	}
	
	@Override
	public int indexOf(final Object o)
	{
		if (o == null)
		{
			for (int i = 0; i < _size; i++)
			{
				if (_elementData[i] == null)
				{
					return i;
				}
			}
		}
		else
		{
			for (int i = 0; i < _size; i++)
			{
				if (o.equals(_elementData[i]))
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	@Override
	public boolean contains(final Object o)
	{
		if (o == null)
		{
			for (int i = 0; i < _size; i++)
			{
				if (_elementData[i] == null)
				{
					return true;
				}
			}
		}
		else
		{
			for (int i = 0; i < _size; i++)
			{
				if (o.equals(_elementData[i]))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		if ((c == null) || c.isEmpty())
		{
			return false;
		}
		
		boolean modified = false;
		final Iterator<? extends E> e = c.iterator();
		if (getCapacity() < (c.size() + _size))
		{
			while (e.hasNext())
			{
				if (add(e.next()))
				{
					modified = true;
				}
			}
		}
		else
		{
			modified = true;
			while (e.hasNext())
			{
				addLastUnsafe(e.next());
			}
		}
		return modified;
	}
	
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean modified = false;
		
		final int size = _size;
		for (int i = size; i-- > 0;)
		{
			if (c.contains(_elementData[i]))
			{
				// переносим последний элемент массива на место удаляемого
				_elementData[i] = _elementData[_size - 1];
				// удаляем последний
				_elementData[--_size] = null;
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		boolean modified = false;
		
		final int size = _size;
		for (int i = size; i-- > 0;)
		{
			if (!c.contains(_elementData[i]))
			{
				// переносим последний элемент массива на место удаляемого
				_elementData[i] = _elementData[_size - 1];
				// удаляем последний
				_elementData[--_size] = null;
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		for (int i = 0; i < _size; i++)
		{
			if (!contains(_elementData[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	private void RangeCheck(final int index)
	{
		if ((index >= _size) || (index < 0))
		{
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
		}
	}
	
	@Override
	public void clear()
	{
		if (_size == 0)
		{
			return;
		}
		
		final int oldSize = _size;
		_size = 0;
		if (oldSize > 1000)
		{
			_elementData = (E[]) new Object[L];
		}
		else
		{
			for (int i = 0; i < oldSize; i++)
			{
				_elementData[i] = null;
			}
		}
		_size = 0;
	}
	
	/**
	 * Осторожно, при таком очищении в массиве могут оставаться ссылки на обьекты, удерживающие эти обьекты в памяти!
	 */
	public void clearSize()
	{
		_size = 0;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new Itr();
	}
	
	private final class Itr implements Iterator<E>
	{
		int cursor = 0;
		
		@Override
		public final boolean hasNext()
		{
			return cursor < size();
		}
		
		@Override
		public final E next()
		{
			try
			{
				return _elementData[cursor++];
			}
			catch (final IndexOutOfBoundsException e)
			{
				throw new NoSuchElementException();
			}
		}
		
		@Override
		public final void remove()
		{
			try
			{
				fastRemove(--cursor);
			}
			catch (final IndexOutOfBoundsException e)
			{
				throw new ConcurrentModificationException();
			}
		}
	}
	
	/**
	 * Получить копию списка
	 * @return список, с параметрами и набором элементов текущего
	 */
	@Override
	public GArray<E> clone()
	{
		final GArray<E> clone = new GArray<>(0);
		if (_size > 0)
		{
			clone._size = _size;
			clone._elementData = (E[]) new Object[_elementData.length];
			System.arraycopy(_elementData, 0, clone._elementData, 0, _size);
		}
		return clone;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder bufer = new StringBuilder();
		for (int i = 0; i < _size; i++)
		{
			if (i != 0)
			{
				bufer.append(", ");
			}
			bufer.append(_elementData[i]);
		}
		return "<" + bufer + ">";
	}
	
	/**
	 * @param offset
	 * @param maxIndex
	 * @return
	 */
	@Override
	public final GArray<E> subList(final int fromIndex, final int toIndex)
	{
		return new GArray<>(this, fromIndex, toIndex);
	}
}
