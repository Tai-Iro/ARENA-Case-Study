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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.remote.RemoteRound;
import org.globalse.arena.remote.RoundInfo;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

// TODO: When rounds end, match moves, ranks, and statistics should be copied into
// arena server to free game peer resources. Issue: dangling remote references to peer
// matches from match front ends.
public abstract class Round extends UnicastRemoteObject implements RemoteRound {
    
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	// Attributes
	private boolean planned = false;
    private boolean completed = false;
	
	// Associations
	private Round previousRound = null;
	private Round nextRound = null;
    private Tournament tournament = null;
    private ArrayList matches = new ArrayList();
	private ArrayList completedMatches = new ArrayList();
    private ArrayList byes = new ArrayList();
	
    
    public Round(Tournament tournament) throws RemoteException {
		if (tournament == null) {
			throw new IllegalArgumentException("Argument tournament cannot be null.");
		}
		this.tournament = tournament;
		tournament.addRound(this);
    }
	
	public Round(Round previous) throws RemoteException {
		if (previous == null) {
			throw new IllegalArgumentException("Argument previous round cannot be null.");
		}
		this.previousRound = previous;
		this.tournament = previous.tournament;
		tournament.addRound(this);
	}
	
	synchronized public RoundInfo getInfo() {
		return new RoundInfo(this);
	}
	
	public Round getPreviousRound() {
		return previousRound;
	}
	
	synchronized public Round getNextRound() {
		return nextRound;
	}
	
	synchronized protected void setNextRound(Round round) {
		this.nextRound = round;
	}
	
	synchronized public boolean isPlanned() {
		return planned;
	}
	
	synchronized protected void setPlanned() {
		planned = true;
	}
	
    synchronized public MatchInfo[] getMatchInfos() {
		List result = new ArrayList();
		RemoteMatch[] matches = this.getMatches();
		for (int i = 0; i < matches.length; i++) {
			try {
				result.add(matches[i].getInfo());
			} catch (RemoteException e) {
				// TODO: this happens if the connection to the peer fails.
				logger.warning("Round failed to create remote match info.");
			}
		}
		return (MatchInfo[])result.toArray(new MatchInfo[result.size()]);
    }
	
	public abstract void plan();
	
	synchronized public boolean isCompleted() {
		return completed;
	}
	
	public Tournament getTournament() {
		return tournament;
	}
	
    synchronized public RemoteMatch[] getMatches() {
		return (RemoteMatch[])matches.toArray(new RemoteMatch[matches.size()]);
    }
	
	// TODO: Invoked from tournament styles only
	synchronized public void addMatch(RemoteMatch match) {
		if (!matches.contains(match)) {
			matches.add(match);
		}
	}
	
	synchronized public User[] getByes() {
		return (User[])byes.toArray(new User[byes.size()]);
	}
	
	// TODO: Invoked from tournament styles only
	synchronized public void addBye(User player) {
		if (!byes.contains(player)) {
			byes.add(player);
		}
	}
	
	synchronized public void open(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException {
		logger.fine("Opening new round.");
		Iterator it = matches.iterator();
		while (it.hasNext()) {
			((RemoteMatch)it.next()).open(ticket);
		}
	}
	
	synchronized public void terminate(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException  {
		Iterator it = matches.iterator();
		while (it.hasNext()) {
			((RemoteMatch) it.next()).terminate(ticket);
		}
	}
	
	private void archiveMatch(MatchInfo matchInfo) {
		// TODO: save the match history into the server and release the
		// remote match.
	}
	
	private void matchEnded(MatchInfo matchInfo) {
		logger.fine("Round received matchEnded.");
		RemoteMatch match = matchInfo.getMatch();
		if (!completedMatches.contains(match)) {
			completedMatches.add(match);
		}
		if (completedMatches.size() == matches.size()) {
			completed = true;
			tournament.roundCompleted(this);
		}
		archiveMatch(matchInfo);
	}
	
	private void matchTerminated(MatchInfo matchInfo) {
		// This is initiated by the tournament, nothing to do.
		archiveMatch(matchInfo);
	}
	
	synchronized public void fireMatchInfoChanged(String ticket, MatchInfo matchInfo) throws RemoteException, InvalidTicketException, AccessDeniedException {
		logger.fine("Round received match info changed, forwarding to arena listeners.");
		tournament.checkAccess(ticket, AccessPolicy.MANAGE);
		String matchState = matchInfo.getState();
		if(matchState.equals(RemoteMatch.FINISHED)) {
			matchEnded(matchInfo);
		} else if (matchState.equals(RemoteMatch.TERMINATED)) {
			matchTerminated(matchInfo);
		}
		Arena.getInstance().getNotifier().fireMatchInfoChanged(tournament, matchInfo);
		logger.fine("... done forwarding to arena listeners.");
	}

	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException
	{
		return tournament.hasAccess(ticket, access);
	}

	synchronized public User getPlayer(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		tournament.checkAccess(ticket, AccessPolicy.PLAY);
		return Arena.getInstance().getUser(ticket);
	}

}
