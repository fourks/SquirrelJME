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
 * System defined control keys.
 *
 * @since 2017/12/31
 */
public interface SystemProgramControlKey
{
	/** Download URL. */
	public static final String DOWNLOAD_URL =
		"X-SquirrelJME-Download-URL";
	
	/** Is the program installed? */
	public static final String IS_INSTALLED =
		"X-SquirrelJME-Is-Installed";
	
	/** Is the program trusted? */
	public static final String IS_TRUSTED =
		"X-SquirrelJME-Is-Trusted";
	
	/** Prefix for state flags. */
	public static final String STATE_FLAG_PREFIX =
		"X-SquirrelJME-State-Flag-";
	
	/** Dependency prefix. */
	public static final String DEPENDENCY_PREFIX =
		"X-SquirrelJME-Dependency-";
}

