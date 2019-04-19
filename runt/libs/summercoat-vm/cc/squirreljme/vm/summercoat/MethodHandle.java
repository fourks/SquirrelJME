// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.summercoat;

/**
 * This interface represents a handle to a method, which is used to refer to
 * methods and such.
 *
 * @since 2019/01/10
 */
public interface MethodHandle
{
	/**
	 * Resolves the handle for the given context class (if there is one) and
	 * returns the handle to actually execute.
	 *
	 * @param __ctxcl The context class, this will be {@code null} if the
	 * invocation is static.
	 * @return The actual handle to execute.
	 * @since 2019/04/19
	 */
	public abstract StaticMethodHandle resolve(LoadedClass __ctxcl);
}

