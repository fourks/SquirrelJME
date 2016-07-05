// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This contains the just in time compiler.
 *
 * A JIT may only be used once for any input class, however the output of the
 * JIT may be used multiple times so that multiple JARs may be combined into
 * a single binary.
 *
 * @since 2016/07/02
 */
public final class JIT
	implements Runnable
{
	/** The endian of the CPU. */
	protected final JITCPUEndian endian;
	
	/** The variant of the CPU. */
	protected final JITCPUVariant cpuvariant;
	
	/** One time lock. */
	private final Object _oncelock =
		new Object();
	
	/** The input source. */
	private final InputStream _input;
	
	/** The output of the JIT. */
	private final JITOutput _output;
	
	/** One time only. */
	private volatile boolean _once;
	
	/**
	 * Initializes the JIT.
	 *
	 * This performs basic sanity checks before the actual compilation of a JIT
	 * is performed.
	 *
	 * @param __fp The producer which generated this JIT.
	 * @apram __ns The namespace of the class.
	 * @param __ic The input stream of the class data.
	 * @param __jo The output of the JIT.
	 * @throws JITException If the class is not correctly formed.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/07/03
	 */
	public JIT(JITFactory.Producer __fp, String __ns, InputStream __ic,
		JITOutput __jo)
		throws JITException, NullPointerException
	{
		// Check
		if (__fp == null || __ns == null || __ic == null || __jo == null)
			throw new NullPointerException("NARG");
		
		// Get some details
		this.endian = __fp.endian();
		this.cpuvariant = __fp.architectureVariant();
		
		// Set
		this._input = __ic;
		this._output = __jo;
		
		// Modify this JIT for a given OS
		__fp.operatingSystemModifier().__modifyJIT(this);
	}
	
	/**
	 * Runs the JIT compilation.
	 *
	 * @since 2016/07/03
	 */
	@Override
	public final void run()
	{
		// Perform the JIT by running and parsing the class file.
		try
		{
			// Setup data stream
			DataInputStream dis = new DataInputStream(this._input);
			
			// Start decoding the class
			__ClassDecoder__ cd = new __ClassDecoder__(this, dis);
			cd.__decode();
			
			throw new Error("TODO");
		}
		
		// {@squirreljme.error ED02 Failed to read the class file.}
		catch (IOException e)
		{
			throw new JITException("ED02", e);
		}
	}
}

