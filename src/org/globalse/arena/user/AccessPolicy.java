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

import org.globalse.arena.server.League;
import org.globalse.arena.server.Tournament;
import org.globalse.arena.remote.MatchInfo;

/**
 * This interface is implemented classes specifying the access policy
 * used by an arena. An access policy authorizes users with different
 * access levels (read, manage, and play) for different instances (of arena, leagues,
 * and tournaments).
 *
 * The access policy for matches is specified by the Match class and
 * cannot be changed.
 *
 * @author Allen Dutoit
 */
public interface AccessPolicy {

	public static final String READ = "read";
	public static final String MANAGE = "manage";
	public static final String PLAY = "play";
	
	public boolean hasArenaAccess(User user, String access);
	
	public boolean hasLeagueAccess(User user, League entity, String access);
	
	public boolean hasTournamentAccess(User user, Tournament entity, String access);
	
}

