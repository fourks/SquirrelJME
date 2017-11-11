// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.runtime.cldc;

/**
 * This class contains the bridge to the internal SquirrelJME run-time
 * classes which provide special run-time functionality.
 *
 * @since 2017/11/10
 */
public final class RuntimeBridge
{
	/** Access to the clock. */
	public static final ClockFunctions CLOCK =
		(ClockFunctions)__get(RuntimeBridgeIndex.CLOCK);
	
	/** High memory access. */
	public static final HighMemoryFunctions HIGH_MEMORY =
		(HighMemoryFunctions)__get(RuntimeBridgeIndex.HIGH_MEMORY);
	
	/** Controls objects. */
	public static final ObjectFunctions OBJECT =
		(ObjectFunctions)__get(RuntimeBridgeIndex.OBJECT);
	
	/** Standard process pipes. */
	public static final PipeFunctions PIPE =
		(PipeFunctions)__get(RuntimeBridgeIndex.PIPE);
	
	/** Process access. */
	public static final ProcessFunctions PROCESS =
		(ProcessFunctions)__get(RuntimeBridgeIndex.PROCESS);
	
	/** Version information. */
	public static final VersionFunctions VERSION =
		(VersionFunctions)__get(RuntimeBridgeIndex.VERSION);
	
	/**
	 * Only contains static instances.
	 *
	 * @since 2017/11/10
	 */
	private RuntimeBridge()
	{
	}
	
	/**
	 * Returns the field for the given identifier.
	 *
	 * @param __id The identifier to get the field of.
	 * @return The object for the given field.
	 * @since 2017/11/10
	 */
	private static final Object __get(int __id)
	{
		switch (__id)
		{
			case RuntimeBridgeIndex.CLOCK:			return CLOCK;
			case RuntimeBridgeIndex.HIGH_MEMORY:	return HIGH_MEMORY;
			case RuntimeBridgeIndex.OBJECT:			return OBJECT;
			case RuntimeBridgeIndex.PIPE:			return PIPE;
			case RuntimeBridgeIndex.PROCESS:		return PROCESS;
			case RuntimeBridgeIndex.VERSION:		return VERSION;
			
			default:
				throw new RuntimeException("OOPS");
		}
	}
}

