// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.zip.streamreader;

import java.io.InputStream;
import java.io.IOException;
import net.multiphasicapps.io.crc32.CRC32DataSink;
import net.multiphasicapps.io.datasink.DataSinkOutputStream;
import net.multiphasicapps.io.dynhistin.DynamicHistoryInputStream;
import net.multiphasicapps.zip.ZipCompressionType;

/**
 * This provides an interface to interact with a single entry within a ZIP
 * stream.
 *
 * @since 2016/07/19
 */
public final class ZipStreamEntry
	extends InputStream
{
	/** The maximum size the data descriptor can be (if there is one). */
	private static final int _MAX_DESCRIPTOR_SIZE =
		16;
	
	/** The data lock. */
	protected final Object lock;
	
	/** The owning stream reader. */
	protected final ZipStreamReader zipreader;
	
	/** The name of the file. */
	protected final String filename;
	
	/** The compression method. */
	protected final ZipCompressionType method;
	
	/** The lower stream. */
	private final __LowerStream__ _lower;
	
	/** The higher stream. */
	private final __HigherStream__ _higher;
	
	/**
	 * Initializes the entry.
	 *
	 * @param __zsr The owning stream reader.
	 * @param __fn The name of the entry.
	 * @param __undef Is the size and CRC undefined?
	 * @param __crc The expected CRC.
	 * @param __comp The compressed size.
	 * @param __uncomp The uncompressed size.
	 * @param __method The compression method.
	 * @param __ins The input data source.
	 * @param __lock The data lock.
	 * @throws IOException If the decompressor could not be initialized.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/07/19
	 */
	ZipStreamEntry(ZipStreamReader __zsr, String __fn, boolean __undef,
		int __crc, int __comp, int __uncomp, ZipCompressionType __method,
		DynamicHistoryInputStream __ins, Object __lock)
		throws IOException, NullPointerException
	{
		// Check
		if (__zsr == null || __fn == null || __method == null ||
			__ins == null || __lock == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.lock = __lock;
		this.zipreader = __zsr;
		this.filename = __fn;
		this.method = __method;
		
		// Setup lower stream
		this._lower = new __LowerStream__(__ins, __undef, __comp);
		
		// Setup higher stream
		this._higher = new __HigherStream__(__method.inputStream(this._lower),
			__undef, __uncomp, __crc);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/07/19
	 */
	@Override
	public void close()
		throws IOException
	{
		// Lock
		synchronized (this.lock)
		{
			throw new Error("TODO");
		}
	}
	
	/**
	 * Returns the compression type that the entry uses.
	 *
	 * @return The compression type.
	 * @since 2016/07/19
	 */
	public ZipCompressionType compressionType()
	{
		return this.method;
	}
	
	/**
	 * Returns the name of the entry.
	 *
	 * @return The entry name.
	 * @since 2016/07/19
	 */
	public String name()
	{
		return this.filename;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/07/19
	 */
	@Override
	public int read()
		throws IOException
	{
		// Lock
		synchronized (this.lock)
		{
			throw new Error("TODO");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/07/19
	 */
	@Override
	public int read(byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, IOException, NullPointerException
	{
		// Check
		if (__b == null)
			throw new NullPointerException("NARG");
		int n = __b.length;
		if (__o < 0 || __l < 0 || (__o + __l) > n)
			throw new IndexOutOfBoundsException("IOOB");
		
		// Lock
		synchronized (this.lock)
		{
			throw new Error("TODO");
		}
	}
	
	/**
	 * This is the higher stream which exposes the uncompressed data and
	 * can calculate the CRC on it.
	 *
	 * @since 2016/07/19
	 */
	private final class __HigherStream__
		extends InputStream
	{
		/** Lock. */
		protected final Object lock =
			ZipStreamEntry.this.lock;
		
		/** The source stream. */
		protected final InputStream input;
	
		/** Is the CRC and size set undefined? */
		protected final boolean undefined;
	
		/** The CRC. */
		protected final int crc;
	
		/** The uncompressed size. */
		protected final int uncompressedsize;
		
		/**
		 * Initializes the higher stream.
		 *
		 * @param __ins The input stream for decompressed data.
		 * @param __undef If {@code true} then the uncompressed size is
		 * unknown.
		 * @param __uncomp The uncompressed size.
		 * @param __crc The CRC that the data must match.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/19
		 */
		private __HigherStream__(InputStream __ins, boolean __undef,
			int __uncomp, int __crc)
			throws NullPointerException
		{
			// Check
			if (__ins == null)
				throw new NullPointerException("NARG");
			
			// Set
			this.input = __ins;
			this.undefined = __undef;
			this.crc = (__undef ? 0 : __crc);
			this.uncompressedsize = (__undef ? -1 : __uncomp);
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final void close()
			throws IOException
		{
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final int read()
			throws IOException
		{
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final int read(byte[] __b, int __o, int __l)
			throws IndexOutOfBoundsException, IOException, NullPointerException
		{
			// Check
			if (__b == null)
				throw new NullPointerException("NARG");
			int n = __b.length;
			if (__o < 0 || __l < 0 || (__o + __l) > n)
				throw new IndexOutOfBoundsException("IOOB");
			
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
	}
	
	/**
	 * This is the lower stream which reads directory from the source data.
	 *
	 * @since 2016/07/19
	 */
	private final class __LowerStream__
		extends InputStream
	{
		/** Lock. */
		protected final Object lock =
			ZipStreamEntry.this.lock;
		
		/** The input source for bytes (and where to detect EOF). */
		protected final DynamicHistoryInputStream input;
		
		/** Is the CRC and size set undefined? */
		protected final boolean undefined;
	
		/** The compressed size. */
		protected final int compressedsize;
	
		/** If the size is undefined, then this is temporarily used. */
		private final byte[] _descbuf;
		
		/**
		 * Initializes the lower stream which detects the end of the actual
		 * uncompressed data.
		 *
		 * @param __ins The input stream to source bytes from.
		 * @param __undef If {@code true} then the compressed size is not
		 * known.
		 * @throws NullPointerException On null arguments.
		 * @since 2016/07/19
		 */
		private __LowerStream__(DynamicHistoryInputStream __ins,
			boolean __undef, int __comp)
			throws NullPointerException
		{
			// Check
			if (__ins == null)
				throw new NullPointerException("NARG");
			
			// Set
			this.input = __ins;
			this.undefined = __undef;
			this.compressedsize = (__undef ? -1 : __comp);
			
			// Setup descriptor array if undefined
			if (__undef)
				this._descbuf = new byte[_MAX_DESCRIPTOR_SIZE];
			else
				this._descbuf = null;
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final void close()
			throws IOException
		{
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final int read()
			throws IOException
		{
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2016/07/19
		 */
		@Override
		public final int read(byte[] __b, int __o, int __l)
			throws IndexOutOfBoundsException, IOException, NullPointerException
		{
			// Check
			if (__b == null)
				throw new NullPointerException("NARG");
			int n = __b.length;
			if (__o < 0 || __l < 0 || (__o + __l) > n)
				throw new IndexOutOfBoundsException("IOOB"); 
			
			// Lock
			synchronized (this.lock)
			{
				throw new Error("TODO");
			}
		}
	}
}

