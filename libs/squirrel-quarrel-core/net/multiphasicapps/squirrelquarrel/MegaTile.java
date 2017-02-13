// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirrelquarrel;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This represents a single mega tile which represents a small region in the
 * level.
 *
 * @since 2017/02/09
 */
public class MegaTile
{
	/** The number of tiles per mega tile. */
	public static final int TILES_PER_MEGA_TILE =
		8;
	
	/** The number of tiles in mega tiles. */
	public static final int TILES_IN_MEGA_TILE =
		TILES_PER_MEGA_TILE * TILES_PER_MEGA_TILE;
	
	/** The size of tiles in pixels. */
	public static final int TILE_PIXEL_SIZE =
		32;
	
	/** The size of megatiles in pixels. */
	public static final int MEGA_TILE_PIXEL_SIZE =
		TILE_PIXEL_SIZE * TILES_PER_MEGA_TILE;
	
	/** The owning level */
	protected final Level level;
	
	/** Terrain information. */
	protected final byte[] terrain =
		new byte[TILES_IN_MEGA_TILE];
	
	/** Fog of war revealed information. */
	protected final byte[] revealedfog =
		new byte[TILES_IN_MEGA_TILE];
	
	/**
	 * Initializes a basic megatile.
	 *
	 * @param __l The owning level.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/02/10
	 */
	public MegaTile(Level __l)
		throws NullPointerException
	{
		// Check
		if (__l == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.level = __l;
		
		// Initialize it with some pattern
		byte[] terrain = this.terrain;
		for (int y = 0; y < TILES_PER_MEGA_TILE; y++)
			for (int x = 0; x < TILES_PER_MEGA_TILE; x++)
				terrain[(y * TILES_PER_MEGA_TILE) + x] =
					(byte)(((x + y) / 2) & 1);
	}
	
	/**
	 * Initializes the megatile from a previously serialized replay or save
	 * game.
	 *
	 * @param __l The owning level.
	 * @param __is The stream to read from.
	 * @throws IOException On read errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/02/10
	 */
	public MegaTile(Level __l, DataInputStream __is)
		throws IOException, NullPointerException
	{
		// Check
		if (__l == null || __is == null)
			throw new NullPointerException("NARG");
		
		// Set
		this.level = __l;
		
		throw new Error("TODO");
	}
	
	/**
	 * Gets the terrain for the given sub-tile.
	 *
	 * @param __x The tile X position.
	 * @param __y The tile Y position.
	 * @return The terrain type for the given tile.
	 * @throws IndexOutOfBoundsException If the position is not in the megatile
	 * bounds.
	 * @since 2017/02/11
	 */
	public TerrainType subTileTerrain(int __x, int __y)
		throws IndexOutOfBoundsException
	{
		// {@squirreljme.error BE03 Cannot get terrain because the tile is
		// out of range.}
		if (__x < 0 || __y < 0 || __x >= TILES_PER_MEGA_TILE ||
			__y >= TILES_PER_MEGA_TILE)
			throw new IndexOutOfBoundsException("BE03");
		
		// Depends
		return TerrainType.of(this.terrain[(__y * TILES_PER_MEGA_TILE) + __x]);
	}
	
	/**
	 * This checks whether the given sub-tile is revealed by the given player.
	 *
	 * @param __p The player to check if they can see the given tile.
	 * @param __x The tile X position.
	 * @param __y The tile Y position.
	 * @return {@code true} if it is revealed.
	 * @throws IndexOutOfBoundsException If the position is not in the megatile
	 * bounds.
	 * @throws NullPointerException On null arguments
	 * @since 2017/02/13
	 */
	public boolean subTileRevealed(Player __p, int __x, int __y)
		throws IndexOutOfBoundsException, NullPointerException
	{
		// Check
		if (__p == null)
			throw new NullPointerException("NARG");
		
		// {@squirreljme.error BE05 Cannot get revealed state because the tile
		// is out of range.}
		if (__x < 0 || __y < 0 || __x >= TILES_PER_MEGA_TILE ||
			__y >= TILES_PER_MEGA_TILE)
			throw new IndexOutOfBoundsException("BE05");
		
		// Check if it is revealed
		return (this.revealedfog[
			(__y * TILES_PER_MEGA_TILE) + __x] & __p.visionMask()) != 0;
	}
}

