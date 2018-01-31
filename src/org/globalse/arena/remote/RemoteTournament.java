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
import org.globalse.arena.remote.exceptions.InvalidNumPlayersException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.TournamentOverbookedException;
import org.globalse.arena.user.User;

/**
 * This is the public interface for remotely accessing a tournament.
 *
 * A <code>RemoteTournament</code>is a remote object accessible via RMI. Notification
 * events about changes in the tournament are sent over the RemoteArenaListener associated with
 * the game for this tournament. Clients use this interface to plan new
 * rounds or to access remote references to rounds or matches in this tournament. To access the
 * attributes of the tournament, the client first uses the getInfo method to return a
 * snapshop, and then accesses the invidual attributes directly from the info object.
 *
 * @author Michael Nagel
 * @author Allen Dutoit
 */
public interface RemoteTournament extends Remote {
	
	// initializing, only visible to the league owner
	public static final String INITIALIZING = "initializing";
	// registration open, accepting applications
    public static final String REGISTRATION = "registration open";
	// registration closed, accepting interested players
    public static final String REGISTRATIONFINISHED = "registration closed";
	// current round planned
    public static final String PLAYING = "playing";
	// current round finished, waiting for next round to be planned
    public static final String ROUNDFINISHED = "current round ended";
	// normal end
    public static final String FINISHED = "tournament ended";
	// abnormal end
	public static final String TERMINATED = "tournament prematurely terminated";
	
    public TournamentInfo getInfo()
		throws RemoteException;
	
	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException;
	
	public void setName(String ticket, String name)
		throws RemoteException,	InvalidTicketException, AccessDeniedException;
	
	public void setDescription(String ticket, String description)
		throws RemoteException,	InvalidTicketException, AccessDeniedException;
	
	public void setMaxNumPlayers(String ticket, int max)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public void openRegistration(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;
	
	public void closeRegistration(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;
	
	public void apply(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;
	
	public void acceptPlayer(String ticket, User player)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException, TournamentOverbookedException;
	
	public void rejectPlayer(String ticket, User player)
		throws RemoteException, InvalidTicketException, InvalidStateException, AccessDeniedException;
	
	public void launch(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException, InvalidNumPlayersException;
	
	public void terminate(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;
	
	public RoundInfo[] getRoundInfos(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public RoundInfo getCurrentRoundInfo(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	//    public void archive(String ticket)
//		throws RemoteException, InvalidTicketException, AccessDeniedException;
	

}

