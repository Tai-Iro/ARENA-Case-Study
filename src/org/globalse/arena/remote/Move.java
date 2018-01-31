/*
 * Copyright 2004 (C) Applied Software Engineering--TU Muenchen
 *                    http://wwwbruegge.in.tum.de
 *
 * This file is part of ARENA.
 *
 * ARENA is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ARENA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ARENA; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.globalse.arena.remote;

import org.globalse.arena.user.User;
import java.io.Serializable;


/**
 * This interface is implemented by classes representing game moves. Moves
 * are sent from match peers to the game peers via the remote method
 * {@link RemoteMatch#playMove}. The moves are then broadcast to other match from ends
 * (both players and spectators) via {@link RemoteMatchListener RemoteMatchListeners}.
 *
 * Only games that use the arena mechanism for broadcasting moves need to
 * implement this class. Games with tight response time requirements may choose
 * to implement their own communication mechanisms between RemoteMatches and
 * MatchPanels.
 *
 * @see RemoteMatch
 * @see RemoteMatchListener
 *
 * @author Allen Dutoit
 */
public interface Move extends Serializable {
	
	public User getPlayer();
}

