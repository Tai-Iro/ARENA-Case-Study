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

import java.io.Serializable;
import java.rmi.RemoteException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.server.Match;
import org.globalse.arena.user.User;

/**
 * This class is a container for transporting information about matches to and
 * from remote methods. Instances are created by the game peer and sent to the
 * match front ends. Once created, MatchInfos are not updated as Matches change.
 *
 * @see RemoteMatch#getInfo
 * @author Michael Nagel
 */
public class MatchInfo implements Serializable {
	
	private RemoteMatch match;
    private String id;
    private User[] players;
    private String state;
    private User[][] ranks;
	
    public MatchInfo(Match match) {
		this.match = match;
		this.id = match.getId();
		this.players = match.getPlayers();
		this.state = match.getState();
		this.ranks = null;
		try {
			this.ranks = match.getRanks();
		} catch (RemoteException e) {} catch (InvalidStateException e) {}
    }
	
    public String getMatchId() {
		return id;
    }
	
	public RemoteMatch getMatch() {
		return match;
	}
	
    public User[] getPlayers() {
		return players;
    }
	
	public boolean hasPlayer(User player) {
		for (int i = 0; i < players.length; i++) {
			if (players[i].equals(player)) {
				return true;
			}
		}
		return false;
	}
	
    public String getState() {
		return state;
    }
	
    public User[][] getRanks() {
		return ranks;
    }
}
