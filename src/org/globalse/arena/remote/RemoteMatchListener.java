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

/**
 * Interface for remote match listeners, to receive notification events about changes in
 * matches. Note that only match start, end, termination, and game moves are sent to
 * remote match listeners. Other changes of state are sent to the remote arena listeners
 * associated to the game.
 *
 * The RemoteMatchListener interface is intended for MatchPanels to receive notification
 * events about the match they display. However, MatchPanels do not usually do not implement
 * this interface directly. Instead, MatchPanels implement the LocalMatchListener
 * interface, then create a MatchListenerAdapter, and register the adapter
 * with the RemoteMatch. This removes the need for game-specific match panels to be in
 * the class path of the arena server and allows game-specific classes to be remotely
 * loaded from a codebase.
 *
 * @author Allen Dutoit
 */
public interface RemoteMatchListener extends Remote {

	public void matchStarted(MatchInfo match) throws RemoteException;
	
	public void matchEnded(MatchInfo match) throws RemoteException;
	
	public void matchTerminated(MatchInfo match) throws RemoteException;
	
	public void movePlayed(MatchInfo match, Move move) throws RemoteException;
}

