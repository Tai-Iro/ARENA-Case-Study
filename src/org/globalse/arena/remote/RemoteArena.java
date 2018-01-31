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
package org.globalse.arena.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Level;
import org.globalse.arena.gamepeer.RemoteGamePeer;
import org.globalse.arena.remote.MatchPanelFactory;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.GamePeerAlreadyRegisteredException;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.MatchNotFoundException;
import org.globalse.arena.remote.exceptions.TournamentStyleNotFoundException;
import org.globalse.arena.user.User;

/**
 * This interface is a facade for accessing the top-level objects of an arena,
 * including the leagues, tournament styles, games, and users. A RemoteArena
 * is typically the only remote object that is bound in the registry. Clients
 * get remote references to other objects by first accessing a <code>RemoteArena</code>.
 *
 * A client usually accesses a RemoteArena following these steps:
 * <UL>
 * <LI>Get a remote reference to the remote arena using {@link java.rmi.Naming#lookup}</LI>
 * <LI>Authenticate with the remote arena using {@link RemoteArena#login}; the login method will return a ticket</LI>
 * <LI>Register an <code>RemoteArenaListener</code> to receive event notifications about changes for a specific game.</LI>
 * <LI>Get the remote objects of interest, for example, with {@link RemoteArena#getMatchById} or {@link RemoteArena#getLeagueInfosByGame}</LI>
 * <LI>Invoke the appropriate remote method on the resulting object</LI>
 * </UL>
 *
 * For examples of how to access a <code>RemoteArena</code>, see {@link org.globalse.arena.matchfrontend.MatchFrontEnd}.
 *
 * @see RemoteArenaListener
 *
 * @author Michael Nagel
 * @author Allen Dutoit
 */
public interface RemoteArena extends Remote {
	
	public User getOperator()
		throws RemoteException;
	
	public String login(String username, String password)
		throws RemoteException, InvalidLoginException;
	
	public String getGuestTicket()
		throws RemoteException;
	
	public User getUser(String ticket)
		throws RemoteException, InvalidTicketException;
	
	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException;
	
	public GameDescriptor [] getGameInfos(String ticket)
		throws RemoteException, InvalidTicketException;
	
	public String [] getTournamentStyleNames(String ticket)
		throws RemoteException, InvalidTicketException;
	
    public LeagueInfo [] getLeagueInfos(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	public LeagueInfo [] getLeagueInfosByGame(String ticket, String gameName)
		throws RemoteException, InvalidTicketException, GameNotFoundException;
	
	public LeagueInfo createLeague(String ticket, User owner, String name, String description, String gameName, String styleName)
		throws RemoteException, InvalidTicketException, AccessDeniedException, GameNotFoundException, TournamentStyleNotFoundException;
	
	public void addListener(String ticket, String gameName, RemoteArenaListener listener)
		throws RemoteException, InvalidTicketException, GameNotFoundException;
	
	public void removeListener(String gameName, RemoteArenaListener listener)
		throws RemoteException, InvalidTicketException, GameNotFoundException;

	// Method used by game peers to make themselves avaiable to an arena.
	public void registerGamePeer(String arenaTicket, RemoteGamePeer gamePeer, String peerTicket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, GamePeerAlreadyRegisteredException;

	// Methods used by match front ends to get matches by id and to get the match panel factory.
	
	public MatchInfo getMatchById(String ticket, String matchId)
		throws RemoteException, InvalidTicketException, AccessDeniedException, MatchNotFoundException;

	public MatchPanelFactory getMatchPanelFactory(String ticket, String gameName)
		throws RemoteException, InvalidTicketException, AccessDeniedException, GameNotFoundException;
	
	// Utility method used by RemoteLogger for logging. Match frontends should use RemoteLogger.
	public void log(Level level, String className, String message)
		throws RemoteException;
	
}
