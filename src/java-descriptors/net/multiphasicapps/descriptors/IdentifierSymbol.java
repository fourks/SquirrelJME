// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.descriptors;

/**
 * This represents an identifier which is part of a binary name or is the name
 * of a method or field.
 *
 * @since 2016/03/14
 */
public final class IdentifierSymbol
	extends __BaseSymbol__
{
	/** Is this a valid method name? */
	protected final boolean validmethod;
	
	/**
	 * Initializes an identifier symbol.
	 *
	 * @param __s The representation of the symbol.
	 * @throws IllegalSymbolException If the identifier contains illegal
	 * characters.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/03/14
	 */
	public IdentifierSymbol(String __s)
		throws IllegalSymbolException, NullPointerException
	{
		super(__s);
		
		// Cannot be blank
		int n = length();
		if (n <= 0)
			throw new IllegalSymbolException(toString());
		
		
		// Check characters
		boolean gtlt = false;
		for (int i = 0; i < n; i++)
			switch (charAt(i))
			{
					// These are illegal
				case '.':
				case ';':
				case '[':
				case '/':
					throw new IllegalSymbolException(toString());
				
					// Flag these
				case '<':
				case '>':
					gtlt = true;
					break;
				
					// Fine
				default:
					break;
			}
		
		// Valid method?
		validmethod = (!gtlt || toString().equals("<init>") ||
			toString().equals("<clinit>"));
	}
	
	/**
	 * Returns {@code true} if this is a valid name for a method.
	 *
	 * @return {@code true} if a valid name for a method.
	 * @since 2016/03/14
	 */
	public boolean isValidMethod()
	{
		return validmethod;
	}
}

