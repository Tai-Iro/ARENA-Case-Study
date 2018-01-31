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

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.TournamentStyleNotFoundException;
import org.globalse.arena.user.User;

/**
 * This is the public interface for remotely accessing a league.
 *
 * A <code>RemoteLeague</code>is a remote object accessible via RMI. Notification
 * events about changes in the league are sent over the RemoteArenaListener associated with
 * the game for this league. Clients use this interface to create and set up new
 * tournaments or to access remote references to tournaments in this league. To access the
 * attributes of the league, the client first uses the getInfo method to return a
 * snapshop, and then accesses the invidual attributes directly from the info object.
 *
 * @author Michael Nagel
 * @author Allen Dutoit
 */
public interface RemoteLeague extends Remote {
	
	public LeagueInfo getInfo()
		throws RemoteException;
	
	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException;
	
	public TournamentInfo[] getTournamentInfos(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public TournamentInfo createTournament(String ticket, String name, String description)
		throws RemoteException, InvalidTicketException, AccessDeniedException, TournamentStyleNotFoundException, InvalidStateException;
	
	public void restrict(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public void unrestrict(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public void addPlayer(String ticket, User player)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public void removePlayer(String ticket, User player)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
//    public void archive(String ticket)
//		throws RemoteException, InvalidTicketException, AccessDeniedException;

}

