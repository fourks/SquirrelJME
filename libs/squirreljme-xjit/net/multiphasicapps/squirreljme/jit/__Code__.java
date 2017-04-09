// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This handles the code attribute for a method.
 *
 * @since 2017/04/09
 */
class __Code__
{
	/** The exported method. */
	protected final ExportedMethod method;
	
	/** The constant pool. */
	protected final __Pool__ pool;
	
	/** The link table for imports. */
	protected final LinkTable linktable;
	
	/**
	 * Initializes the code decoder and perform some initial seeding work that
	 * is needed for the decoder.
	 *
	 * @param __em The output exported method.
	 * @param __is The input stream for the code.
	 * @param __pool The constant pool.
	 * @param __lt The link table.
	 * @throws IOException On read errors.
	 * @throws JITException On initial parsing errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/04/09
	 */
	__Code__(ExportedMethod __em, DataInputStream __is, __Pool__ __pool,
		LinkTable __lt)
		throws IOException, JITException, NullPointerException
	{
		// Check
		if (__em == null || __is == null || __pool == null || __lt == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.method = __em;
		this.pool = __pool;
		this.linktable = __lt;
		
		throw new todo.TODO();
	}
	
	/**
	 * Runs the code decoder.
	 *
	 * @throws IOException On read errors.
	 * @throws JITException If the code decodes improperly.
	 * @since 2017/04/09
	 */
	void __run()
		throws IOException, JITException
	{
		throw new todo.TODO();
	}
}

