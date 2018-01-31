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
package org.globalse.arena.user;

import org.globalse.arena.server.Arena;
import org.globalse.arena.server.League;
import org.globalse.arena.server.Match;
import org.globalse.arena.server.Tournament;
import org.globalse.arena.user.User;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.MatchInfo;
import java.rmi.RemoteException;

/**
 * This class provides a default access policy for arenas.
 *
 * @author Allen Dutoit
 */
public class DefaultAccessPolicy implements AccessPolicy {
	
	public boolean hasArenaAccess(User user, String access) {
		if (access.equals(MANAGE)) {
			return user.equals(Arena.getInstance().getOperator());
		}
		// Anybody can see or play in an arena.
		return true;
	}
	
	public boolean hasLeagueAccess(User user, League league, String access) {
		// A user can manage a league only if he owns it.
		if (access.equals(MANAGE)) {
			return user.equals(league.getOwner());
		}
		// A user can see or play in the league if it is unrestricted or if the
		// player is registered with the league.
		if (!league.isRestricted()) {
			return true;
		}
		// In restricted leagues, only the league owner, the registered player,
		// and the operator can see or play.
		return league.hasPlayer(user) ||
			league.getOwner().equals(user) ||
			Arena.getInstance().getOperator().equals(user);
	}
	
	public boolean hasTournamentAccess(User user, Tournament tournament, String access) {
		Arena arena = Arena.getInstance();
		League league = tournament.getLeague();
		User leagueOwner = league.getOwner();
		User operator = Arena.getInstance().getOperator();
		
		// A user can manage a facilitated tournament only if he owns the league. Players
		// and league owners can manage unfaciltiated tournaments.
		if (access.equals(MANAGE)) {
			if (user.equals(leagueOwner)) {
				return true;
			}
			if (!tournament.isFacilitated()) {
				return hasTournamentAccess(user, tournament, PLAY);
			}
			return false;
		}
		// A user can play in the tournament if the league is unrestricted or
		// if the player is registered with the tournament.
		// Guests cannot play
		if (access.equals(PLAY)) {
			if (arena.isUserGuest(user)) {
				return false;
			}
			if (!league.isRestricted()) {
				return true;
			}
			return tournament.isPlayerAccepted(user);
		}
		if (access.equals(READ)) {
			// Only the league owner and the operator can see a tournament
			// before it is initialized
			if (tournament.getState().equals(Tournament.INITIALIZING)) {
				return
					user.equals(leagueOwner) ||
					user.equals(operator);
			}
			// Anybody can see unrestricted leagues after they are initialized.
			if (!league.isRestricted()) {
				return true;
			}
			// Only players of a restricted league, the league owner, or the
			// operatorator can see the tournament.
			if (league.isRestricted()) {
				return league.hasPlayer(user) ||
					user.equals(leagueOwner) ||
					user.equals(operator);
			}
		}
		return false;
	}
}

