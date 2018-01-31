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

import java.util.*;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.Move;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.RemoteMatchListener;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

/**
 * A match is a contest between two or more players within the scope of a game.
 * The outcome of a match is a partial ranking of the players who competed in the
 * match. This class is the abstract superclass for all game-specific match implementation. See
 * {@link RemoteMatch} for the public methods remotely accessible on matches.
 *
 * Each concrete game (e.g., {@link org.globalse.arena.ttt.TicTacToe TicTacToe})
 * provides a concrete match class (e.g., TicTacToeMatch) to
 * track the progress of an ongoing match, to ensure the proper order of player moves, to define
 * the semantic of game move, and to return the player ranking after the match was completed.
 *
 * In turn, the abstract class provides event notification for updating match front ends,
 * takes care of bookkeeping with the round and tournament classes, and authorizes
 * of players. This class is implemented as a remote object so that it can be created
 * in a virtual machine (e.g., a {@link org.globalse.arena.gamepeer.GamePeer GamePeer})
 * that is different than the round to which
 * it belongs (e.g., the arena server). This class implements the RemoteMatch public
 * interface that the round and match front ends use to access the match.
 *
 * When implementing a concrete match class, the abstract methods playMove, getMoves, and
 * getRanks must be implemented. The method leave could also be extended by the subclass
 * if leaving the match
 *
 * @see Game
 * @see RemoteMatch
 * @see org.globalse.arena.ttt.TicTacToeMatch
 *
 * @author Allen Dutoit
 * @author Michael Nagel
 */
public abstract class Match extends UnicastRemoteObject implements RemoteMatch {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	// Map of players indexed by match tickets
	private Map matchTickets = new HashMap();
	
	// Seed for generating player tickets.
	private static Random random = new Random();
	
	// Attributes
	private String id;
    private String state = INITIALIZING;
	
	// Arena ticket used when firing arena listeners.
	private String matchOwnerTicket = null;
	
	// Associations
	private RemoteRound round = null;
    private List players = new ArrayList();
    private Statistics statistics = null;
	
	// Map of listeners indexed by player
	private Map playerListeners = new HashMap();
	
	// List of spectator listeners
	private List spectatorListeners = new ArrayList();
	
	// List of threads for notifying remote match listeners.
	private ArrayList threads = new ArrayList();
	
    public Match(RemoteRound round, User[] players) throws RemoteException {
		super();
		this.id = (new UID()).toString();
		this.round = round;
		for (int i = 0; i < players.length; i++) {
			this.players.add(players[i]);
		}
    }
	
	synchronized public MatchInfo getInfo() {
		return new MatchInfo(this);
    }
	
	synchronized public String getId() {
		return id;
	}
	
	synchronized public User[] getPlayers() {
		synchronized(players) {
			return (User[])players.toArray(new User[players.size()]);
		}
	}
	
	synchronized public boolean hasPlayer(User player) {
		return players.contains(player);
	}
	
	synchronized public String getState() {
		return state;
	}
	
	// Invoked by tournament
    synchronized public void open(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException {
		if (!round.hasAccess(ticket, AccessPolicy.MANAGE)) {
			throw new AccessDeniedException("Ticket " + ticket + " cannot open this match.");
		}
		if (!state.equals(INITIALIZING)) {
			throw new InvalidStateException("Can only open a match when it is being initialized.");
		}
		state = CONNECTING;
		matchOwnerTicket = ticket;
		fireMatchInfoChanged(matchOwnerTicket);
    }
	
	// Invoked by join
	synchronized protected void start() throws InvalidStateException {
		logger.fine("Starting match.");
		if (!state.equals(CONNECTING)) {
			throw new InvalidStateException("Can only start a match after it is opened and all players joined.");
		}
		state = PLAYING;
		fireMatchInfoChanged(matchOwnerTicket);
		fireMatchStarted();
	}
	
	synchronized protected void end() throws InvalidStateException {
		logger.fine("Ending match normally.");
		if (!state.equals(PLAYING)) {
			throw new InvalidStateException("Can only end a match that is being played.");
		}
		state = FINISHED;
		fireMatchEnded();
		fireMatchInfoChanged(matchOwnerTicket);
		logger.info("Match ended.");
	}
	
	// Invoked by tournament
    synchronized public void terminate(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		if (!round.hasAccess(ticket, AccessPolicy.MANAGE)) {
			throw new AccessDeniedException("Ticket " + ticket + " is not allowed to terminate this match.");
		}
		state = TERMINATED;
		fireMatchTerminated();
		fireMatchInfoChanged(matchOwnerTicket);
	}
	
	private String getMatchTicket(User player) {
		String playerTicket = Long.toHexString(random.nextLong());
		while (matchTickets.get(playerTicket) != null) {
			playerTicket = Long.toHexString(random.nextLong());
		}
		matchTickets.put(playerTicket, player);
		return playerTicket;
	}
	
	protected User getPlayerFromMatchTicket(String matchTicket) throws InvalidTicketException {
		return (User)matchTickets.get(matchTicket);
	}
	
	synchronized public String join(String ticket, RemoteMatchListener listener)
		throws RemoteException, InvalidStateException, InvalidTicketException, AccessDeniedException {
		User player = round.getPlayer(ticket);
		if (!state.equals(CONNECTING)) {
			throw new InvalidStateException("Can only join a match after it is opened and before it is started.");
		}
		if (!hasPlayer(player)) {
			throw new AccessDeniedException("Can only join a match as a player.");
		}
		// Throw an exception if the player has already joined.
		if (playerListeners.get(player) != null) {
			throw new InvalidStateException("Can only join a match once.");
		}
		
		playerListeners.put(player, listener);
		fireMatchInfoChanged(matchOwnerTicket);
		if (playerListeners.size() == players.size()) {
			start();
		}
		return getMatchTicket(player);
	}
	
	synchronized public Move[] watch(RemoteMatchListener listener) {
		if (state != FINISHED && !spectatorListeners.contains(listener)) {
			spectatorListeners.add(listener);
		}
		return getMoves();
	}
	
	protected abstract Move[] getMoves();
	
	synchronized public void leave(RemoteMatchListener listener) {
		logger.fine("Leaving match...");
		if (state == FINISHED) {
			logger.fine("...match already ended, nothing to do.");
			return;
		}
		if (spectatorListeners.contains(listener)) {
			logger.fine("...found spectator.");
			spectatorListeners.remove(listener);
		} else {
			User player = null;
			for (Iterator i = playerListeners.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				if (entry.getValue().equals(listener)) {
					player = (User)entry.getKey();
					break;
				}
			}
			if (player != null) {
				logger.fine("...found player.");
				playerListeners.remove(player);
				for (Iterator i = matchTickets.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					if (entry.getValue().equals(player)) {
						matchTickets.remove(entry.getKey());
						break;
					}
				}
				fireMatchInfoChanged(matchOwnerTicket);
			} else {
				logger.warning("...leaving match found nobody.");
			}
		}
	}
	
	synchronized public User [] getConnectedPlayers() {
		Set players = playerListeners.keySet();
		return (User[])players.toArray(new User[players.size()]);
	}
	
	protected RemoteMatchListener [] getPlayerListeners() {
		return (RemoteMatchListener[])playerListeners.values().toArray(new RemoteMatchListener[playerListeners.size()]);
	}
	
	protected RemoteMatchListener [] getSpectatorListeners() {
		return (RemoteMatchListener[])spectatorListeners.toArray(new RemoteMatchListener[playerListeners.size()]);
	}
	
	protected abstract class MatchNotifierThread extends Thread {
		protected MatchInfo info = null;
		protected Move move = null;
		
		protected MatchNotifierThread(MatchInfo info) {
			this(info, null);
		}
		
		protected MatchNotifierThread(MatchInfo info, Move move) {
			this.info = info;
			this.move = move;
			queue();
		}
		
		private void queue() {
			if (threads == null) {
				throw new NullPointerException("Cannot queue a notifier threads on a null list.");
			}
			synchronized(threads) {
				threads.add(this);
				if (threads.size() == 1) {
					start();
				}
			}
			
		}
		
		private void startNextThread() {
			synchronized(threads) {
				threads.remove(this);
				if (threads.size() > 0) {
					((Thread)threads.get(0)).start();
				}
			}
		}
		
		abstract public void notify(RemoteMatchListener listener) throws RemoteException;
		
		public void run() {
			RemoteMatchListener[] listeners = null;
			listeners = getPlayerListeners();
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					try {
						notify(listeners[i]);
					} catch (RemoteException e) {
						logger.warning("Running player listeners: got exception.");
						e.printStackTrace();
						leave(listeners[i]);
					}
				}
			}
			startNextThread();
			listeners = getSpectatorListeners();
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					try {
						notify(listeners[i]);
					} catch (RemoteException e) {
						logger.warning("Running spectator listeners: got exception.");
						e.printStackTrace();
						leave(listeners[i]);
					}
				}
			}
		}
	}
	
	protected void fireMatchInfoChanged(final String ticket) {
		// Fire the event in a thread to avoid deadlocks with the round remote object.
		final MatchInfo info = new MatchInfo(this);
		new Thread() {
			public void run() {
				try {
					round.fireMatchInfoChanged(ticket, info);
				} catch (Exception e) {
					logger.warning("Error during firing of match info for arena listeners.");
				}
			}
		}.start();
	}
	
	protected void fireMatchStarted() {
		new MatchNotifierThread(new MatchInfo(this)) {
			public void notify(RemoteMatchListener listener) throws RemoteException {
				listener.matchStarted(info);
			}
		};
	}
	
	protected void fireMatchEnded() {
		new MatchNotifierThread(new MatchInfo(this)) {
			public void notify(RemoteMatchListener listener) throws RemoteException {
				listener.matchEnded(info);
			}
			public void run() {
				// After notifying all listeners about the end of the match, clear
				// the listeners since there will be no more events. This releases
				// resources used by the remote listeners.
				super.run();
				playerListeners.clear();
				spectatorListeners.clear();
				logger.fine("Finished fire match ended, dropped listeners.");
			}
		};
	}
	
	protected void fireMatchTerminated() {
		new MatchNotifierThread(new MatchInfo(this)) {
			public void notify(RemoteMatchListener listener) throws RemoteException {
				listener.matchTerminated(info);
			}
			public void run() {
				// After notifying all listeners about the end of the match, clear
				// the listeners since there will be no more events. This releases
				// resources used by the remote listeners.
				super.run();
				playerListeners.clear();
				spectatorListeners.clear();
				logger.fine("Finished fire match terminated, dropped listeners.");
			}
		};
	}
//	public void updateStats(User p, String name, double value) {
//	}
	
}

