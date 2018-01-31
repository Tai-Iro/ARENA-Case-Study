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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.globalse.arena.gamepeer.RemoteGamePeer;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.exceptions.GamePeerAlreadyRegisteredException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.MatchNotFoundException;
import org.globalse.arena.server.Game;
import org.globalse.arena.server.Round;
import org.globalse.arena.user.User;

/**
 *
 */
public class GamePeerManager {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	// GamePeerManager is a singleton
	private GamePeerManager() {}
	private static GamePeerManager instance = new GamePeerManager();
	public static GamePeerManager getInstance() {
		return instance;
	}
	
	// Ordered by least recent use
	private List peers = new ArrayList();
	
	// Indexed by peer
	private Map peerTickets = new HashMap();
	
	// Indexed by peer
	private Map peerHostNames = new HashMap();
	
	// Indexed by match id
	private Map matches = new HashMap();
	
	synchronized public void registerGamePeer(RemoteGamePeer peer, String peerTicket) throws GamePeerAlreadyRegisteredException {
		if (peers.contains(peer)) {
			logger.warning("GamePeerManager did not register a game peer on host " +
							   peerHostNames.get(peer) + ", because it is already known to this Arena.");
			throw new GamePeerAlreadyRegisteredException("Game peer on " + peerHostNames.get(peer) + " is already registered.");
		}
		peers.add(peer);
		peerTickets.put(peer, peerTicket);
		String hostname = null;
		String hostinet = null;
		try {
			hostinet = RemoteServer.getClientHost();
			hostname = InetAddress.getByName(hostinet).getCanonicalHostName();
		} catch (ServerNotActiveException e) {
			// If this method was not invoked from a remote client,
			// the peer is local to this virtual machine.
			hostname = "localhost";
		} catch (UnknownHostException e) {
			hostname = hostinet;
		}
		peerHostNames.put(peer, hostname);
		logger.info("GamePeerManager registered game peer on host " + hostname);
		
	}
	
	synchronized public void unRegisterGamePeer(RemoteGamePeer peer) {
		if (!peers.contains(peer)) {
			logger.warning("Trying to unregister an unknown game peer.");
		} else {
			logger.info("GamePeerManager unregistering game peer on host " + peerHostNames.get(peer));
			peers.remove(peer);
			peerTickets.remove(peer);
			peerHostNames.remove(peer);
		}
	}
	
	synchronized public RemoteMatch createMatch(Round round, User[] players) {
		RemoteMatch match = null;
		Game game = round.getTournament().getLeague().getGame();
		String peerTicket = null;
		while (match == null) {
			try {
				if (peers.size() == 0) {
					logger.info("GamePeerManager creating match locally.");
					match = game.createMatch(round, players);
				} else {
					RemoteGamePeer peer = (RemoteGamePeer)peers.get(0);
					peers.remove(peer);
					peerTicket = (String) peerTickets.get(peer);
					logger.info("GamePeerManager creating match on game peer " + peerHostNames.get(peer));
					match = peer.createMatch(peerTicket, game, round, players);
					
					// If the match creation failed, the next statement is not reached.
					// Consequently, the peer is not added back in the queue, removing
					// stale references or misconfigured game peers.
					peers.add(peer);
				}
				logger.fine("... match successfully created.");
			} catch (InvalidTicketException e) {
				logger.severe("GamePeerManager failed to create match because game peer did not recognize ticket: " + peerTicket);
				e.printStackTrace();
			} catch (RemoteException e) {
				logger.warning("GamePeerManager failed to create match because connection to game peer failed.");
				e.printStackTrace();
			}
		}
		try {
			matches.put(match.getInfo().getMatchId(), match);
		} catch (RemoteException e) {
			// Connection to remote match failed during getInfo()
			match = null;
			logger.severe("GamePeerManager failed to get info from match, because connection failed.");
		}
		return match;
	}
	
	synchronized public MatchInfo getMatchById(String matchId)
		throws MatchNotFoundException, RemoteException {
		RemoteMatch match = (RemoteMatch)matches.get(matchId);
		if (match == null) {
			throw new MatchNotFoundException("Game peer manager does not know about match " + matchId + ".");
		}
		return match.getInfo();
	}
	
	synchronized public void releaseMatch(String matchId) {
		matches.remove(matchId);
	}
	
}

