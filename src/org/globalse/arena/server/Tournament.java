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

import org.globalse.arena.remote.exceptions.*;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.globalse.arena.remote.RemoteTournament;
import org.globalse.arena.remote.RoundInfo;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

// TODO: The state of tournaments and the state of matches are not consistent. Proof read.
public class Tournament extends UnicastRemoteObject implements RemoteTournament {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	// Attributes
	private String id;
    private String name;
    private String description;
	private int maxNumPlayers = 0;
	private boolean facilitated;
    private String state = INITIALIZING;
	private String ownerTicket;
	
    // Associations
    private League league = null;
	private List interestedPlayers = new ArrayList();
	private List acceptedPlayers = new ArrayList();
    private ArrayList rounds = new ArrayList();
	
	
    public Tournament(League league) throws RemoteException {
		if (league == null) {
			throw new NullPointerException("Cannot create a tournament in null league.");
		}
		this.id = (new UID()).toString();
		this.league = league;
		this.facilitated = league.isRestricted();
    }
	
	public String getId() {
		return id;
	}
	
	public boolean hasAccess(String ticket, String access)
		throws RemoteException, InvalidTicketException {
		return Arena.getInstance().hasTournamentAccess(ticket, this, access);
	}
	
	// Used by Round.
	void checkAccess(String ticket, String access) throws RemoteException, InvalidTicketException, AccessDeniedException {
		if (!hasAccess(ticket, access)) {
			throw new AccessDeniedException("Ticket " + ticket
												+ " is not allowed to " + access + " this tournament.");
		}
	}
    
	synchronized public TournamentInfo getInfo() {
		return new TournamentInfo(this);
		
	}
	
    synchronized public String getName() {
		return name;
    }
	
	synchronized public void setName(String ticket, String name)
		throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		this.name = name;
	}
	
    synchronized public String getDescription() {
		return description;
    }
	
	synchronized public void setDescription(String ticket, String description)
		throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		this.description = description;
	}
	
	synchronized public int getMaxNumPlayers() {
		return maxNumPlayers;
	}
	
	synchronized public void setMaxNumPlayers(String ticket, int max)
		throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		this.maxNumPlayers = max;
	}
	
	synchronized public void facilitate(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		facilitated = true;
	}
	
	synchronized public void unfaciltiate(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		facilitated = false;
	}
	
	synchronized public boolean isFacilitated() {
		return facilitated;
	}
	
	public League getLeague() {
		return league;
	}
	
    synchronized public void openRegistration(String ticket) throws RemoteException, InvalidStateException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!state.equals(INITIALIZING) && !state.equals(REGISTRATIONFINISHED)) {
			throw new InvalidStateException("Can only open registration during initialization or before launch.");
		}
		state = REGISTRATION;
		logger.info("Tournament registration opened.");
    }
	
	synchronized public User[] getInterestedPlayers() {
		return (User[])interestedPlayers.toArray(new User[interestedPlayers.size()]);
	}
	
	synchronized public void apply(String ticket) throws RemoteException, InvalidStateException, InvalidTicketException {
		User player = Arena.getInstance().getUser(ticket);
		if (!state.equals(REGISTRATION)) {
			throw new InvalidStateException("Can only apply for tournament during registration.");
		}
		if (!interestedPlayers.contains(player)) {
			interestedPlayers.add(player);
			if (facilitated) {
				Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
			} else {
				try {
					acceptPlayer(ticket, player);
				} catch (Exception e) {
					// TODO:
				}
			}
		}
	}
	
	synchronized public void withdraw(String ticket) throws RemoteException, InvalidStateException, InvalidTicketException {
		User player = Arena.getInstance().getUser(ticket);
		if (!state.equals(REGISTRATION)) {
			throw new InvalidStateException("Can only withdraw from tournaments during registration.");
		}
		if (interestedPlayers.contains(player)) {
			interestedPlayers.remove(player);
			if (facilitated) {
				Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
			} else {
				try {
					rejectPlayer(ticket, player);
				} catch (Exception e) {
					// TODO:
				}
			}
		}
	}
	
	synchronized public User[] getAcceptedPlayers() {
		return (User[])acceptedPlayers.toArray(new User[acceptedPlayers.size()]);
	}
	
	synchronized public boolean isPlayerAccepted(User player) {
		return acceptedPlayers.contains(player);
	}
	
	synchronized public void acceptPlayer(String ticket, User player) throws RemoteException, InvalidStateException, TournamentOverbookedException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!state.equals(REGISTRATION) && !state.equals(REGISTRATIONFINISHED)) {
			throw new InvalidStateException("Can only accept players during registration and before launch.");
		}
		if (!acceptedPlayers.contains(player)) {
			if (maxNumPlayers > 0 && acceptedPlayers.size() >= maxNumPlayers) {
				throw new TournamentOverbookedException("Tournament overbooked, " + acceptedPlayers.size() + " players have already been accepted.");
			}
			acceptedPlayers.add(player);
			interestedPlayers.remove(player);
			Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		}
	}
	
	synchronized public void rejectPlayer(String ticket, User player)
		throws RemoteException, InvalidTicketException, InvalidStateException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!state.equals(REGISTRATION) && !state.equals(REGISTRATIONFINISHED)) {
			throw new InvalidStateException("Can only reject players during registration and before launch.");
		}
		if (acceptedPlayers.contains(player)) {
			acceptedPlayers.remove(player);
			interestedPlayers.add(player);
			Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		}
	}
	
	synchronized public void closeRegistration(String ticket)
		throws RemoteException, InvalidStateException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!state.equals(REGISTRATION)) {
			throw new InvalidStateException("Can only close registration during registration.");
		}
		state = REGISTRATIONFINISHED;
		logger.info("Tournament registration closed.");
		Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
	}
	
	synchronized public void launch(String ticket) throws RemoteException, InvalidStateException, InvalidTicketException, AccessDeniedException, InvalidNumPlayersException {
		TournamentStyle style = league.getTournamentStyle();
		checkAccess(ticket, AccessPolicy.MANAGE);
		ownerTicket = ticket;
		if (!state.equals(REGISTRATIONFINISHED)) {
			throw new InvalidStateException("Can only launch tournament when registration is finished.");
		}
		if (!style.isNumPlayersLegal(this, acceptedPlayers.size())) {
			String styleName = "Tournament style";
			try {
				styleName = Arena.getInstance().getTournamentStyleName(style);
			} catch (TournamentStyleNotFoundException e) {}
			throw new InvalidNumPlayersException(styleName +
													 " cannot deal with the current number of acceplated players (" + acceptedPlayers.size() + ").");
		}
		Round round = style.planRounds(this);
		state = PLAYING;
		Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		round.open(ticket);
		logger.info("Tournament launched.");
	}
	
	private void planNextRound() throws InvalidStateException, RemoteException {
		TournamentStyle style = league.getTournamentStyle();
		if (!state.equals(ROUNDFINISHED)) {
			throw new InvalidStateException("Can only plan next round when previous round is completed.");
		}
		if (style.isTournamentFinished(this)) {
			state = FINISHED;
			Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		} else {
			Round nextRound = getCurrentRound().getNextRound();
			nextRound.plan();
			state = PLAYING;
			Arena.getInstance().getNotifier().fireRoundCreated(nextRound);
			if (!facilitated) {
				try {
					nextRound.open(ownerTicket);
				} catch (InvalidTicketException e) {} catch (AccessDeniedException e) {}
			}
		}
		logger.info("Next round planned.");
	}
	
	synchronized public void terminate(String ticket) throws RemoteException, InvalidStateException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.MANAGE);
		if (!state.equals(PLAYING) && !state.equals(ROUNDFINISHED)) {
			throw new InvalidStateException("Can only terminate a tournament after launch.");
		}
		System.out
			.println("Tournament/terminateRound: Terminating current round.");
		getCurrentRound().terminate(ticket);
		roundCompleted(getCurrentRound());
		// TODO: check if this is sufficient
		state = TERMINATED;
		Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		logger.info("Tournament terminated.");
	}
	
	synchronized public String getState() {
		return state;
	}
	
	synchronized public void roundCompleted(Round round) {
		TournamentStyle style = league.getTournamentStyle();
		state = ROUNDFINISHED;
		if (style.isTournamentFinished(this)) {
			state = FINISHED;
			Arena.getInstance().getNotifier().fireTournamentInfoChanged(this);
		} else {
			if (!facilitated) {
				try {
					planNextRound();
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		}
	}
	
	synchronized public Round getCurrentRound() {
		if (rounds.size() == 0) return null;
		return (Round) rounds.get(rounds.size() - 1);
	}
	
	synchronized public RoundInfo getCurrentRoundInfo(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.READ);
		Round round = getCurrentRound();
		if (round == null) {
			return null;
		}
		return round.getInfo();
	}
	
	synchronized public Round[] getRounds() {
		Round[] result = null;
		synchronized(rounds) {
			result = (Round[])rounds.toArray(new Round[rounds.size()]);
		}
		return result;
	}
	
	synchronized protected void addRound(Round round) {
		if (!rounds.contains(round)) {
			rounds.add(round);
		}
	}
	synchronized public RoundInfo[] getRoundInfos(String ticket) throws RemoteException, InvalidTicketException, AccessDeniedException {
		checkAccess(ticket, AccessPolicy.READ);
		RoundInfo[] result = new RoundInfo[rounds.size()];
		int j = 0;
		for (Iterator i = rounds.iterator(); i.hasNext(); j++) {
			Round round = (Round)i.next();
			result[j] = new RoundInfo(round);
		}
		return result;
	}
	
	synchronized public User[][] getRanks() throws InvalidStateException {
		if (state.equals(TERMINATED)) {
			return null;
		}
		return league.getTournamentStyle().getRanks(this);
	}
}
