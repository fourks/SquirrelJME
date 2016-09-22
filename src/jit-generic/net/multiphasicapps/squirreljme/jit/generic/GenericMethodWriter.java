// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit.generic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.multiphasicapps.io.data.DataEndianess;
import net.multiphasicapps.io.data.ExtendedDataOutputStream;
import net.multiphasicapps.squirreljme.classformat.MethodInvokeType;
import net.multiphasicapps.squirreljme.classformat.MethodLinkage;
import net.multiphasicapps.squirreljme.classformat.MethodReference;
import net.multiphasicapps.squirreljme.classformat.StackMapType;
import net.multiphasicapps.squirreljme.nativecode.base.NativeEndianess;
import net.multiphasicapps.squirreljme.jit.base.JITException;
import net.multiphasicapps.squirreljme.jit.base.JITTriplet;
import net.multiphasicapps.squirreljme.jit.JITMethodWriter;
import net.multiphasicapps.squirreljme.jit.JITOutputConfig;
import net.multiphasicapps.squirreljme.nativecode.NativeABI;

/**
 * This is a writer which manages details for methods along the potential for
 * it to contain byte code.
 *
 * @since 2016/08/19
 */
public final class GenericMethodWriter
	implements JITMethodWriter
{
	/** The used configuration. */
	protected final JITOutputConfig.Immutable config;	
	
	/** The stream to write to. */
	protected final ExtendedDataOutputStream output;
	
	/** The output endianess. */
	protected final NativeEndianess endianess;
	
	/** The bit level of the CPU. */
	protected final int wordsize;
	
	/** The register allocator to use. */
	protected final GenericAllocator allocator;
	
	/** The ABI used on the target system. */
	protected final NativeABI abi;
	
	/** State of the stack for each Java operation. */
	protected final Map<Integer, GenericAllocatorState> jopstates =
		new LinkedHashMap<>();
	
	/** This method reference. */
	protected final MethodReference thisref;
	
	/** Valid jump targets. */
	private volatile int[] _jumptargets;
	
	/**
	 * Initializes the generic method writer.
	 *
	 * @param __conf The used configuration.
	 * @param __os The stream to write to.
	 * @param __thisref This method reference.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/08/21
	 */
	public GenericMethodWriter(JITOutputConfig.Immutable __conf,
		OutputStream __os, MethodReference __thisref)
		throws NullPointerException
	{
		// Check
		if (__os == null || __thisref == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.thisref = __thisref;
		
		// Wrap
		this.config = __conf;
		ExtendedDataOutputStream dos = new ExtendedDataOutputStream(__os);
		this.output = dos;
		
		// Set endianess
		NativeEndianess endianess;
		JITTriplet triplet = __conf.triplet();
		switch ((endianess = triplet.endianess()))
		{
				// Big endian
			case BIG:
				dos.setEndianess(DataEndianess.BIG);
				break;
				
				// Little endian
			case LITTLE:
				dos.setEndianess(DataEndianess.LITTLE);
				break;
			
				// Unknown
			default:
				throw new RuntimeException("OOPS");
		}
		this.endianess = endianess;
		
		// CPU bits
		this.wordsize = triplet.bits();
		
		// {@squirreljme.error BA0o Cannot initialize a register allocator
		// because no ABI setup has been specified.}
		GenericABI abi = __conf.<GenericABI>getObject(GenericABI.class);
		if (abi == null)
			throw new JITException("BA0o");
		
		// Create new allocator
		NativeABI nabi = abi.abi();
		this.abi = nabi;
		this.allocator = new GenericAllocator(__conf, nabi);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/09/04
	 */
	@Override
	public void atInstruction(int __code, int __pos)
		throws JITException
	{
		// If there is already a state for this position then it must be
		// restored. Jump forwards to unprocessed code will cause states to
		// exist.
		Map<Integer, GenericAllocatorState> jopstates = this.jopstates;
		GenericAllocatorState gas = jopstates.get(__pos);
		GenericAllocator allocator = this.allocator;
		if (gas != null)
			throw new Error("TODO");
		
		// Otherwise store it
		// If the current position is a jump target then record the state of
		// the allocator before the work is performed so that the state may
		// be transfered back for jump handling.
		// Also always add the implicit state on entry of a method
		else if (__pos == 0 ||
			Arrays.binarySearch(this._jumptargets, __pos) >= 0)
			jopstates.put(0, allocator.recordState());
		
		// Debug
		System.err.printf("DEBUG -- Op %d @ %d: %s%n", __code, __pos,
			allocator);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/08/19
	 */
	@Override
	public void close()
		throws JITException
	{
		// Close the output
		try
		{
			this.output.close();
		}
		
		// {@squirreljme.error BA06 Could not close the method.}
		catch (IOException e)
		{
			throw new JITException("BA06", e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/09/06
	 */
	@Override
	public void invoke(MethodInvokeType __type, int __pid,
		MethodReference __ref, StackMapType[] __st, int[] __sp,
		StackMapType __rvt, int __rv)
		throws JITException, NullPointerException
	{
		// Check
		if (__type == null || __ref == null || __st == null || __sp == null)
			throw new NullPointerException("NARG");
		
		// Setup linkage
		MethodLinkage gml = new MethodLinkage(this.thisref,
			__ref, __type);
		
		throw new Error("TODO");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/09/03
	 */
	@Override
	public final void jumpTargets(int[] __jt)
		throws JITException, NullPointerException
	{
		// Check
		if (__jt == null)
			throw new NullPointerException("NARG");
		
		// Store it
		this._jumptargets = __jt;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/08/29
	 */
	@Override
	public final void primeArguments(boolean __eh, StackMapType[] __t)
		throws JITException, NullPointerException
	{
		// Check
		if (__t == null)
			throw new NullPointerException("NARG");
		
		// Just send to the allocator
		GenericAllocator allocator = this.allocator;
		allocator.primeArguments(__eh, __t);
		
		// If there are exception handlers present then it will be required
		// to copy the initial argument registers to the CPU stack
		if (__eh)
			throw new Error("TODO");
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/09/03
	 */
	@Override
	public final void variableCounts(int __stack, int __locals)
		throws JITException
	{
		// Send it to the allocator
		this.allocator.variableCounts(__stack, __locals);
	}
}

