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

import java.io.Serializable;
import java.rmi.RemoteException;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.user.User;

/**
 * The Game interface is an abstract factory  for creating Match and Statistics
 * specific to a game. It also provides general information about the game. To integrate a new game
 * in an Arena, a developer needs to provide concrete implementations of Game, Match, and Statistics,
 * develop a MatchPanelFrontEnd for viewing and playing matches, and register the Game in the
 * properties file loaded by the StartArena.main() method.
 *
 * Games are serializable so that they can be sent to GamePeers when matches
 * are created remotely. Moreover, Game classes are singletons and must define a public static
 * getInstance method so that Arena can access the singleton with the java reflection API.
 *
 * For an example of simple game, see the {@link org.globalse.arena.ttt} package.
 *
 * Note that the name of the game is stored separately, so that
 * the Arena operator can ensure that each game is registered under a unique name.
 *
 * @see Match
 * @see Statistics
 * @see org.globalse.arena.remote.MatchPanelFactory
 *
 * @author Allen Dutoit
 */
public interface Game extends Serializable {

	/**
	 * Returns the minimum number of players needed for a match of this game.
	 *
	 */
	public int getMinPlayersPerMatch();
	
	/**
	 * Returns the maximum number of players who can take part in the same match of this game.
	 * This value should be equal or more than the minimum number of players.
	 *
	 */
	public int getMaxPlayersPerMatch();

	/**
	 * Creates a new match of this game for the specified round and players. The match is
	 * created locally. However, it is possible that the round is a remote object
	 * on another host, when this method is invoked by a game peer. This method throws
	 * a RemoteException when the newly created match cannot add itself to the remote round.
	 *
	 * @param    round               The round in which the new match will take place
	 * @param    players             An array of users specifying which players should take part in this match. The length of the array should be between getMinPlayersPerMatch and getMaxPlayersPerMatch.
	 *
	 * @return   the new match
	 *
	 * @exception   RemoteException
	 *
	 */
	public Match createMatch(RemoteRound round, User[] players) throws RemoteException;
	
	/**
	 * Creates a new statistics object for this game.
	 *
	 * @return   the new statistics object
	 *
	 */
	public Statistics createStatistics();

}
