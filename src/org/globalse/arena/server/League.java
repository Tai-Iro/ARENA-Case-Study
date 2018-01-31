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
package org.globalse.arena.server;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.RemoteLeague;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.TournamentStyleNotFoundException;
import org.globalse.arena.server.Arena;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

public class League extends UnicastRemoteObject implements RemoteLeague {
	
	// Attributes
	private String id;
    private String name;
    private String description;
	private boolean restricted = true;
	
	// Associations
	private User owner;
    private Game game;
	private TournamentStyle style;
    private List tournaments = new ArrayList();
	private List players = new ArrayList();
	
    
    public League(User owner, Game game, TournamentStyle style, String name, String description) throws RemoteException {
		if (owner == null) {
			throw new NullPointerException("Cannot create a league with a null owner.");
		}
		if (game == null) {
			throw new NullPointerException("Cannot create a league with a null game.");
		}
		if (style == null) {
			throw new NullPointerException("Cannot create a league with a null style.");
		}
		if (name == null) {
			throw new NullPointerException("Cannot create a league with a null name.");
		}
		this.owner = owner;
		this.game = game;
		this.style = style;
		this.id = (new UID()).toString();
		this.name = name;
		this.description = description;
		Arena.getInstance().addLeague(this);
    }
	
	synchronized public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException {
		return Arena.getInstance().hasLeagueAccess(ticket, this, access);
	}
	
	private void checkAccess(String ticket, String access) throws RemoteException, InvalidTicketException, AccessDeniedException {
		if (!Arena.getInstance().hasLeagueAccess(ticket, this, access)) {
			throw new AccessDeniedException("Ticket " + ticket
												+ " is not allowed to " + access + " the league " + this.getName() + ".");
		}
	}
	
	synchronized public String getId() {
		return id;
	}
	
    synchronized public String getName() {
		return name;
    }
	
    synchronized public void setName(String ticket, String string) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		name = string;
		Arena.getInstance().getNotifier().fireLeagueInfoChanged(this);
    }
	
    synchronized public String getDescription() {
		return description;
    }
	
    synchronized public void setDescription(String ticket, String string) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		description = string;
		Arena.getInstance().getNotifier().fireLeagueInfoChanged(this);
    }
	
	synchronized public LeagueInfo getInfo() throws RemoteException {
		return new LeagueInfo(this);
	}
	
	synchronized public void unrestrict(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		restricted = false;
		Arena.getInstance().getNotifier().fireLeagueInfoChanged(this);
	}
	
	synchronized public void restrict(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		restricted = true;
		Arena.getInstance().getNotifier().fireLeagueInfoChanged(this);
	}
	
	synchronized public boolean isRestricted() {
		return restricted;
	}

    synchronized public Game getGame() {
		return game;
    }

	synchronized public TournamentStyle getTournamentStyle() {
		return style;
	}
	
	synchronized public User getOwner() {
		return owner;
	}
	
	synchronized public void setOwner(String ticket, User newOwner) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		this.owner = newOwner;
		Arena.getInstance().getNotifier().fireLeagueInfoChanged(this);
	}
	
	synchronized public User[] getPlayers() {
		return (User[])players.toArray(new User[players.size()]);
	}
	
	synchronized public boolean hasPlayer(User player) {
		return players.contains(player);
	}
	
	synchronized public void addPlayer(String ticket, User player) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!players.contains(player)) {
			players.add(player);
		}
	}
	
	synchronized public void removePlayer(String ticket, User player) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (players.contains(player)) {
			players.remove(player);
		}
	}
	
    synchronized public Tournament[] getTournaments() {
		return (Tournament[])tournaments.toArray(new Tournament[tournaments.size()]);
    }
	
	synchronized public TournamentInfo [] getTournamentInfos(String ticket) throws RemoteException, InvalidTicketException {
		List result = new ArrayList();
		Tournament[] tournaments = this.getTournaments();
		for (int i = 0; i < tournaments.length; i++) {
			if (Arena.getInstance().hasTournamentAccess(ticket, tournaments[i], AccessPolicy.READ)) {
				result.add(new TournamentInfo(tournaments[i]));
			}
		}
		return (TournamentInfo[])result.toArray(new TournamentInfo[result.size()]);
	}
	
    synchronized public TournamentInfo createTournament(String ticket, String name, String description)
		throws RemoteException, InvalidTicketException, AccessDeniedException, TournamentStyleNotFoundException, InvalidStateException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		Tournament tournament = new Tournament(this);
		tournaments.add(tournament);
		tournament.setName(ticket, name);
		tournament.setDescription(ticket, description);
		TournamentInfo tournamentInfo = new TournamentInfo(tournament);
		Arena.getInstance().getNotifier().fireTournamentCreated(tournament);
		return tournamentInfo;
    }
    
}
