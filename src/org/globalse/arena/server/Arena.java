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
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.globalse.arena.gamepeer.RemoteGamePeer;
import org.globalse.arena.remote.GameDescriptor;
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.MatchPanelFactory;
import org.globalse.arena.remote.RemoteArena;
import org.globalse.arena.remote.RemoteArenaListener;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.GameAlreadyExistsException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.GamePeerAlreadyRegisteredException;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.MatchNotFoundException;
import org.globalse.arena.remote.exceptions.TournamentStyleNotFoundException;
import org.globalse.arena.remote.exceptions.UserAlreadyExistsException;
import org.globalse.arena.server.GamePeerManager;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.DefaultAccessPolicy;
import org.globalse.arena.user.GateKeeper;
import org.globalse.arena.user.User;

/**
 */
public class Arena extends UnicastRemoteObject implements RemoteArena  {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	// Arena is a singelton.
	private static Arena instance = null;
	
	public static void init() throws RemoteException {
		if (instance == null) {
			instance = new Arena();
		}
	}
	
	public static Arena getInstance() {
		return instance;
	}
	
	// The arena operator is the only user who can create leagues.
	private User operator;
	
	// The gate keeper keeps track of the users allowed to log into the arena
	// and authenticates users upon login.
	private GateKeeper gateKeeper = new GateKeeper();
	
	// The access policy authorizes logged in users for each remote method
	// invoked in the arena or related League or Tournament.
	private AccessPolicy policy = new DefaultAccessPolicy();
	
	// Map of styles registered in this arena, indexed by style name
	private Map styles = new HashMap();
    
	// List of active leagues
	private List leagues = new ArrayList();
	
	// Listeners and listener threads
	private ArenaNotifier notifier = new ArenaNotifier();
	
	// Constructor is private to ensure that callers use the getInstance method instead
    private Arena() throws RemoteException {
		super();
    }
	
	////////////////////////////////////////////////////////////////////////////
	// Authentication - All authentication is delegated to the gate keeper, which
	// is not visible to classes outside arena.
	
    public String login(String username, String password) throws RemoteException, InvalidLoginException {
		return gateKeeper.login(username, password);
    }
	
	public String getGuestTicket() throws RemoteException {
		return gateKeeper.getGuestTicket();
	}
	
	public boolean isUserGuest(User user) {
		return gateKeeper.isUserGuest(user);
	}
	
	public User createUser(String username, String password) throws UserAlreadyExistsException {
		return gateKeeper.createUser(username, password);
	}
	
	public User getUser(String ticket) throws RemoteException, InvalidTicketException {
		return gateKeeper.getUser(ticket);
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Access control
	
	void setAccessPolicy(AccessPolicy policy) {
		this.policy = policy;
	}
	
	public User getOperator() {
		return operator;
	}
	
	void setOperator(User operator) {
		this.operator = operator;
	}
	
	private void checkArenaAccess(User user, String access)
		throws AccessDeniedException {
		if (!policy.hasArenaAccess(user, access)) {
			throw new AccessDeniedException("User " + user.getLogin()
												+ " is not allowed to " + access + " the arena.");
		}
	}
	
	public boolean hasAccess(String ticket, String access) throws RemoteException, InvalidTicketException {
		User user = getUser(ticket);
		return hasAccess(user, access);
	}
	
	boolean hasAccess(User user, String access) {
		return policy.hasArenaAccess(user, access);
	}
	
	boolean hasLeagueAccess(String ticket, League league, String access) throws RemoteException, InvalidTicketException {
		User user = getUser(ticket);
		return hasLeagueAccess(user, league, access);
	}
	
	boolean hasLeagueAccess(User user, League league, String access) {
		return policy.hasLeagueAccess(user, league, access);
	}
	
	boolean hasTournamentAccess(String ticket, Tournament tournament, String access)
		throws RemoteException, InvalidTicketException {
		User user = getUser(ticket);
		return hasTournamentAccess(user, tournament, access);
	}
	
	boolean hasTournamentAccess(User user, Tournament tournament, String access) {
		return policy.hasTournamentAccess(user, tournament, access);
	}
	
    ////////////////////////////////////////////////////////////////////////////
	// Methods for accessing top level objects (styles, games, leagues)
	
	public String [] getTournamentStyleNames(String ticket) throws RemoteException {
		List result = new ArrayList();
		synchronized(styles) {
			for (Iterator i = styles.keySet().iterator(); i.hasNext();) {
				result.add(i.next());
			}
		}
		return (String[])result.toArray(new String[result.size()]);
	}
	
	TournamentStyle getTournamentStyleByName(String name)
		throws TournamentStyleNotFoundException {
		TournamentStyle style = null;
		synchronized(styles) {
			style = (TournamentStyle)styles.get(name);
		}
		if (style == null) {
			throw new TournamentStyleNotFoundException("Style " + name + " not found.");
		}
		return style;
	}
	
	public String getTournamentStyleName(TournamentStyle style)
		throws TournamentStyleNotFoundException {
		synchronized(styles) {
			for (Iterator i = styles.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				if (entry.getValue() == style) {
					return (String)entry.getKey();
				}
			}
		}
		throw new TournamentStyleNotFoundException("Tournament style not registered in arena.");
	}
	
	public void registerTournamentStyle(String name, TournamentStyle style) {
		synchronized(styles) {
			styles.put(name, style);
		}
	}
	
	public GameDescriptor[] getGameInfos(String ticket) throws RemoteException {
		return GameManager.getInstance().getGameInfos();
	}
	
	public Game getGameByName(String gameName) throws GameNotFoundException {
		return GameManager.getInstance().getGameByName(gameName);
	}
	
	public String getGameName(Game game) throws GameNotFoundException {
		return GameManager.getInstance().getGameName(game);
	}
	
	public String getGameDescription(Game game) throws GameNotFoundException {
		return GameManager.getInstance().getGameDescription(game);
	}
	
	public void registerGame(Game game, String name, String description, MatchPanelFactory panelFactory) throws GameAlreadyExistsException {
		GameManager.getInstance().registerGame(game, name, description, panelFactory);
	}
	
	public void registerGamePeer(String ticket, RemoteGamePeer peer, String peerTicket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, GamePeerAlreadyRegisteredException {
		User user = getUser(ticket);
		checkArenaAccess(user, AccessPolicy.MANAGE);
		GamePeerManager.getInstance().registerGamePeer(peer, peerTicket);
	}
	
	public LeagueInfo[] getLeagueInfos(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException {
		User user = getUser(ticket);
		List result = new ArrayList();
		synchronized(leagues) {
			for (Iterator i = leagues.iterator(); i.hasNext();) {
				League league = (League)i.next();
				if (policy.hasLeagueAccess(user, league, AccessPolicy.READ)) {
					result.add(new LeagueInfo(league));
				}
			}
		}
		return (LeagueInfo[])result.toArray(new LeagueInfo[result.size()]);
	}
	
	public LeagueInfo [] getLeagueInfosByGame(String ticket, String gameName)
		throws RemoteException, InvalidTicketException, GameNotFoundException {
		User user = getUser(ticket);
		// This triggers an ElementNotFound exception when the game name is not valid.
		Game game = getGameByName(gameName);
		List result = new ArrayList();
		synchronized(leagues) {
			for (Iterator i = leagues.iterator(); i.hasNext();) {
				League league = (League)i.next();
				if (league.getGame() == game) {
					if (policy.hasLeagueAccess(user, league, AccessPolicy.READ)) {
						result.add(new LeagueInfo(league));
					}
				}
			}
		}
		return (LeagueInfo[])result.toArray(new LeagueInfo[result.size()]);
	}
	
	void addLeague(League league) {
		synchronized(leagues) {
			if (!leagues.contains(league)) {
				leagues.add(league);
			}
		}
	}
	
	void removeLeague(League league) {
		synchronized(leagues) {
			if (leagues.contains(league)) {
				leagues.remove(league);
			}
		}
	}
	
	public LeagueInfo createLeague(String ticket, User owner, String name, String description, String gameName, String styleName)
		throws RemoteException, InvalidTicketException, AccessDeniedException, GameNotFoundException, TournamentStyleNotFoundException {
		User user = getUser(ticket);
		Game game = getGameByName(gameName);
		TournamentStyle style = getTournamentStyleByName(styleName);
		checkArenaAccess(user, AccessPolicy.MANAGE);
		League league = new League(owner, game, style, name, description);
		return new LeagueInfo(league);
	}
	////////////////////////////////////////////////////////////////////////////
	// Match and match panel lookup
	
	public MatchInfo getMatchById(String ticket, String matchId) throws AccessDeniedException, InvalidTicketException, RemoteException, MatchNotFoundException  {
		// TODO: This should be stricter
		checkArenaAccess(getUser(ticket), AccessPolicy.READ);
		return GamePeerManager.getInstance().getMatchById(matchId);
	}
	
	public MatchPanelFactory getMatchPanelFactory(String ticket, String gameName) throws AccessDeniedException, InvalidTicketException, RemoteException, GameNotFoundException  {
		// TODO: This should be stricter
		checkArenaAccess(getUser(ticket), AccessPolicy.READ);
		return GameManager.getInstance().getMatchPanelFactory(gameName);
	}
	////////////////////////////////////////////////////////////////////////////
    // Methods for managing arena listeners.
	
	ArenaNotifier getNotifier() {
		return notifier;
	}
	
	public void addListener(String ticket, String gameName, RemoteArenaListener listener)
		throws RemoteException, InvalidTicketException, GameNotFoundException {
		User user = getUser(ticket);
		Game game = getGameByName(gameName);
		notifier.addListener(game, user, listener);
	}
	
	public void removeListener(String gameName, RemoteArenaListener listener)
		throws RemoteException, GameNotFoundException {
		Game game = getGameByName(gameName);
		notifier.removeListener(game, listener);
	}
	
	////////////////////////////////////////////////////////////////////////////
	// Method for remote logging by match front ends
	public void log(Level level, String className, String message) {
		String hostname = null;
		try {
			hostname = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			hostname = "localhost";
		}
		logger.log(level, "Class " + className + " on " + hostname + " logs \"" + message + "\".");
	}
	
}
