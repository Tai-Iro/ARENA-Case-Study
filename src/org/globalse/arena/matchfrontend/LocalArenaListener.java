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


/**
 * This interface should be implemented by listeners on match front ends that
 * want to receive events from the arena server about changes in Leagues,
 * Tournaments, Rounds, and Matches (state changes only, no game moves).
 *
 * Local listeners should be wrapped with an ArenaListenerAdapter when registered
 * with the Arena.addListener method.
 *
 * Providing a local interface to match front ends removes the need for the
 * game-specific listeners to be in the server's class path.
 *
 * @see ArenaListenerAdapter
 * @see RemoteArenaListener
 * @author Allen Dutoit
 */

public interface LocalArenaListener {
	
	public void leagueInfoChanged(LeagueInfo leagueInfo);

	public void tournamentCreated(TournamentInfo tournamentInfo);

	public void tournamentInfoChanged(TournamentInfo tournamentInfo);

	public void roundCreated(RoundInfo roundInfo);
	
	public void matchInfoChanged(MatchInfo matchInfo);
}

