// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.interpreter;

import net.multiphasicapps.classprogram.CPComputeMachine;

/**
 * This is the computational machine which performs the actual logic operations
 * which are needed for the interpreter to actually perform work.
 *
 * @since 2016/04/08
 */
public class JVMComputeMachine
	implements CPComputeMachine<JVMThread, JVMVariable[]>
{
	/** The owning engine. */
	protected final JVMEngine engine;	
	
	/**
	 * Initializes the computing machine.
	 *
	 * @param __e The owning engine.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/04/08
	 */
	JVMComputeMachine(JVMEngine __e)
		throws NullPointerException
	{
		// Check
		if (__e == null)
			throw new NullPointerException("NARG");
		
		// Set
		engine = __e;
	}
}

