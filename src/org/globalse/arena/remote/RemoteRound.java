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
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.User;

/**
 * This is the public interface for remotely accessing rounds. Only
 * <code>RemoteMatch</code>es use this interface to authenticate players and to send event notifications
 * to the arena server. Match front end do not directly interact with <code>RemoteRound</code>s,
 * instead, they call remote methods on <code>RemoteTournament</code>.
 *
 * @author Allen Dutoit
 */
public interface RemoteRound extends Remote {
	
	// Method used by matches to authorize changes of state
	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException;
	
	// Method used by remote matches to identify player
	public User getPlayer(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	// Method used by remote matches to fire arena listeners.
	public void fireMatchInfoChanged(String ticket, MatchInfo matchInfo)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
}

