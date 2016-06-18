// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.cldc;

/**
 * This class contains a special method which implements an in place merge
 * sort algorithm except that it works on a special int array of indices
 * instead of the original array. This permits
 *
 * @since 2016/06/18
 */
public final class SpecialIntArraySort
{
	/**
	 * Not used.
	 *
	 * @since 2016/06/18
	 */
	private SpecialIntArraySort()
	{
	}
	
	/**
	 * Sorts the given input indices using a special comparator of an unknown
	 * type. The result is a set of indices of the sorted input which is in the
	 * index order of the data that is sorted.
	 *
	 * @param <Q> Original data passed to the 
	 * @param __q The original data to sort.
	 * @param __from The inclusive starting index.
	 * @param __to The exclusive ending index.
	 * @param __comp The special comparator for the input data.
	 * @return The array of indices in their sorted order.
	 * @since 2016/06/18
	 */
	public static <Q> int[] sort(Q __q, int __from, int __to,
		SpecialComparator<Q> __comp)
	{
		throw new Error("TODO");
	}
	
	/**
	 * This is the the comparator which is used for comparing two values
	 * by their index.
	 *
	 * @param <Q> The original data, may be an array or collection.
	 * @since 2016/06/18
	 */
	public static interface SpecialComparator<Q>
	{
		/**
		 * Compares to values based on their index number.
		 *
		 * @param __q The potential data source.
		 * @param __a The first index.
		 * @param __b The second index.
		 * @return The comparison index.
		 * @since 2016/06/18
		 */
		public abstract int compare(Q __q, int __a, int __b);
	}
}

