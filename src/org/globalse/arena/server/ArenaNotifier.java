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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteArenaListener;
import org.globalse.arena.remote.RoundInfo;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

/**
 * This class is responsible for managing arena listeners and the threads associated
 * with broadcasting state changes to the arena listeners (e.g., state changes of
 * leagues, tournaments, rounds, and matches). Note that game moves are not broadcast over
 * arena listeners, they are handled instead by the RemoteMatch and RemoteMatchListener
 * interfaces.
 *
 * This class allocates a thread and an event queue for each active game. Leagues,
 * tournaments, rounds, and matches queue events by invoking the fire methods. When
 * the queue is not empty, the notifier thread assocaited with the game
 * finds the relevant listeners associated with the object of interest, checks
 * the access of the user associated with the listener, and invokes the appropriate
 * listener method. If the call to the listener fails (e.g., network failure or
 * match front end crashed), the listener is removed from the arena. The thread then
 * moves on to the next listener.
 *
 * When the event queue for a game is empty, the thread ends to minimize the number
 * of active threads (for example, many games might not be active for a long time).
 * A new thread will be created when the first event for the game is queued.
 *
 * Synchronization between producer and consumer threads is achieved with a
 * lock on the eventQueues object.
 *
 * This class is package protected so that only Arena, League, Tournament, Round, and
 * Match access this class.
 *
 * @author Allen Dutoit
 */
class ArenaNotifier {
	
	// Map of game listeners indexed by games. Game listeners are in turn also
	// a map of user listeners indexed by user. User listeners are a list of
	// remote arena listeners.
	private Map listeners = new HashMap();
	
	// Map of notifier threads indexed by Game
	private Map threads = new HashMap();
	
	// Map of event queues indexed by Game
	private Map eventQueues = new HashMap();
	
	public void addListener(Game game, User user, RemoteArenaListener listener) {
		synchronized(listeners) {
			List userListeners = getListenersByGameAndUser(game, user);
			if (!userListeners.contains(listener)) {
				userListeners.add(listener);
			}
		}
	}
	
	public void removeListener(Game game, RemoteArenaListener listener) {
		synchronized(listeners) {
			Map gameListeners = getListenersByGame(game);
			for (Iterator i = gameListeners.values().iterator(); i.hasNext();) {
				List userListeners = (List)i.next();
				if (userListeners.contains(listener)) {
					// Do not break out on the first match: the same listener could be
					// registered for different games and users
					userListeners.remove(listener);
				}
			}
		}
	}
	
	private Map getListenersByGame(Game game) {
		Map gameListeners = (Map)listeners.get(game);
		if (gameListeners == null) {
			gameListeners = new HashMap();
			listeners.put(game, gameListeners);
		}
		return gameListeners;
	}
	
	private List getListenersByGameAndUser(Game game, User user) {
		Map gameListeners = getListenersByGame(game);
		List userListeners = (List)gameListeners.get(user);
		if (userListeners == null) {
			userListeners = new ArrayList();
			gameListeners.put(user, userListeners);
		}
		return userListeners;
	}
	
	private RemoteArenaListener [] getListeners(League league) {
		List result = new ArrayList();
		synchronized(listeners) {
			Game game = league.getGame();
			Map gameListeners = getListenersByGame(game);
			for (Iterator i = gameListeners.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				User user = (User)entry.getKey();
				if (Arena.getInstance().hasLeagueAccess(user, league, AccessPolicy.READ)) {
					result.addAll((List)entry.getValue());
				}
			}
		}
		return (RemoteArenaListener[])result.toArray(new RemoteArenaListener[result.size()]);
	}
	
	private RemoteArenaListener [] getListeners(Tournament tournament) {
		List result = new ArrayList();
		synchronized(listeners) {
			Game game = tournament.getLeague().getGame();
			Map gameListeners = getListenersByGame(game);
			for (Iterator i = gameListeners.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				User user = (User)entry.getKey();
				if (Arena.getInstance().hasTournamentAccess(user, tournament, AccessPolicy.READ)) {
					result.addAll((List)entry.getValue());
				}
			}
		}
		return (RemoteArenaListener[])result.toArray(new RemoteArenaListener[result.size()]);
	}
	
	private abstract class ArenaEvent {
		private League league;
		private Tournament tournament;
		ArenaEvent(League league, Tournament tournament) {
			if (league == null && tournament == null) {
				throw new IllegalArgumentException("League and tournament cannot be both null.");
			}
			this.league = league;
			this.tournament = tournament;
		}
		ArenaEvent(League league) {
			this(league, null);
		}
		ArenaEvent(Tournament tournament) {
			this(null, tournament);
		}
		Tournament getTournament() {
			return tournament;
		}
		League getLeague() {
			return league;
		}
		abstract void notify(RemoteArenaListener listener) throws RemoteException;
	}
	
	private class NotifierThread extends Thread {
		private Game game;
		NotifierThread(Game game) {
			this.game = game;
		}
		
		public void run() {
			ArenaEvent event = null;
			RemoteArenaListener [] listeners;
			
			while (true) {
				
				// Get the next event in the game queue. If there are no such
				// events, remove this thread from the threads map and terminate.
				// Both actions need to be in the same synchronized block to prevent
				// that a thread terminates while a new event is queued.
				synchronized(eventQueues) {
					event = nextEvent(game);
					if (event == null) {
						threads.remove(game);
						break;
					}
				}
				
				// An event was successfully dequeued. Get the applicable listeners
				// depending on whether it is a league or a tournament event. The
				// getListeners method also checks access.
				Tournament tournament = event.getTournament();
				if (tournament != null) {
					listeners = getListeners(tournament);
				} else {
					listeners = getListeners(event.getLeague());
				}
				
				// For each listener, send the event using the event specific
				// notification method. If the notification fails for a listener,
				// remove the listener from the arena to avoid future such exceptions.
				for (int i = 0; i < listeners.length; i++) {
					try {
						event.notify(listeners[i]);
					} catch (Exception e) {
						removeListener(game, listeners[i]);
					}
				}
			}
		}
	}
	
	private void queueEvent(Game game, ArenaEvent event) {
		synchronized(eventQueues) {
			
			// Find the event queue applicable to this game and create one if
			// necessary.
			List eventQueue = (List)eventQueues.get(game);
			if (eventQueue == null) {
				eventQueue = new ArrayList();
				eventQueues.put(game, eventQueue);
			}
			eventQueue.add(event);
			
			// Check if there is a thread for notifying events in this game. If
			// not, create one.
			NotifierThread thread = (NotifierThread)threads.get(game);
			if (thread == null) {
				thread = new NotifierThread(game);
				threads.put(game, thread);
				thread.start();
			}
		}
	}
	
	private ArenaEvent nextEvent(Game game) {
		ArenaEvent result = null;
		synchronized(eventQueues) {
			List eventQueue = (List)eventQueues.get(game);
			if (eventQueue != null && eventQueue.size() > 0) {
				result = (ArenaEvent)eventQueue.get(0);
				eventQueue.remove(result);
			}
		}
		return result;
	}
	
	void fireLeagueInfoChanged(final League league) {
		ArenaEvent event = new ArenaEvent(league) {
			private LeagueInfo leagueInfo = new LeagueInfo(league);
			void notify(RemoteArenaListener listener) throws RemoteException {
				listener.leagueInfoChanged(leagueInfo);
			}
		};
		queueEvent(league.getGame(), event);
	}
	
	void fireTournamentCreated(final Tournament tournament) {
		ArenaEvent event = new ArenaEvent(tournament) {
			private TournamentInfo tournamentInfo = new TournamentInfo(tournament);
			void notify(RemoteArenaListener listener) throws RemoteException {
				listener.tournamentCreated(tournamentInfo);
			}
		};
		queueEvent(tournament.getLeague().getGame(), event);
	}
	
	void fireTournamentInfoChanged(final Tournament tournament) {
		ArenaEvent event = new ArenaEvent(tournament) {
			private TournamentInfo tournamentInfo = new TournamentInfo(tournament);
			void notify(RemoteArenaListener listener) throws RemoteException {
				listener.tournamentInfoChanged(tournamentInfo);
			}
		};
		queueEvent(tournament.getLeague().getGame(), event);
	}
	
	void fireRoundCreated(final Round round) {
		Tournament tournament =  round.getTournament();
		ArenaEvent event = new ArenaEvent(tournament) {
			private RoundInfo roundInfo = new RoundInfo(round);
			void notify(RemoteArenaListener listener) throws RemoteException {
				listener.roundCreated(roundInfo);
			}
		};
		queueEvent(tournament.getLeague().getGame(), event);
	}
	
	void fireMatchInfoChanged(Tournament tournament, final MatchInfo info) {
		ArenaEvent event = new ArenaEvent(tournament) {
			private MatchInfo matchInfo = info;
			void notify(RemoteArenaListener listener) throws RemoteException {
				listener.matchInfoChanged(matchInfo);
			}
		};
		queueEvent(tournament.getLeague().getGame(), event);
	}
}

