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
package org.globalse.arena.matchfrontend;
import org.globalse.arena.remote.*;


import org.globalse.arena.user.User;

/**
 * This interface should be implemented by listeners on the match front ends
 * wanting to receive events during a match, such as change of states, players
 * joining or leaving, or moves. Local listeners should then be wrapped with a
 * MatchListenerAdapter and registered with the Match.join or Match.watch
 * methods.
 *
 * @see MatchListenerAdapter
 * @see RemoteMatchListener
 * @author Allen Dutoit
 */
public interface LocalMatchListener {

	public void matchStarted(MatchInfo match);
	
	public void matchEnded(MatchInfo match);
	
	public void matchTerminated(MatchInfo match);
	
	public void movePlayed(MatchInfo match, Move move);
	
}

