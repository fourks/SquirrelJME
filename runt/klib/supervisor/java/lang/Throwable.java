// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package java.lang;

import cc.squirreljme.jvm.Assembly;
import cc.squirreljme.jvm.CallStackItem;
import cc.squirreljme.jvm.JVMFunction;
import cc.squirreljme.jvm.SystemCallIndex;
import cc.squirreljme.jvm.SystemCallError;
import cc.squirreljme.jvm.SystemCallIndex;

/**
 * This is the base class for all throwable types.
 *
 * @since 2019/05/25
 */
public class Throwable
{
	/** The message to use. */
	transient final String _message;
	
	/** The cause of this exception. */
	transient final Throwable _cause;
	
	/** The call trace. */
	transient final int[] _rawtrace;
	
	/**
	 * Initializes the exception with no message or cause.
	 *
	 * @since 2019/05/25
	 */
	public Throwable()
	{
		this._message = null;
		this._cause = null;
		
		// Get the trace
		this._rawtrace = Throwable.__trace();
	}
	
	/**
	 * Initializes the exception with the given message and no cause.
	 *
	 * @param __m The message.
	 * @since 2019/05/25
	 */
	public Throwable(String __m)
	{
		this._message = __m;
		this._cause = null;
		
		// Get the trace
		this._rawtrace = Throwable.__trace();
	}
	
	/**
	 * Initializes the exception with the given message and cause.
	 *
	 * @param __m The message.
	 * @param __t The cause.
	 * @since 2019/05/25
	 */
	public Throwable(String __m, Throwable __t)
	{
		this._message = __m + "hiya";
		this._cause = __t;
		
		// Get the trace
		this._rawtrace = Throwable.__trace();
	}
	
	/**
	 * Initializes the exception with the given cause and no message.
	 *
	 * @param __t The cause.
	 * @since 2019/05/25
	 */
	public Throwable(Throwable __t)
	{
		this._message = null;
		this._cause = __t;
		
		// Get the trace
		this._rawtrace = Throwable.__trace();
	}
	
	/**
	 * Returns the message.
	 *
	 * @return The message used.
	 * @since 2019/06/16
	 */
	public String getMessage()
	{
		return this._message;
	}
	
	/**
	 * Prints the raw stack trace.
	 *
	 * @since 2019/06/17
	 */
	public void printStackTrace()
	{
		// Print this and any causes!
		for (Throwable rover = this; rover != null; rover = rover._cause)
		{
			// Is this the main trace or a caused by?
			todo.DEBUG.note("%s Stack Trace: %s", (rover == this ?
				"Supervisor" : "Caused By"), rover._message);
			
			// Obtain the raw trace that was captured on construction
			int[] rawtrace = this._rawtrace;
			int rawn = rawtrace.length;
			
			// Print all the items in it
			StringBuilder sb = new StringBuilder();
			for (int base = 0; base < rawn; base += CallStackItem.NUM_ITEMS)
				try
				{
					// Print it out
					todo.DEBUG.note("    %s::%s:%s (%s:%d) A@%d J@%d/%d",
						JVMFunction.jvmLoadString(
							rawtrace[base + CallStackItem.CLASS_NAME]),
						JVMFunction.jvmLoadString(
							rawtrace[base + CallStackItem.METHOD_NAME]),
						JVMFunction.jvmLoadString(
							rawtrace[base + CallStackItem.METHOD_TYPE]),
						JVMFunction.jvmLoadString(
							rawtrace[base + CallStackItem.SOURCE_FILE]),
						rawtrace[base + CallStackItem.SOURCE_LINE],
						rawtrace[base + CallStackItem.PC_ADDRESS],
						rawtrace[base + CallStackItem.JAVA_PC_ADDRESS],
						rawtrace[base + CallStackItem.JAVA_OPERATION]);
				}
				
				// Error getting stack trace element
				catch (Throwable t)
				{
					todo.DEBUG.code('X', 'T', base);
				}
		}
	}
	
	/**
	 * Returns the call stack.
	 *
	 * @return The resulting call stack.
	 * @since 2019/06/17
	 */
	private static final int[] __trace()
	{
		// Get the call height, ignore if not supported!
		int callheight = Assembly.sysCallPV(SystemCallIndex.CALL_STACK_HEIGHT);
		if (callheight <= 0 || Assembly.sysCallPV(SystemCallIndex.ERROR_GET,
			SystemCallIndex.CALL_STACK_HEIGHT) != SystemCallError.NO_ERROR)
			return new int[0];
		
		// Remove the top-most frame because it will be this method
		callheight--;
		
		// Get the call parameters
		int[] rv = new int[callheight * CallStackItem.NUM_ITEMS];
		for (int z = 0, base = 0; z < callheight; z++,
			base += CallStackItem.NUM_ITEMS)
			for (int i = 0; i < CallStackItem.NUM_ITEMS; i++)
			{
				// Get parameter
				int vx = Assembly.sysCallPV(SystemCallIndex.CALL_STACK_ITEM,
					1 + z, i);
				
				// Nullify unknown or invalid parameters
				if (Assembly.sysCallPV(SystemCallIndex.ERROR_GET,
					SystemCallIndex.CALL_STACK_ITEM) !=
					SystemCallError.NO_ERROR)
					vx = 0;
				
				// Fill in
				rv[base + i] = vx;
			}
		
		// Return the raw parameters
		return rv;
	}
}

