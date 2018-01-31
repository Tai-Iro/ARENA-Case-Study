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
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.user.User;

public interface TournamentStyle {
	
	public boolean isNumPlayersLegal(Tournament tournament, int number);

    public Round planRounds(Tournament tournament) throws RemoteException;
	
    public boolean isTournamentFinished(Tournament tournament);

	public User[][] getRanks(Tournament tournament) throws InvalidStateException;
	
}
