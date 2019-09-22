// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package todo;

import cc.squirreljme.jvm.Assembly;
import cc.squirreljme.jvm.SystemCallError;
import cc.squirreljme.jvm.SystemCallIndex;

/**
 * Contains basic debug output printing.
 *
 * @since 2019/05/26
 */
public final class DEBUG
{
	/**
	 * Not used.
	 *
	 * @since 2019/05/26
	 */
	private DEBUG()
	{
	}
	
	/**
	 * Prints a very basic debug code.
	 *
	 * @param __a First character.
	 * @param __b Second character.
	 * @since 2019/09/22
	 */
	public static final void code(char __a, char __b)
	{
		// Get the pipe descriptor for standard error, ignore if it fails
		int fd = Assembly.sysCallV(SystemCallIndex.PD_OF_STDERR);
		if (SystemCallError.getError(SystemCallIndex.PD_OF_STDERR) != 0)
			return;
		
		// Pipe characters
		DEBUG.__pipe(fd, __a);
		DEBUG.__pipe(fd, __b);
		
		// Ending newline
		DEBUG.__pipe(fd, '\n');
	}
	
	/**
	 * Prints a very basic debug code.
	 *
	 * @param __a First character.
	 * @param __b Second character.
	 * @param __v Value.
	 * @since 2019/06/13
	 */
	public static final void code(char __a, char __b, int __v)
	{
		// Get the pipe descriptor for standard error, ignore if it fails
		int fd = Assembly.sysCallV(SystemCallIndex.PD_OF_STDERR);
		if (SystemCallError.getError(SystemCallIndex.PD_OF_STDERR) != 0)
			return;
		
		// Pipe characters
		DEBUG.__pipe(fd, __a);
		DEBUG.__pipe(fd, __b);
		DEBUG.__pipe(fd, ' ');
		
		// Print digit as hex, this should print all 8 digits
		for (int i = 28; i >= 0; i -= 4)
		{
			// Get upper most hex
			int h = ((__v >>> i) & 0xF);
			
			// Print letter or number?
			DEBUG.__pipe(fd, (char)(h >= 10 ? 'a' + (h - 10) : '0' + h));
		}
		
		// Ending newline
		DEBUG.__pipe(fd, '\n');
	}
	
	/**
	 * Prints a barrier in printing!
	 *
	 * @param __a First character.
	 * @param __b Second character.
	 * @since 2019/09/22
	 */
	public static final void codeBarrier(char __a, char __b)
	{
		// Get the pipe descriptor for standard error, ignore if it fails
		int fd = Assembly.sysCallV(SystemCallIndex.PD_OF_STDERR);
		if (SystemCallError.getError(SystemCallIndex.PD_OF_STDERR) != 0)
			return;
		
		// Pipe characters
		DEBUG.__pipe(fd, __a);
		DEBUG.__pipe(fd, __b);
		DEBUG.__pipe(fd, ' ' );
		
		// Print barrier
		for (int i = 0; i < 20; i++)
			DEBUG.__pipe(fd, '*');
		
		// Ending newline
		DEBUG.__pipe(fd, '\n');
	}
	
	/**
	 * Prints a very basic debug code with a String Utf pointer.
	 *
	 * @param __a First character.
	 * @param __b Second character.
	 * @param __v UTF String pointer.
	 * @since 2019/09/22
	 */
	public static final void codeUtf(char __a, char __b, int __v)
	{
		// Get the pipe descriptor for standard error, ignore if it fails
		int fd = Assembly.sysCallV(SystemCallIndex.PD_OF_STDERR);
		if (SystemCallError.getError(SystemCallIndex.PD_OF_STDERR) != 0)
			return;
		
		// Pipe characters
		DEBUG.__pipe(fd, __a);
		DEBUG.__pipe(fd, __b);
		DEBUG.__pipe(fd, ' ');
		
		// If there is no string, then print null
		if (__v == 0)
		{
			DEBUG.__pipe(fd, 'n');
			DEBUG.__pipe(fd, 'u');
			DEBUG.__pipe(fd, 'l');
			DEBUG.__pipe(fd, 'l');
		}
		
		// Otherwise decode and print the string out
		else
		{
			// Read string length
			int len = Assembly.memReadJavaShort(__v, 0) & 0xFFFF;
			
			// Print string characters out
			for (int i = 0; i < len; i++)
			{
				// Read character
				char c = (char)(Assembly.memReadByte(__v, 2 + i) & 0xFF);
				
				// Special handling according to input character
				boolean slash = false;
				switch (c)
				{
					case '\t': c = 't'; break;
					case '\r': c = 'r'; break;
					case '\n': c = 'n'; break;
					case '\b': c = 'b'; break;
				}
				
				// Print it out
				if (slash)
					DEBUG.__pipe(fd, '\\');
				DEBUG.__pipe(fd, c);
			}
		}
		
		// Ending newline
		DEBUG.__pipe(fd, '\n');
	}
	
	/**
	 * Prints a debug note.
	 *
	 * @param __fmt The string format, compatible with Java except that it
	 * is very limited in the formats it supports.
	 * @param __args Arguments to the note.
	 * @since 2019/05/26
	 */
	public static final void note(String __fmt, Object... __args)
	{
		// Get the pipe descriptor for standard error, ignore if it fails
		int fd = Assembly.sysCallV(SystemCallIndex.PD_OF_STDERR);
		if (SystemCallError.getError(SystemCallIndex.PD_OF_STDERR) != 0)
			return;
		
		// Debug
		/*DEBUG.code('N', 'f', Assembly.objectToPointer(__fmt));
		DEBUG.code('N', 'a', Assembly.objectToPointer(__args));*/
		
		// Argument pointer
		int argp = 0;
		
		// Print char by char to the console
		boolean percent = false;
		for (int i = 0, n = __fmt.length(); i < n; i++)
		{
			// Read character here
			char c = __fmt.charAt(i);
			
			// Handle percent
			if (percent)
			{
				// Clear flag
				percent = false;
				
				// Plain percent
				if (c == '%')
					DEBUG.__pipe(fd, '%');
				
				// Newline
				else if (c == 'n')
					DEBUG.__pipe(fd, '\n');
				
				// Just treat as string
				else if (argp < __args.length)
				{
					// Get string form of it
					Object av = __args[argp++];
					String sv = (av == null ? "null" : av.toString());
					
					// Pipe through all string characters
					for (int j = 0, q = sv.length(); j < q; j++)
						DEBUG.__pipe(fd, sv.charAt(j));
				}
				
				// Unknown sequence?
				else
					DEBUG.__pipe(fd, '?');
			}
			
			// Flag percent
			else if (c == '%')
				percent = true;
			
			// Plain character
			else
				DEBUG.__pipe(fd, c);
		}
		
		// End with newline sequence
		DEBUG.__pipe(fd, '\n');
	}
	
	/**
	 * Pipes out the given character.
	 *
	 * @param __fd The pipe descriptor.
	 * @param __c The character to pipe.
	 * @since 2019/06/11
	 */
	private static final void __pipe(int __fd, char __c)
	{
		// This uses non-wrapped calls since because this is the supervisor
		// we want to print out the bytes without performing wrapping and
		// virtualization of call IDs
		
		// Single byte sequence
		if (__c <= 0x7F)
		{
			// Forward
			Assembly.sysCallPV(SystemCallIndex.PD_WRITE_BYTE,
				__fd, __c & 0xFF);
		}
		
		// Double byte sequence
		else
		{
			// Forward
			Assembly.sysCallPV(SystemCallIndex.PD_WRITE_BYTE,
				__fd, (__c >>> 6) | 0b1100_0000);
			Assembly.sysCallPV(SystemCallIndex.PD_WRITE_BYTE,
				__fd, (__c & 0b111111));
		}
	}
}

