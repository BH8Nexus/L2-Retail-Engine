package l2r.commons.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;

import gnu.trove.set.hash.TLinkedHashSet;

/**
 * @author bloodshed <a href="http://l2nextgen.ru/">L2NextGen</a>
 * @email rkx.bloodshed@gmail.com
 * @date 17.02.2013
 * @time 4:19:25
 */
public final class ArraysUtil
{
	public static final Pattern DEFAULT_DELIMITER = Pattern.compile("[\\s,;]+");
	
	private ArraysUtil()
	{
	}
	
	/**
	 * Генерирует список из значений от <tt>start</tt>(включительно) до <tt>end</tt>(включительно) с шагом 1
	 */
	public static final int[] getIntArrayRange(final int start, final int end)
	{
		return getIntArrayRange(start, end, 1);
	}
	
	/**
	 * Генерирует список из значений от <tt>start</tt>(включительно) до <tt>end</tt>(включительно) с <tt>шагом step</tt>
	 */
	public static final int[] getIntArrayRange(final int start, final int end, final int step)
	{
		final int length = (int) Math.floor(((double) (end - start) / (double) step) + 1.);
		final int[] array = new int[length];
		for (int i = 0; i < length; i++)
		{
			final int element = i == 0 ? start : array[i - 1] + step;
			array[i] = element;
		}
		
		return array;
	}
	
	public static final String[] split(final String source, final Pattern delimiter)
	{
		if (source == null)
		{
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		return delimiter.split(source);
	}
	
	public static final String[] split(final String source)
	{
		return split(source, DEFAULT_DELIMITER);
	}
	
	public static final int[] toIntArray(final String string, final String delimiter)
	{
		if (string.isEmpty())
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		
		final String[] strings = string.split(delimiter);
		if (strings.length <= 0)
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		
		final int[] ints = new int[strings.length];
		for (int i = 0; i < strings.length; i++)
		{
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}
	
	public static final int[] toIntArray(final String s)
	{
		if (s.isEmpty())
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
		final String[] values = split(s);
		final int[] ret = new int[values.length];
		for (int i = 0; i < values.length; i++)
		{
			ret[i] = Integer.parseInt(values[i]);
		}
		return ret;
	}
	
	public static final float[] toFloatArray(final String string, final String delimiter)
	{
		final String[] strings = string.split(delimiter);
		if (strings.length <= 0)
		{
			return new float[0];
		}
		
		final float[] floats = new float[strings.length];
		for (int i = 0; i < strings.length; i++)
		{
			floats[i] = Float.parseFloat(strings[i]);
		}
		return floats;
	}
	
	public static final float[] toFloatArray(final String s)
	{
		if (s.isEmpty())
		{
			return ArrayUtils.EMPTY_FLOAT_ARRAY;
		}
		final String[] values = split(s);
		final float[] ret = new float[values.length];
		for (int i = 0; i < values.length; i++)
		{
			ret[i] = Float.parseFloat(values[i]);
		}
		return ret;
	}
	
	public static final double[] toDoubleArray(final String string, final String delimiter)
	{
		if (string.isEmpty())
		{
			return ArrayUtils.EMPTY_DOUBLE_ARRAY;
		}
		
		final String[] strings = string.split(delimiter);
		if (strings.length <= 0)
		{
			return new double[0];
		}
		
		final double[] ints = new double[strings.length];
		for (int i = 0; i < strings.length; i++)
		{
			ints[i] = Double.parseDouble(strings[i]);
		}
		return ints;
	}
	
	public static final double[] toDoubleArray(final String s)
	{
		if (s.isEmpty())
		{
			return ArrayUtils.EMPTY_DOUBLE_ARRAY;
		}
		final String[] values = split(s);
		final double[] ret = new double[values.length];
		for (int i = 0; i < values.length; i++)
		{
			ret[i] = Double.parseDouble(values[i]);
		}
		return ret;
	}
	
	public static final long[] toLongArray(final String s)
	{
		if (s.isEmpty())
		{
			return ArrayUtils.EMPTY_LONG_ARRAY;
		}
		final String[] values = split(s);
		final long[] ret = new long[values.length];
		for (int i = 0; i < values.length; i++)
		{
			ret[i] = Long.parseLong(values[i]);
		}
		return ret;
	}
	
	public static final String[] toStringArray(final String s)
	{
		if (s.isEmpty())
		{
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		final String[] values = split(s);
		final String[] ret = new String[values.length];
		for (int i = 0; i < values.length; i++)
		{
			ret[i] = values[i].trim();
		}
		return ret;
	}
	
	/**
	 * Swaps the elements at the specified positions in the specified array. (If the specified positions are equal, invoking this method leaves the array unchanged.)
	 * @param array
	 */
	public static final void shuffle(final Object[] array)
	{
		int size = array.length;
		// Shuffle array
		for (int i = size; i > 1; i--)
		{
			swap(array, i - 1, Rnd.nextInt(i));
		}
	}
	
	/**
	 * Swaps the two specified elements in the specified array.
	 */
	private static void swap(Object[] arr, int i, int j)
	{
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}
	
	/**
	 * Check if index is in valid range of array, if so return array value
	 * @param array
	 * @param index
	 * @return array element or null, if index out of range
	 */
	public static <T> T getValid(T[] array, int index, T def)
	{
		if (array == null)
		{
			return def;
		}
		if ((index < 0) || (array.length <= index))
		{
			return def;
		}
		return array[index];
	}
	
	public static String deepToString(final Object[] a)
	{
		if (a == null)
		{
			return "null";
		}
		
		int bufLen = 20 * a.length;
		if ((a.length != 0) && (bufLen <= 0))
		{
			bufLen = Integer.MAX_VALUE;
		}
		final StringBuilder buf = new StringBuilder(bufLen);
		deepToString(a, buf, new TLinkedHashSet<Object[]>());
		return buf.toString();
	}
	
	private static void deepToString(final Object[] a, final StringBuilder buf, final Set<Object[]> dejaVu)
	{
		if (a == null)
		{
			buf.append("null");
			return;
		}
		dejaVu.add(a);
		for (int i = 0; i < a.length; i++)
		{
			if (i != 0)
			{
				buf.append(", ");
			}
			
			final Object element = a[i];
			if (element == null)
			{
				buf.append("null");
			}
			else
			{
				final Class<?> eClass = element.getClass();
				if (eClass.isArray())
				{
					if (eClass == byte[].class)
					{
						buf.append(Arrays.toString((byte[]) element));
					}
					else if (eClass == short[].class)
					{
						buf.append(Arrays.toString((short[]) element));
					}
					else if (eClass == int[].class)
					{
						buf.append(Arrays.toString((int[]) element));
					}
					else if (eClass == long[].class)
					{
						buf.append(Arrays.toString((long[]) element));
					}
					else if (eClass == char[].class)
					{
						buf.append(Arrays.toString((char[]) element));
					}
					else if (eClass == float[].class)
					{
						buf.append(Arrays.toString((float[]) element));
					}
					else if (eClass == double[].class)
					{
						buf.append(Arrays.toString((double[]) element));
					}
					else if (eClass == boolean[].class)
					{
						buf.append(Arrays.toString((boolean[]) element));
					}
					else if (dejaVu.contains(element))
					{
						buf.append("[...]");
					}
					else
					{
						deepToString((Object[]) element, buf, dejaVu);
					}
				}
				else
				{
					buf.append(element.toString());
				}
			}
		}
		dejaVu.remove(a);
	}
	
	private static <T extends Comparable<T>> void eqBrute(final T[] a, final int lo, final int hi)
	{
		if ((hi - lo) == 1)
		{
			if (a[hi].compareTo(a[lo]) < 0)
			{
				final T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		}
		else if ((hi - lo) == 2)
		{
			int pmin = a[lo].compareTo(a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = a[pmin].compareTo(a[lo + 2]) < 0 ? pmin : lo + 2;
			if (pmin != lo)
			{
				final T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			eqBrute(a, lo + 1, hi);
		}
		else if ((hi - lo) == 3)
		{
			int pmin = a[lo].compareTo(a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = a[pmin].compareTo(a[lo + 2]) < 0 ? pmin : lo + 2;
			pmin = a[pmin].compareTo(a[lo + 3]) < 0 ? pmin : lo + 3;
			if (pmin != lo)
			{
				final T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			int pmax = a[hi].compareTo(a[hi - 1]) > 0 ? hi : hi - 1;
			pmax = a[pmax].compareTo(a[hi - 2]) > 0 ? pmax : hi - 2;
			if (pmax != hi)
			{
				final T e = a[hi];
				a[hi] = a[pmax];
				a[pmax] = e;
			}
			eqBrute(a, lo + 1, hi - 1);
		}
	}
	
	private static <T extends Comparable<T>> void eqSort(final T[] a, final int lo0, final int hi0)
	{
		int lo = lo0;
		int hi = hi0;
		if ((hi - lo) <= 3)
		{
			eqBrute(a, lo, hi);
			return;
		} /* * Pick a pivot and move it out of the way */
		final T pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;
		while (lo < hi)
		{ /* * Search forward from a[lo] until an element is found that * is greater than the pivot or lo >= hi */
			while ((a[lo].compareTo(pivot) <= 0) && (lo < hi))
			{
				lo++;
			}
			while ((pivot.compareTo(a[hi]) <= 0) && (lo < hi))
			{
				hi--;
			}
			if (lo < hi)
			{
				final T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		} /* * Put the median in the "center" of the list */
		a[hi0] = a[hi];
		a[hi] = pivot; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
		eqSort(a, lo0, lo - 1);
		eqSort(a, hi + 1, hi0);
	}
	
	/**
	 * An enhanced quick sort
	 * @author Jim Boritz
	 */
	public static <T extends Comparable<T>> void eqSort(final T[] a)
	{
		eqSort(a, 0, a.length - 1);
	}
	
	private static <T> void eqBrute(final T[] a, final int lo, final int hi, final Comparator<T> c)
	{
		if ((hi - lo) == 1)
		{
			if (c.compare(a[hi], a[lo]) < 0)
			{
				final T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		}
		else if ((hi - lo) == 2)
		{
			int pmin = c.compare(a[lo], a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = c.compare(a[pmin], a[lo + 2]) < 0 ? pmin : lo + 2;
			if (pmin != lo)
			{
				final T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			eqBrute(a, lo + 1, hi, c);
		}
		else if ((hi - lo) == 3)
		{
			int pmin = c.compare(a[lo], a[lo + 1]) < 0 ? lo : lo + 1;
			pmin = c.compare(a[pmin], a[lo + 2]) < 0 ? pmin : lo + 2;
			pmin = c.compare(a[pmin], a[lo + 3]) < 0 ? pmin : lo + 3;
			if (pmin != lo)
			{
				final T e = a[lo];
				a[lo] = a[pmin];
				a[pmin] = e;
			}
			int pmax = c.compare(a[hi], a[hi - 1]) > 0 ? hi : hi - 1;
			pmax = c.compare(a[pmax], a[hi - 2]) > 0 ? pmax : hi - 2;
			if (pmax != hi)
			{
				final T e = a[hi];
				a[hi] = a[pmax];
				a[pmax] = e;
			}
			eqBrute(a, lo + 1, hi - 1, c);
		}
	}
	
	private static <T> void eqSort(final T[] a, final int lo0, final int hi0, final Comparator<T> c)
	{
		int lo = lo0;
		int hi = hi0;
		if ((hi - lo) <= 3)
		{
			eqBrute(a, lo, hi, c);
			return;
		} /* * Pick a pivot and move it out of the way */
		final T pivot = a[(lo + hi) / 2];
		a[(lo + hi) / 2] = a[hi];
		a[hi] = pivot;
		while (lo < hi)
		{ /* * Search forward from a[lo] until an element is found that * is greater than the pivot or lo >= hi */
			while ((c.compare(a[lo], pivot) <= 0) && (lo < hi))
			{
				lo++;
			}
			while ((c.compare(pivot, a[hi]) <= 0) && (lo < hi))
			{
				hi--;
			}
			if (lo < hi)
			{
				final T e = a[lo];
				a[lo] = a[hi];
				a[hi] = e;
			}
		} /* * Put the median in the "center" of the list */
		a[hi0] = a[hi];
		a[hi] = pivot; /* * Recursive calls, elements a[lo0] to a[lo-1] are less than or * equal to pivot, elements a[hi+1] to a[hi0] are greater than * pivot. */
		eqSort(a, lo0, lo - 1, c);
		eqSort(a, hi + 1, hi0, c);
	}
	
	/**
	 * An enhanced quick sort
	 * @author Jim Boritz
	 */
	public static <T> void eqSort(final T[] a, final Comparator<T> c)
	{
		eqSort(a, 0, a.length - 1, c);
	}
	
	/**
	 * Enlarge and add element to array
	 * @param array
	 * @param element
	 * @return new array with element
	 */
	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	public static <T> T[] add(final T[] array, final T element)
	{
		final Class type = array != null ? array.getClass().getComponentType() : element != null ? element.getClass() : Object.class;
		final T[] newArray = (T[]) copyArrayGrow(array, type);
		newArray[newArray.length - 1] = element;
		return newArray;
	}
	
	/**
	 * Trim and remove element from array
	 * @param array
	 * @param value
	 * @return new array without element, if it present in array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] remove(final T[] array, final T value)
	{
		if (array == null)
		{
			return null;
		}
		
		final int index = org.apache.commons.lang3.ArrayUtils.indexOf(array, value);
		if (index == -1)
		{
			return array;
		}
		
		final int length = array.length;
		
		final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
		System.arraycopy(array, 0, newArray, 0, index);
		if (index < (length - 1))
		{
			System.arraycopy(array, index + 1, newArray, index, length - index - 1);
		}
		
		return newArray;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T[] copyArrayGrow(final T[] array, final Class<? extends T> type)
	{
		if (array != null)
		{
			final int arrayLength = Array.getLength(array);
			final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
			System.arraycopy(array, 0, newArray, 0, arrayLength);
			return newArray;
		}
		return (T[]) Array.newInstance(type, 1);
	}
	
	public static int countNull(Object[] array)
	{
		if (array == null)
		{
			return 0;
		}
		
		int nullCount = 0;
		
		for (Object obj : array)
		{
			if (obj == null)
			{
				nullCount++;
			}
		}
		
		return nullCount;
	}
	
	public static int countNotNull(Object[] array)
	{
		return array == null ? 0 : array.length - countNull(array);
	}
	
	/**
	 * @param <T>
	 * @param array to remove null elements from
	 * @return an array without null elements - can be the same, if the original contains no null elements
	 * @throws NullPointerException if array is null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] compact(T[] array)
	{
		final int newSize = countNotNull(array);
		
		if (array.length == newSize)
		{
			return array;
		}
		
		final T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), newSize);
		
		int index = 0;
		
		for (T t : array)
		{
			if (t != null)
			{
				result[index++] = t;
			}
		}
		
		return result;
	}
	
	/**
	 * @param <T>
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside a foreach loop, and supports addition
	 */
	public static <T> List<T> asForeachSafeList(T... array)
	{
		return asForeachSafeList(true, array);
	}
	
	/**
	 * @param <T>
	 * @param allowAddition determines that list MUST support add operation or not
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside a foreach loop, and supports addition if required
	 */
	public static <T> List<T> asForeachSafeList(boolean allowAddition, T... array)
	{
		final int newSize = countNotNull(array);
		
		if ((newSize == 0) && !allowAddition)
		{
			return CollectionUtils.emptyList();
		}
		
		if (newSize <= 8)
		{
			return new CopyOnWriteArrayList<>(compact(array));
		}
		
		final List<T> result = new GArray<>(newSize);
		
		for (T t : array)
		{
			if (t != null)
			{
				result.add(t);
			}
		}
		
		return result;
	}
	
	public static <T> Iterable<T> iterable(Object[] array)
	{
		return new NullFreeArrayIterable<>(array);
	}
	
	public static <T> Iterable<T> iterable(Object[] array, boolean allowNull)
	{
		if (allowNull)
		{
			return new ArrayIterable<>(array);
		}
		return new NullFreeArrayIterable<>(array);
	}
	
	private static class ArrayIterable<T> implements Iterable<T>
	{
		protected final Object[] _array;
		
		private ArrayIterable(Object[] array)
		{
			_array = array;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new ArrayIterator<>(_array);
		}
	}
	
	private static final class NullFreeArrayIterable<T> extends ArrayIterable<T>
	{
		private NullFreeArrayIterable(Object[] array)
		{
			super(array);
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new NullFreeArrayIterator<>(_array);
		}
	}
	
	public static <T> Iterator<T> iterator(Object[] array)
	{
		return new NullFreeArrayIterator<>(array);
	}
	
	public static <T> Iterator<T> iterator(Object[] array, boolean allowNull)
	{
		if (allowNull)
		{
			return new ArrayIterator<>(array);
		}
		return new NullFreeArrayIterator<>(array);
	}
	
	private static class ArrayIterator<T> implements Iterator<T>
	{
		private final Object[] _array;
		
		private int _index;
		
		private ArrayIterator(Object[] array)
		{
			_array = array;
		}
		
		/**
		 * @param obj
		 * @return boolean
		 */
		boolean allowElement(Object obj)
		{
			return true;
		}
		
		@Override
		public final boolean hasNext()
		{
			for (;;)
			{
				if (_array.length <= _index)
				{
					return false;
				}
				
				if (allowElement(_array[_index]))
				{
					return true;
				}
				
				_index++;
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final T next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}
			
			return (T) _array[_index++];
		}
		
		@Override
		public final void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static final class NullFreeArrayIterator<T> extends ArrayIterator<T>
	{
		private NullFreeArrayIterator(Object[] array)
		{
			super(array);
		}
		
		@Override
		boolean allowElement(Object obj)
		{
			return obj != null;
		}
	}
	
	/**
	 * Finds free (non-occupied) index in an array in such a way, that amortized time to allocate an index is O(1). This implementations looks for an index {@code i} such that {@code a[i] == null} starting from the given {@code lastFreeIndex} and then cycles to the beginning of array where it starts
	 * from {@code minIndex}. On cycle it counts the number of free indices in array. If less than a quarter of indices a free then {@code a.length} is returned to indicate that array shall be reallocated to a larger size.
	 * @param a the array.
	 * @param lastFoundIndex last result of this method. On the first invocation is must be equal to {@code minIndex}.
	 * @param minIndex Minimal allowed index.
	 * @return an index {@code i}, such that {@code i >= minIndex && a[i] == null || i == a.length}. The result of {@code a.length} indicates that array shall be reallocated with {@link #grow(Object[], int)} method.
	 */
	public static int findFreeIndex(Object[] a, int lastFoundIndex, int minIndex)
	{
		for (int i = lastFoundIndex; i < a.length; i++)
		{
			if (a[i] == null)
			{
				return i;
			}
		}
		int freeCount = 0;
		int firstFreeIndex = 0;
		for (int i = lastFoundIndex; --i >= minIndex;)
		{
			if (a[i] == null)
			{
				freeCount++;
				firstFreeIndex = i;
			}
		}
		return freeCount <= (a.length >> 2) ? a.length : firstFreeIndex;
	}
	
	// Byte methods
	
	public static final int getC(final byte[] array, final int pos)
	{
		return array[pos] & 0x000000FF;
	}
	
	public static final int getD(final byte[] array, final int pos)
	{
		return (array[pos] & 0x000000FF) | ((array[pos + 1] << 8) & 0x0000FF00) | ((array[pos + 2] << 16) & 0x00FF0000) | ((array[pos + 3] << 24) & 0xFF000000);
	}
	
	public static final double getF(final byte[] array, final int pos)
	{
		return Double.longBitsToDouble(getQ(array, pos));
	}
	
	public static final int getH(final byte[] array, final int pos)
	{
		return (array[pos] & 0x000000FF) | ((array[pos + 1] << 8) & 0x0000FF00);
	}
	
	public static final long getQ(final byte[] array, final int pos)
	{
		return (getD(array, pos) & 0xFFFFFFFFL) | ((getD(array, pos + 4) & 0xFFFFFFFFL) << 32);
	}
	
	public static final void putC(final byte[] array, final int pos, final int value)
	{
		array[pos] = (byte) (value & 0x000000FF);
	}
	
	public static final void putD(final byte[] array, final int pos, final int value)
	{
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) ((value >> 8) & 0x000000FF);
		array[pos + 2] = (byte) ((value >> 16) & 0x000000FF);
		array[pos + 3] = (byte) ((value >> 24) & 0x000000FF);
	}
	
	public static final void putF(final byte[] array, final int pos, final double value)
	{
		putQ(array, pos, Double.doubleToRawLongBits(value));
	}
	
	public static final void putH(final byte[] array, final int pos, final int value)
	{
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) ((value >> 8) & 0x000000FF);
	}
	
	public static final void putQ(final byte[] array, final int pos, final long value)
	{
		array[pos] = (byte) (value & 0x000000FF);
		array[pos + 1] = (byte) ((value >> 8) & 0x000000FF);
		array[pos + 2] = (byte) ((value >> 16) & 0x000000FF);
		array[pos + 3] = (byte) ((value >> 24) & 0x000000FF);
		array[pos + 4] = (byte) ((value >> 32) & 0x000000FF);
		array[pos + 5] = (byte) ((value >> 40) & 0x000000FF);
		array[pos + 6] = (byte) ((value >> 48) & 0x000000FF);
		array[pos + 7] = (byte) ((value >> 56) & 0x000000FF);
	}
}
