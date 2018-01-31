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
package org.globalse.arena.ttt;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.server.Game;
import org.globalse.arena.server.Match;
import org.globalse.arena.server.Statistics;
import org.globalse.arena.user.User;

/**
 * This class is a concrete game class for the classic two-player tic tac toe game.
 * It provides factory methods for creating tic tac toe matches and tic tac toe
 * statistics. Tic tac toe matches involve exactly two players.
 *
 * @see TicTacToeMatch
 * @see TicTacToeStatistics
 *
 * @author Allen Dutoit
 * @author Michael Nagel
 */
public class TicTacToe implements Game {

	private static Logger logger = Logger.getLogger("org.globalse.arena.ttt");
	
	private TicTacToe() {}
	private static Game instance = new TicTacToe();
	public static Game getInstance() {
		return instance;
	}

	public Match createMatch(RemoteRound round, User[] players) throws RemoteException {
		logger.info("Creating tic tac toe match.");
		return new TicTacToeMatch(round, players);
	}

    public int getMinPlayersPerMatch() {
        return 2;
    }
	
	public int getMaxPlayersPerMatch() {
		return 2;
	}

	public Statistics createStatistics() {
		return new TicTacToeStatistics();
	}

}
