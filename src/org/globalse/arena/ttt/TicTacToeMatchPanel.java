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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.globalse.arena.matchfrontend.LocalMatchListener;
import org.globalse.arena.matchfrontend.MatchFrontEnd;
import org.globalse.arena.matchfrontend.MatchListenerAdapter;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.Move;
import org.globalse.arena.remote.RemoteArena;
import org.globalse.arena.remote.RemoteLogger;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidMoveException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.ttt.TicTacToeMove;
import org.globalse.arena.user.User;

public class TicTacToeMatchPanel extends JPanel implements LocalMatchListener, AncestorListener {
	
	public static final int STATE_DISCONNECT = -2;
    public static final int STATE_INITIALIZING = -1;
    public static final int STATE_WAIT = 0;
    public static final int STATE_MOVE = 1;
    public static final int STATE_SECOND_PLAYER_WON = 2;
    public static final int STATE_FIRST_PLAYER_WON = 3;
    public static final int STATE_MATCH_TIED = 4;
    public static final int STATE_MATCH_TERMINATED = 5;
    public static final long EMPTY = 0L;
	
	private User user;
	private RemoteArena remoteArena;
	private RemoteLogger logger;
	private int state = STATE_INITIALIZING;
	private boolean playing = false;
	private long[][] board;
	private MatchInfo matchInfo;
	private String matchTicket;
	private MatchListenerAdapter adapter;
	
	private JButton buttons[][] = new JButton[3][3];
	private JLabel status;
	
	public TicTacToeMatchPanel(String ticket, MatchInfo matchInfo) {
		init();
		retrieveMatch(matchInfo);
//		setState(STATE_INITIALIZING);
		addAncestorListener(this);
	}
	
	private void retrieveMatch(MatchInfo matchInfo) {
		this.remoteArena = MatchFrontEnd.getRemoteArena();
		this.logger = new RemoteLogger(remoteArena);
		this.matchInfo = matchInfo;
		RemoteMatch match = matchInfo.getMatch();
		this.user = MatchFrontEnd.getUser();
		this.state = STATE_INITIALIZING;
		this.board = new long[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				this.board[i][j] = EMPTY;
		
		try {
			logger.log(Level.INFO, "TicTacToeMatchPanel", "Establishing TTT specific connection to match...");
			this.adapter = new MatchListenerAdapter(this);
			if (matchInfo.hasPlayer(user)) {
				this.playing = true;
				this.matchTicket = match.join(MatchFrontEnd.getTicket(), adapter);
			} else {
				this.playing = false;
				Move [] moves = match.watch(adapter);
				for (int i = 0; i < moves.length; i++) {
					movePlayed(matchInfo, moves[i]);
				}
			}
			logger.log(Level.INFO, "TicTacToeMatchPanel", "... TTT connection established, waiting for match events.");
		} catch (Exception e) {
			logger.log(Level.WARNING, "TicTacToeMatchPanel", "Encountered exception while setting up match: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public class ButtonListener implements ActionListener {
		
		private int x;
		private int y;
		
		public ButtonListener( int x, int y ) {
			this.x= x;
			this.y = y;
		}
		
		public void actionPerformed(ActionEvent ev) {
			try {
				playMove(x,y);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void init() {
		
		setLayout(new GridBagLayout());
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				buttons[i][j] = new JButton();
				buttons[i][j].addActionListener(new ButtonListener(i,j));
				add(
					buttons[i][j],
					new GridBagConstraints(
					   i,
					   j,
					   1,
					   1,
					   1.0,
					   1.0,
					   GridBagConstraints.CENTER,
					   GridBagConstraints.BOTH,
					   new Insets(0, 0, 0, 0),
					   0,
					   0));
			}
		
		status = new JLabel("Connected, please wait...");
		
		add(
			status,
			new GridBagConstraints(
			   0,
			   3,
			   3,
			   1,
			   1.0,
			   0,
			   GridBagConstraints.CENTER,
			   GridBagConstraints.HORIZONTAL,
			   new Insets(0, 0, 0, 0),
			   0,
			   0));
	}
	
	
	synchronized public void playMove(int x, int y) throws RemoteException, InvalidTicketException, InvalidMoveException, AccessDeniedException {
		logger.log(Level.INFO, "TicTacToeMatchPanel", "Playing TTT move: " + x + ", " + y);
		if (playing) {
			if (state == STATE_MOVE) {
				matchInfo.getMatch().playMove(matchTicket, new TicTacToeMove(user, x, y));
				logger.log(Level.INFO, "TicTacToeMatchPanel", "... ttt move played.");
			} else {
				logger.log(Level.INFO, "TicTacToeMatchPanel", "... ttt move ignored (not your turn).");
			}
		} else {
			logger.log(Level.INFO, "TicTacToeMatchPanel", "... move ignored (you are a spectator).");
		}
	}
	
	synchronized public void leaveMatch() {
		try {
			logger.log(Level.INFO, "TicTacToeMatchPanel", "Leaving TTT match.");
			matchInfo.getMatch().leave(adapter);
			logger.log(Level.INFO, "TicTacToeMatchPanel", "... left TTT match.");
		} catch (RemoteException e) {
			logger.log(Level.WARNING, "TicTacToeMatchPanel", "... leaving match failed.");
		}
	}
	
	public boolean hasFirstTurn(long id) {
		return matchInfo.getPlayers()[0].getId() == id;
	}
	
	public void setState(int newState) {
		updateBoard();
		state = newState;
		switch (state) {
			case STATE_INITIALIZING:
				status.setText("Waiting for opponents to join");
				break;
				
			case STATE_WAIT:
				status.setText("Waiting for opponent to play");
				break;
				
			case STATE_MOVE:
				status.setText("Your turn");
				break;
				
			case STATE_MATCH_TIED:
				status.setText("Game over - tie");
				break;
				
			case STATE_FIRST_PLAYER_WON:
				status.setText("Game over - " + matchInfo.getPlayers()[0].getLogin() + " won");
				break;
				
			case STATE_SECOND_PLAYER_WON:
				status.setText("Game over - " + matchInfo.getPlayers()[1].getLogin() + " won");
				break;
				
			case STATE_MATCH_TERMINATED:
				status.setText("Game over - match abruptly terminated.");
				break;
		}
	}
	
	private void updateBoard() {
		for ( int i = 0; i < 3; i++)
			for ( int j = 0; j < 3; j++) {
				long field = board[i][j];
				if (field != EMPTY)  {
					if (hasFirstTurn(field)) {
						buttons[i][j].setText("X");
					} else {
						buttons[i][j].setText("O");
					}
				}
			}
	}
	
	synchronized public void matchOpened(MatchInfo match) {
	}
	
	synchronized public void matchStarted(MatchInfo match) {
		if (!playing) {
			setState(STATE_WAIT);
		} else if (hasFirstTurn(user.getId())) {
			setState(STATE_MOVE);
		} else {
			setState(STATE_WAIT);
		}
	}
	
	synchronized public void matchEnded(MatchInfo matchInfo) {
		User [][] ranks = null;
		ranks = matchInfo.getRanks();
		if (ranks == null) {
			//BUG. This should never happend.
			logger.log(Level.SEVERE, "TicTacToeMatchPanel", "BUG: Match ended with null ranks");
			setState(STATE_MATCH_TERMINATED);
		} else {
			if (ranks[0].length == 2) {
				setState(STATE_MATCH_TIED);
			} else if (hasFirstTurn(ranks[0][0].getId())) {
				setState(STATE_FIRST_PLAYER_WON);
			} else {
				setState(STATE_SECOND_PLAYER_WON);
			}
		}
	}
	
	synchronized public void matchTerminated(MatchInfo matchInfo) {
		setState(STATE_MATCH_TERMINATED);
	}
	
	synchronized public void movePlayed(MatchInfo match, Move m) {
		logger.log(Level.INFO, "TicTacToeMatchPanel", "Received TTT_MOVE.");
		TicTacToeMove move = (TicTacToeMove)m;
		board[move.getX()][move.getY()] = move.getPlayer().getId();
		if (playing) {
			if (user.equals(move.getPlayer())) {
				setState(STATE_WAIT);
			} else {
				setState(STATE_MOVE);
			}
		} else {
			setState(STATE_WAIT);
		}
	}
	
	public void ancestorAdded(AncestorEvent e) {
	}
	
	synchronized public void ancestorRemoved(AncestorEvent e) {
		leaveMatch();
	}
	
	public void ancestorMoved(AncestorEvent e) {
	}
}
