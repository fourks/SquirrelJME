// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.build.projects;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class is used to provide access to all of the APIs which are available
 * for usage in SquirrelJME. An API defines and/or implements an interface
 * that provides a standard interface.
 *
 * @since 2016/12/04
 */
public final class APIManager
	extends __Namespace__
{
	/**
	 * Initializes the API manager.
	 *
	 * @param __pm The owning project manager.
	 * @param __p The path containing APIs.
	 * @throws IOException On read errors.
	 * @since 2016/12/04
	 */
	APIManager(ProjectManager __pm, Iterable<Path> __p)
		throws IOException
	{
		super(__pm, Arrays.<Path>asList(__p));
	}
}

