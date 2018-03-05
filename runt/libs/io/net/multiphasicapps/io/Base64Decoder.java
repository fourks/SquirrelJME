// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

/**
 * This decodes the base64 character set, ignoring invalid characters, and
 * provides the binary data for the input. If the padding character is reached
 * or if the input stream runs out of characters then EOF is triggered.
 *
 * @since 2018/03/05
 */
public final class Base64Decoder
	extends InputStream
{
	/** The source reader. */
	protected final Reader in;
	
	/** Ignore padding characters. */
	protected final boolean ignorepadding;
	
	/** The alphabet to use for decoding. */
	private final char[] _alphabet;
	
	/**
	 * Initializes the decoder using the default alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The pre-defined character set to use for the alphabet.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, Base64Alphabet __chars)
		throws NullPointerException
	{
		this(__in, __chars._alphabet, false);
	}
	
	/**
	 * Initializes the decoder using the specified custom alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The characters to use for the alphabet.
	 * @throws IllegalArgumentException If the alphabet is of the incorrect
	 * size.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, String __chars)
		throws IllegalArgumentException, NullPointerException
	{
		this(__in, __chars.toCharArray(), false);
	}
	
	/**
	 * Initializes the decoder using the specified custom alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The characters to use for the alphabet.
	 * @throws IllegalArgumentException If the alphabet is of the incorrect
	 * size.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, char[] __chars)
		throws IllegalArgumentException, NullPointerException
	{
		this(__in, __chars, false);
	}
	
	/**
	 * Initializes the decoder using the default alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The pre-defined character set to use for the alphabet.
	 * @param __ip Ignore padding characters and do not treat them as the end
	 * of the stream.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, Base64Alphabet __chars, boolean __ip)
		throws NullPointerException
	{
		this(__in, __chars._alphabet, __ip);
	}
	
	/**
	 * Initializes the decoder using the specified custom alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The characters to use for the alphabet.
	 * @param __ip Ignore padding characters and do not treat them as the end
	 * of the stream.
	 * @throws IllegalArgumentException If the alphabet is of the incorrect
	 * size.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, String __chars, boolean __ip)
		throws IllegalArgumentException, NullPointerException
	{
		this(__in, __chars.toCharArray(), __ip);
	}
	
	/**
	 * Initializes the decoder using the specified custom alphabet.
	 *
	 * @param __in The input set of characters.
	 * @param __chars The characters to use for the alphabet.
	 * @param __ip Ignore padding characters and do not treat them as the end
	 * of the stream.
	 * @throws IllegalArgumentException If the alphabet is of the incorrect
	 * size.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/03/05
	 */
	public Base64Decoder(Reader __in, char[] __chars, boolean __ip)
		throws IllegalArgumentException, NullPointerException
	{
		if (__in == null || __chars == null)
			throw new NullPointerException("NARG");
		
		// {@squirreljme.erorr BD0g The alphabet to use for the base64
		// decoder must be 64 characters plus one padding character.
		// (The character count)}
		int n;
		if ((n = __chars.length) != 65)
			throw new IllegalArgumentException(String.format("BD0g %d", n));
		
		this.in = __in;
		this.ignorepadding = __ip;
		this._alphabet = __chars.clone();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/03/05
	 */
	@Override
	public final void close()
		throws IOException
	{
		this.in.close();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/03/05
	 */
	@Override
	public final int read()
		throws IOException
	{
		byte[] next = new byte[1];
		for (;;)
		{
			int rc = this.read(next, 0, 1);
			if (rc < 0)
				return -1;
			else if (rc == 0)
				continue;
			else if (rc == 1)
				return (next[0] & 0xFF);
			else
				throw new RuntimeException("OOPS");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/03/05
	 */
	@Override
	public final int read(byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, IOException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		if (__o < 0 || __l < 0 || (__o + __l) > __b.length)
			throw new IndexOutOfBoundsException("IOOB");
		
		throw new todo.TODO();
	}
}

