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
import org.globalse.arena.user.User;


/**
 * This class is an adapter wrapping around a local match listener so that it can be registered
 * as a remote match listeners. This adapter publishes an RMI interface and forwards
 * notification events from the server to the specified local listener. This removes the need
 * for local listeners to be remote objects.
 *
 * There are two reasons behind this class:
 * <UL><LI>Match front ends need only to implement LocalMatchListener and need not worry about
 *   RMI specific details</LI>.
 * <LI>Only this adapter needs to be in the game peer  and arena class path. Local listeners (which are
 *   usually game specific) need not. This allows new games to be added without having to install
 * them in every match front end and game peer.</LI>
 * </UL>
 *
 * @see RemoteMatchListener
 * @see LocalMatchListener
 * @see org.globalse.arena.remote.RemoteMatch#join
 * @see org.globalse.arena.remote.RemoteMatch#watch
 * @author Allen Dutoit
 */
public final class MatchListenerAdapter extends UnicastRemoteObject implements RemoteMatchListener {
	
	private LocalMatchListener localListener;
	
	public MatchListenerAdapter(LocalMatchListener localListener) throws RemoteException {
		super();
		this.localListener = localListener;
	}
	
	public void matchStarted(MatchInfo matchInfo) throws RemoteException {
		localListener.matchStarted(matchInfo);
	}
	
	public void matchEnded(MatchInfo matchInfo) throws RemoteException {
		localListener.matchEnded(matchInfo);
	}
	
	public void matchTerminated(MatchInfo matchInfo) throws RemoteException {
		localListener.matchTerminated(matchInfo);
	}
	
	public void movePlayed(MatchInfo matchInfo, Move move) throws RemoteException {
		localListener.movePlayed(matchInfo, move);
	}
	
}

