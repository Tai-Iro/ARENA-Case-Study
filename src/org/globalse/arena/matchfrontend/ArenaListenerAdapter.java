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
package org.globalse.arena.matchfrontend;

import org.globalse.arena.remote.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

/**
 * This class is an adapter wrapping around a local arena listener so that it can
 * be registered with an arena as a remote arena listener. The adapter publishes an RMI
 * interface and forwards remote notification events from the server to the
 * specified local listener. This removes the need for local arena listeners to be remote
 * objects themselves.
 *
 * There are two reasons behind this class:
 * <UL>
 * <LI>Match front ends need only to implement <code>LocalArenaListener</code> and need not worry about
 *   RMI details.</LI>
 * <LI>Only this adapter needs to be in the arena server class path. Local listeners
 *   (which are often game specific) need not. This allows new games to be added without
 *   having to be installed with every match front end.</LI>
 * </UL>
 *
 * @see RemoteArenaListener
 * @see LocalArenaListener
 * @see org.globalse.arena.remote.RemoteArena#addListener
 * @author Allen Dutoit
 */
public final class ArenaListenerAdapter extends UnicastRemoteObject implements RemoteArenaListener {
	
	private LocalArenaListener localListener;
	
	public ArenaListenerAdapter(LocalArenaListener localListener) throws RemoteException {
		super();
		this.localListener = localListener;
	}
	
	public void leagueInfoChanged(LeagueInfo league) throws RemoteException {
		localListener.leagueInfoChanged(league);
	}
	
	public void tournamentCreated(TournamentInfo tournament) throws RemoteException {
		localListener.tournamentCreated(tournament);
	}
	
	public void tournamentInfoChanged(TournamentInfo tournament) throws RemoteException {
		localListener.tournamentInfoChanged(tournament);
	}
	
	public void roundCreated(RoundInfo roundInfo) throws RemoteException {
		localListener.roundCreated(roundInfo);
	}
	
	public void matchInfoChanged(MatchInfo matchInfo) throws RemoteException {
		localListener.matchInfoChanged(matchInfo);
	}
	
}

