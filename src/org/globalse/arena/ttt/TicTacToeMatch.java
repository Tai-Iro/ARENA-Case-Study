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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.Move;
import org.globalse.arena.remote.RemoteMatchListener;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidMoveException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.server.Match;
import org.globalse.arena.user.User;

/**
 * This class is a concrete match for the TicTacToe game. It tracks
 * the ongoing moves of two players, detects ties, victories, and losses,
 * and returns the rankings of the player once the match has completed.
 *
 * @see TicTacToe
 *
 * @author Allen Dutoit
 * @author Michael Nagel
 */
public class TicTacToeMatch extends Match {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.ttt");

    private static final int EMPTY = -1;
    private static final int PLR1 = 0;
    private static final int PLR2 = 1;
    private static final int NONE = 2;
    private static final int TIE = 3;
	
    private User[] players = new User[2];
    private int[][] board = new int[3][3];
	private List moves = new ArrayList();
    private int winner = NONE;
    private int turn = 0;
	
    TicTacToeMatch(RemoteRound round, User[] players) throws RemoteException {
		super(round, players);
		this.players = players;
		this.turn = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = EMPTY;
			}
		}
		logger.info("Successfully created tic tac toe match.");
    }
	
	synchronized protected Move[] getMoves() {
		return (Move[])moves.toArray(new Move[moves.size()]);
	}
	
    private void setWinner(int winner) {
		if (this.winner == NONE) {
			this.winner = winner;
			try {
				end();
			} catch (InvalidStateException e) {
				logger.warning("BUG: setWinner was invoked in an invalid state: " + e.getMessage());
				this.winner = NONE;
			}
		}
    }
	
    private void checkBoard() {
		int result = NONE;
		if ((board[0][0] != EMPTY)
			&& (((board[0][0] == board[0][1]) && (board[0][0] == board[0][2]))
					|| ((board[0][0] == board[1][1]) && (board[0][0] == board[2][2])) || ((board[0][0] == board[1][0]) && (board[0][0] == board[2][0]))))
			result = board[0][0];
		if ((board[1][0] != EMPTY) && (board[1][0] == board[1][1])
			&& (board[1][0] == board[1][2]))
			result = board[1][0];
		if ((board[0][1] != EMPTY) && (board[0][1] == board[1][1])
			&& (board[0][1] == board[2][1]))
			result = board[0][1];
		if ((board[0][2] != EMPTY)
			&& (((board[0][2] == board[1][2]) && (board[0][2] == board[2][2])) || ((board[0][2] == board[1][1]) && (board[0][2] == board[2][0]))))
			result = board[0][2];
		if ((board[2][0] != EMPTY) && (board[2][0] == board[2][1])
			&& (board[2][0] == board[2][2]))
			result = board[2][0];
		
		boolean free = false;
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				if (board[i][j] == EMPTY)
					free = true;
		
		if ((!free) && (result == NONE)) result = TIE;
		
		if (result != NONE) setWinner(result);
    }
	
	private void nextTurn() {
		turn = (1-turn);
	}
	
	private void fireMovePlayed(TicTacToeMove move) {
		new MatchNotifierThread(new MatchInfo(this), move) {
			public void notify(RemoteMatchListener listener) throws RemoteException {
				listener.movePlayed(info, move);
			}
		};
	}
	
	synchronized public void playMove(String matchTicket, Move m)
		throws InvalidMoveException, InvalidTicketException, AccessDeniedException {
		User user = getPlayerFromMatchTicket(matchTicket);
		if (!user.equals(m.getPlayer())) {
			throw new AccessDeniedException("User " + user.getLogin() + " cannot play for " + m.getPlayer().getLogin());
		}
		if (!(m instanceof TicTacToeMove)) {
			throw new InvalidMoveException("Only tic tac toe moves can be played in this game.");
		}
		TicTacToeMove move = (TicTacToeMove)m;
		if (!move.getPlayer().equals(players[turn])) {
			throw new InvalidMoveException("It is not your turn to play.");
		}
		int x = move.getX();
		int y = move.getY();
		if ((x > 2) || (y > 2)) {
			throw new InvalidMoveException("Move (" + x + "," + y + ") is off the board.");
		}
		if (board[x][y] != EMPTY) {
			throw new InvalidMoveException("Cell (" + x + "," + y + ") is not empty.");
		}
		board[x][y] = turn;
		moves.add(move);
		fireMovePlayed(move);
		checkBoard();
		nextTurn();
	}
	
	synchronized public void leave(RemoteMatchListener listener) {
		super.leave(listener);
		if (getState().equals(PLAYING)) {
			User [] connectedPlayers = getConnectedPlayers();
			if (connectedPlayers.length == 1) {
				if (connectedPlayers[0].equals(players[0])) {
					setWinner(PLR1);
				} else {
					setWinner(PLR2);
				}
			} else if (connectedPlayers.length == 0) {
				setWinner(TIE);
			}
		}
	}
	
    synchronized public User[][] getRanks()throws InvalidStateException {
		User [][] result = null;
		if (winner == TIE || getState().equals(TERMINATED)) {
			result = new User[][]{
				{players[0], players[1]}
			};
		} else if (getState().equals(FINISHED)) {
			if (winner == PLR1) {
				result = new User[][]{
					{players[0]},
					{players[1]}
				};
			} else {
				result = new User[][]{
					{players[1]},
					{players[0]}
				};
			}
		} else {
			throw new InvalidStateException("Player ranks are not defined until the match ends.");
		}
		return result;
    }
}
