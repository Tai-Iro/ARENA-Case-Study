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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.Logger;
import org.globalse.arena.remote.RemoteArena;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.GamePeerAlreadyRegisteredException;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.server.Game;
import org.globalse.arena.user.User;

/**
 * Implementation of <code>RemoteGamePeer</code>. <code>GamePeer</code> registers with the
 * specified arena server when created and then waits for requests for creating matches.
 * Remote matches live on until it is not referenced by anybody and
 * garbage collected.
 *
 * @author Allen Dutoit
 */
public class GamePeer extends UnicastRemoteObject implements RemoteGamePeer {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.gamepeer");

	// Random see for creating game peer tickets
	private static Random random = new Random();
	
	// Game peer ticket expected when creating matches
	private String peerTicket;
	
	/**
	 * Creates a game peer object and registers it with the specified arena using
	 * the specified operator user name and password. This constructor also generates
	 * a random ticket that the arena server uses for authentication when creating
	 * matches. Assuming a secure communication channel between game peer and arena server,
	 * this prevents both rogue peers and rogue arena servers.
	 *
	 * @param    arenaHost           the hostname or ip address where the arena server is running
	 * @param    arenaPort           the TCP/IP port the arena server is waiting for RMI requests
	 * @param    operatorName        the user name of the operator
	 * @param    operatorPassword    the password of the operator
	 *
	 * @exception   RemoteException
	 * @exception   NotBoundException
	 * @exception   MalformedURLException
	 * @exception   InvalidLoginException
	 * @exception   GamePeerAlreadyRegisteredException
	 * @exception   InvalidTicketException
	 * @exception   AccessDeniedException
	 *
	 */
    public GamePeer(String arenaHost, int arenaPort, String operatorName, String operatorPassword)
		throws RemoteException, NotBoundException, MalformedURLException, InvalidLoginException, GamePeerAlreadyRegisteredException, InvalidTicketException, AccessDeniedException {
		super();
		String url = "rmi://" + arenaHost + ":" + arenaPort + "/ArenaServer";
		RemoteArena arena = (RemoteArena)Naming.lookup(url);
		String arenaTicket = arena.login(operatorName, operatorPassword);
		peerTicket = Long.toHexString(random.nextLong());
		arena.registerGamePeer(arenaTicket, this, peerTicket);
		logger.info("Game peer successfully registered.");
	}
	
	synchronized public RemoteMatch createMatch(String peerTicket, Game game, RemoteRound round, User[] players)
		throws RemoteException, InvalidTicketException {
		if (!this.peerTicket.equals(peerTicket)) {
			throw new InvalidTicketException("Game peer received invalid ticket.");
		}
		RemoteMatch result = game.createMatch(round, players);
		logger.info("Game peer successfully created match.");
		return result;
	}
	
}
