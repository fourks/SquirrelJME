// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.tests.zip.streamwriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Random;
import net.multiphasicapps.tests.IndividualTest;
import net.multiphasicapps.tests.InvalidTestException;
import net.multiphasicapps.tests.TestGroupName;
import net.multiphasicapps.tests.TestFamily;
import net.multiphasicapps.tests.TestInvoker;
import net.multiphasicapps.zip.streamwriter.ZipStreamWriter;

/**
 * This tests that ZIP files are correctly streamed and that any output ZIP
 * file can be read by the already existing ZIP code.
 *
 * @since 2016/07/10
 */
public class ZipWriterStreamTest
	implements TestInvoker
{
	/**
	 * {@inheritDoc}
	 * @since 2016/07/10
	 */
	@Override
	public void runTest(IndividualTest __t)
		throws NullPointerException, Throwable
	{
		// Check
		if (__t == null)
			throw new NullPointerException();
		
		throw new Error("TODO");
	}
	/**
	 * {@inheritDoc}
	 * @since 2016/07/10
	 */
	@Override
	public TestFamily testFamily()
	{
		// Generate some random seeds
		Random rand = new Random(0x1989_07_06);
		
		return new TestFamily(
			"net.multiphasicapps.zip.streamwriter.ZipStreamWriter",
			Long.toString(rand.nextLong()),
			Long.toString(rand.nextLong()),
			Long.toString(rand.nextLong()));
	}
}

