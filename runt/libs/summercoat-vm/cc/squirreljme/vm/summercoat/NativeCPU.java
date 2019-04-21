// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.summercoat;

import dev.shadowtail.classfile.nncc.ArgumentFormat;
import dev.shadowtail.classfile.nncc.NativeCode;
import dev.shadowtail.classfile.nncc.NativeInstruction;
import dev.shadowtail.classfile.nncc.NativeInstructionType;
import java.util.LinkedList;
import java.util.List;

/**
 * This represents a native CPU which may run within its own thread to
 * execute code that is running from within the virtual machine.
 *
 * @since 2019/04/21
 */
public final class NativeCPU
	implements Runnable
{
	/** Maximum amount of CPU registers. */
	public static final int MAX_REGISTERS =
		64;
	
	/** The memory to read/write from. */
	protected final WritableMemory memory;
	
	/** Stack frames. */
	private final LinkedList<Frame> _frames =
		new LinkedList<>();
	
	/**
	 * Initializes the native CPU.
	 *
	 * @param __mem The memory space.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/21
	 */
	public NativeCPU(WritableMemory __mem)
		throws NullPointerException
	{
		if (__mem == null)
			throw new NullPointerException("NARG");
		
		this.memory = __mem;
	}
	
	/**
	 * Enters the given frame for the given address.
	 *
	 * @param __pc The address of the frame.
	 * @return The newly created frame.
	 * @since 2019/04/21
	 */
	public final Frame enterFrame(int __pc)
	{
		// Setup new frame
		Frame rv = new Frame();
		rv._pc = __pc;
		rv._lastpc = __pc;
		
		// Add to frame list
		this._frames.addLast(rv);
		
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/04/21
	 */
	@Override
	public final void run()
	{
		this.run(0);
	}
	
	/**
	 * Runs anything after this frame.
	 *
	 * @param __fl The frame limit.
	 * @since 2019/04/21
	 */
	public final void run(int __fl)
	{
		// Read the CPU stuff
		boolean reload = true;
		WritableMemory memory = this.memory;
		
		// Frame specific info
		Frame nowframe = null;
		int[] r = null;
		int pc = -1;
		
		// Execution is effectively an infinite loop
		LinkedList<Frame> frames = this._frames;
		for (int frameat = frames.size(), lastframe = -1; frameat >= __fl;
			frameat = frames.size())
		{
			// Reload parameters?
			if ((reload |= (lastframe != frameat)))
			{
				// Before dumping this frame, store old info
				if (nowframe != null)
					nowframe._pc = pc;
				
				// Get current frame, stop execution if there is nothing
				// left to execute
				nowframe = frames.peekLast();
				if (nowframe == null)
					return;
				
				// Load stuff needed for execution
				r = nowframe._registers;
				pc = nowframe._pc;
				
				// Used to auto-detect frame change
				lastframe = frameat;
				
				// No longer reload information
				reload = false;
			}
			
			// Read operation
			nowframe._lastpc = pc;
			int op = memory.memReadByte(pc);
			
			// Debug
			todo.DEBUG.note("@%08x -> (%02x) %s", pc,
				op, NativeInstruction.mnemonic(op));
			
			throw new todo.TODO();
		}
	}
	
	/**
	 * This represents a single frame in the execution stack.
	 *
	 * @since 2019/04/21
	 */
	public static final class Frame
	{
		/** Registers for this frame. */
		final int[] _registers =
			new int[MAX_REGISTERS];
		
		/** The PC address for this frame. */
		volatile int _pc;
		
		/** Last executed address. */
		int _lastpc;
	}
}
