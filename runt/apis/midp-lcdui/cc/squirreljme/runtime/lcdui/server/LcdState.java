// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.runtime.lcdui.server;

/**
 * This class contains the entire state of the LCD subsystem.
 *
 * @since 2018/03/17
 */
public final class LcdState
{
	/** The handler for requests to the LCD server. */
	protected final LcdRequestHandler requesthandler;
	
	/** The display manager. */
	protected final LcdDisplays displays;
	
	/** Displayables that exist. */
	protected final LcdDisplayables displayables;
	
	/** The callback manager. */
	protected final LcdCallbackManager callbacks =
		new LcdCallbackManager();
	
	/**
	 * Initializes the state storage for the LCDUI interface.
	 *
	 * @param __rh The handler for requests.
	 * @param __dm The display manager.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/15
	 */
	public LcdState(LcdRequestHandler __rh, LcdDisplays __dm,
		LcdDisplayables __ys)
		throws NullPointerException
	{
		if (__rh == null || __dm == null || __ys == null)
			throw new NullPointerException("NARG");
		
		this.requesthandler = __rh;
		this.displays = __dm;
		this.displayables = __ys;
	}
	
	/**
	 * Returns the callback manager.
	 *
	 * @return The callback manager.
	 * @since 2018/03/18
	 */
	public final LcdCallbackManager callbacks()
	{
		return this.callbacks;
	}
	
	/**
	 * Returns the displayable manager.
	 *
	 * @return The displayable manager.
	 * @since 2018/03/18
	 */
	public final LcdDisplayables displayables()
	{
		return this.displayables;
	}
	
	/**
	 * Returns the display manager.
	 *
	 * @return The display manager.
	 * @since 2018/03/18
	 */
	public final LcdDisplays displays()
	{
		return this.displays;
	}
	
	/**
	 * Returns the request handler.
	 *
	 * @return The request handler.
	 * @since 2018/03/18
	 */
	public final LcdRequestHandler requestHandler()
	{
		return this.requesthandler;
	}
}

