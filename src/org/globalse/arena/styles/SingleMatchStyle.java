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
package org.globalse.arena.styles;

import java.rmi.RemoteException;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.server.Game;
import org.globalse.arena.server.GamePeerManager;
import org.globalse.arena.server.Round;
import org.globalse.arena.server.Tournament;
import org.globalse.arena.server.TournamentStyle;
import org.globalse.arena.user.User;

public class SingleMatchStyle implements TournamentStyle {
	
	private static TournamentStyle instance = new SingleMatchStyle();
	public static TournamentStyle getInstance() {
		return instance;
	}
	
	private SingleMatchStyle() {}
	
	public boolean isNumPlayersLegal(Tournament tournament, int number) {
		Game game = tournament.getLeague().getGame();
		if (number >= game.getMinPlayersPerMatch() &&
		   number <= game.getMaxPlayersPerMatch()) {
			return true;
		}
		return false;
	}
	
	
    public boolean isTournamentFinished(Tournament tournament) {
		Round [] rounds = tournament.getRounds();
		return rounds.length > 0 && rounds[0].isCompleted();
    }
	
    public Round planRounds(Tournament tournament) throws RemoteException {
		Round round = new SingleRound(tournament);
		RemoteMatch match = GamePeerManager.getInstance().createMatch(round, tournament.getAcceptedPlayers());
		round.addMatch(match);
		round.plan();
		return round;
    }
	
	public User[][]getRanks(Tournament tournament) {
		Round[]rounds = tournament.getRounds();
		if (rounds.length == 0) return null;
		User[][] ranks = null;
		try {
			rounds[0].getMatches()[0].getRanks();
		} catch (RemoteException e) {} catch (InvalidStateException e) {}
		return ranks;
	}
	
	private class SingleRound extends Round {
		SingleRound(Tournament tournament) throws RemoteException {
			super(tournament);
		}
		public void plan() {
			setPlanned();
		}
	}
}
