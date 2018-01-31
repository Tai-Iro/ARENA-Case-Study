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
package org.globalse.arena.gamepeer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.server.Game;
import org.globalse.arena.user.User;

/**
 * Remote interface for the game peer, which includes a single method
 * for the arena server to request the create of a remote match.
 *
 * @author Allen Dutoit
 */
public interface RemoteGamePeer extends Remote {

	/**
	 * This method creates a match in the context of this game peer and returns
	 * a remote reference to the new match.
	 *
	 * @param    ticket              the game peer ticket that was used to register the game peer with the server
	 * @param    game                the concrete game for which the match should be created; the game should be in the class path of the game peer or accessible via the code base specified in the arena server
	 * @param    round               the round to which the new match belongs; the round always resides on the arena server
	 * @param    players             an array of players that will take part in the match
	 *
	 * @return   a remote reference to the newly created match
	 *
	 * @exception   RemoteException
	 * @exception   InvalidTicketException
	 *
	 * @see org.globalse.arena.remote.RemoteArena#registerGamePeer
	 */
	public RemoteMatch createMatch(String ticket, Game game, RemoteRound round, User[] players)
		throws RemoteException, InvalidTicketException;
	
}
