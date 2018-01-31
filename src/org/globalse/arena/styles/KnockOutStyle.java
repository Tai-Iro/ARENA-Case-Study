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

import org.globalse.arena.server.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.user.User;

public class KnockOutStyle implements TournamentStyle {
	
	private static TournamentStyle instance = new KnockOutStyle();
	public static TournamentStyle getInstance() {
		return instance;
	}
	
	private KnockOutStyle() {}
	
	public boolean isNumPlayersLegal(Tournament tournament, int number) {
		Game game = tournament.getLeague().getGame();
		if (number >= game.getMinPlayersPerMatch()) {
			return true;
		}
		return false;
	}
	
	// A knock out tournament is completed when there are fewer players left than
	// players per match.
    public boolean isTournamentFinished(Tournament tournament) {
		KnockOutRound lastRound = (KnockOutRound)tournament.getCurrentRound();
		Game game = tournament.getLeague().getGame();
		return lastRound.getWinners().length < game.getMinPlayersPerMatch();
    }
	
	public Round planRounds(Tournament tournament) throws RemoteException {
		KnockOutRound round = new KnockOutRound(tournament);
		round.plan();
		return round;
	}
	
	public User[][] getRanks(Tournament tournament) throws InvalidStateException {
		Round[] rounds = tournament.getRounds();
		List ranks = new ArrayList();
		List players = new ArrayList();
		
		// Add the winners of the last round
		RemoteMatch [] lastMatches = rounds[rounds.length-1].getMatches();
		for (int m = 0; m < lastMatches.length; m++) {
			User[][] matchRanks = null;
			try {
				matchRanks = lastMatches[m].getRanks();
			} catch (RemoteException e) {}
			if (matchRanks != null && matchRanks.length > 0) {
				for (int p = 0; p < matchRanks[0].length; p++) {
					players.add(matchRanks[0][p]);
				}
			}
		}
		ranks.add(players.toArray(new User[players.size()]));
		// Add the loosers of all the other rounds
		players.clear();
		for (int i = 1, j = rounds.length-1; j >= 0; j--) {
			RemoteMatch [] matches = rounds[j].getMatches();
			for (int m = 0; m <matches.length; m++) {
				User [][] matchRanks = null;
				try {
					matchRanks = matches[m].getRanks();
				} catch (RemoteException e) {}
				if (matchRanks != null) {
					for (int r = 1; r < matchRanks.length; r++) {
						for (int p = 0; p < matchRanks[r].length; p++) {
							players.add(matchRanks[r][p]);
						}
					}
				}
			}
			if (players.size() > 0) {
				ranks.add(players.toArray(new User[players.size()]));
				players.clear();
			}
		}
		return (User[][])ranks.toArray(new User[ranks.size()][]);
	}
	
	private class KnockOutRound extends Round {
		
		KnockOutRound(Tournament tournament) throws RemoteException {
			super(tournament);
		}
		KnockOutRound(KnockOutRound previous) throws RemoteException {
			super(previous);
		}
		
		User[] getWinners() {
			List players = new ArrayList();
			MatchInfo [] matchInfos = getMatchInfos();
			for (int i = 0; i < matchInfos.length; i++) {
				User[] winners = matchInfos[i].getRanks()[0];
				for (int j = 0; j < winners.length; j++) {
					players.add(winners[j]);
				}
			}
			User [] byes = getByes();
			for (int i = 0; i < byes.length; i++) {
				players.add(byes[i]);
				
			}
			return (User[])players.toArray(new User[players.size()]);
		}
		
		public Round getNextRound() throws IllegalStateException {
			if (!isCompleted()) {
				throw new IllegalStateException("Current round not completed.");
			}
			Round nextRound = super.getNextRound();
			if (nextRound == null) {
				if (!isTournamentFinished(getTournament())) {
					try {
					nextRound = new KnockOutRound(this);
					} catch (Exception e) {
						// TODO: BUG. should never happen.
					}
				}
			}
			setNextRound(nextRound);
			return nextRound;
		}
		public void plan() throws IllegalStateException {
			KnockOutRound previous = (KnockOutRound)getPreviousRound();
			if (isPlanned()) return;
			
			User [] players = null;
			Tournament tournament = getTournament();
			League league = tournament.getLeague();
			Game game = league.getGame();
			// Collect the winners of the previous round
			if (previous != null) {
				if (!previous.isCompleted()) {
					throw new IllegalStateException("This round cannot be planned until the previous one is completed.");
				}
				players = previous.getWinners();
			} else {
				players = getTournament().getAcceptedPlayers();
			}
			// Assign the winners to matches
			List matchPlayers = new ArrayList();
			for (int i = 0; i < players.length; i++) {
				matchPlayers.add(players[i]);
				if (matchPlayers.size() == game.getMaxPlayersPerMatch()) {
					addMatch(GamePeerManager.getInstance().createMatch(this, (User[])matchPlayers.toArray(new User[matchPlayers.size()])));
					matchPlayers.clear();
				}
			}
			if (matchPlayers.size() >= game.getMinPlayersPerMatch()) {
				addMatch(GamePeerManager.getInstance().createMatch(this, (User[])matchPlayers.toArray(new User[matchPlayers.size()])));
			} else if (matchPlayers.size() > 0) {
				for (Iterator i = matchPlayers.iterator(); i.hasNext();) {
					addBye((User)i.next());
				}
			}
			setPlanned();
		}
	}
}
