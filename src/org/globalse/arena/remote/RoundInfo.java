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
import org.globalse.arena.server.Round;
import java.io.Serializable;
import org.globalse.arena.user.User;


/**
 * This class is a container for transporting information about rounds to and
 * from remote methods. Instances are created by the arena server and sent to the
 * match front ends. Once created, RoundInfos are not updated as rounds change.
 *
 * @see RemoteTournament#getRoundInfos
 * @author Michael Nagel
 */
public class RoundInfo implements Serializable {
	
	private RemoteLeague league;
	private RemoteTournament tournament;
	private MatchInfo[] matchInfos;
	private User[] byes;
	
	/**
	 * Creates a snapshot for the specified round. This constructor is typically
	 * only invoked by the arena server before serializing the RoundInfo for the
	 * match front end.
	 *
	 * @param    round               a  Round
	 *
	 */
	public RoundInfo(Round round) {
		this.tournament = round.getTournament();
		this.league = round.getTournament().getLeague();
		this.matchInfos = round.getMatchInfos();
		this.byes = round.getByes();
	}
	
	/**
	 * Returns a remote reference to the league associated with this round.
	 *
	 * @return   a RemoteLeague
	 *
	 */
	public RemoteLeague getLeague() {
		return league;
	}
	
	/**
	 * Returns a remote reference to the tournament associated with this round.
	 *
	 * @return   a RemoteTournament
	 *
	 */
	public RemoteTournament getTournament() {
		return tournament;
	}
	
	/**
	 * Returns an array of match info objects for the matches in this round. Match
	 * infos are snapshots of the matches at the time the invocation of this method.
	 *
	 * @return   a MatchInfo[]
	 *
	 */
	public MatchInfo[] getMatchInfos() {
		return matchInfos;
	}
	
	/**
	 * Return the byes associated with this round, that is, the players that do not
	 * play during this round and that are automatically qualified for the next round.
	 *
	 * @return   an User[]
	 *
	 */
	public User[] getByes() {
		return byes;
	}
}

