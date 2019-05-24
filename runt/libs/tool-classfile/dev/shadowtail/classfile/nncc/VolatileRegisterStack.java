// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package dev.shadowtail.classfile.nncc;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a stack which is used to manage which volatile registers are used.
 *
 * @since 2019/05/24
 */
public final class VolatileRegisterStack
{
	/** The base register. */
	protected final int base;
	
	/** Registers being used. */
	private final List<Integer> _used =
		new ArrayList<>();
	
	/**
	 * Initializes the volatile stack.
	 *
	 * @param __b The base register.
	 * @since 2019/05/25
	 */
	public VolatileRegisterStack(int __b)
	{
		this.base = __b;
	}
	
	/**
	 * Clears all of the used volatile reisters.
	 *
	 * @since 2019/05/25
	 */
	public final void clear()
	{
		this._used.clear();
	}
}
