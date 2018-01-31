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

import java.io.Serializable;
import java.util.Map;
import org.globalse.arena.server.Tournament;
import org.globalse.arena.user.User;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.server.GameManager;
import org.globalse.arena.remote.exceptions.GameNotFoundException;

/**
 * This class is a container for transporting information about tournaments to and
 * from remote methods. Instances are created by the arena server and sent to the
 * match front ends. Once created, TournamentInfos are not updated as tournaments change.
 *
 * @see RemoteTournament#getInfo
 * @author Michael Nagel
 */
public class TournamentInfo implements Serializable {

    private RemoteTournament tournament;
	private RemoteLeague league;
	private String id;
    private String name;
    private String description;
    private String state;
	private String gameName;
	private String styleName;
	private int maxNumPlayers;
	private int numRounds;
	private boolean facilitated;
	private User[] interestedPlayers;
	private User[] acceptedPlayers;
	private User[][] ranks = null;

	/**
	 * Creates a TournamentInfo object for the specified tournament. This constructor
	 * is only invokved by the arena server, right before serializing this object for
	 * the match front end.
	 *
	 * @param    tournament          a  Tournament
	 *
	 */
    public TournamentInfo(Tournament tournament) {
		this.tournament = tournament;
		this.id = tournament.getId();
		this.league = tournament.getLeague();
		try {
			this.gameName = GameManager.getInstance().getGameName(tournament.getLeague().getGame());
		} catch (GameNotFoundException e) {}
		this.name = tournament.getName();
		this.description = tournament.getDescription();
        this.state = tournament.getState();
		this.maxNumPlayers = tournament.getMaxNumPlayers();
		this.facilitated = tournament.isFacilitated();
		this.interestedPlayers = tournament.getInterestedPlayers();
		this.acceptedPlayers = tournament.getAcceptedPlayers();
		if (tournament.getState().equals(RemoteTournament.PLAYING)) {
			this.numRounds = tournament.getRounds().length;
		} else {
			this.numRounds = 0;
		}
		if (tournament.getState().equals(RemoteTournament.FINISHED)) {
			try {
				ranks = tournament.getRanks();
			} catch (InvalidStateException e) {}
		}
    }

	/**
	 * Returns a remote reference to the associated tournament.
	 *
	 * @return   a RemoteTournament
	 *
	 */
    public RemoteTournament getTournament() {
        return tournament;
    }

	/**
	 * Returns a remote reference to the associated league.
	 *
	 * @return   a RemoteLeague
	 *
	 */
	public RemoteLeague getLeague() {
		return league;
	}
	
	/**
	 * Returns the name of the associated game. Games instances are not sent to the match front end.
	 *
	 * @return   a String
	 *
	 */
	public String getGameName() {
		return gameName;
	}
	
	/**
	 * Returns the unique id of the associated tournament.
	 *
	 * @return   a String
	 *
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the name of the associated tournament at the time of this object was created.
	 *
	 * @return   a String
	 *
	 */
    public String getName() {
        return name;
    }

	/**
	 * Returns the description of the associated tournament at the time this object was created.
	 *
	 * @return   a String
	 *
	 */
    public String getDescription() {
        return description;
    }
	
	/**
	 * Returns the state of the associated tournament at the time this object was created.
	 *
	 * @return   a String
	 *
	 */
    public String getState() {
        return state;
    }

	/**
	 * Returns the maximum number of players of the associated tournament.
	 *
	 * @return   an int
	 *
	 */
	public int getMaxNumPlayers() {
		return maxNumPlayers;
	}

	/**
	 * Returns true if the tournament is facilitated, that is, if the league owner advances the state of the tournament.
	 *
	 * @return   a boolean
	 *
	 */
	public boolean isFacilitated() {
		return facilitated;
	}
	
	/**
	 * Returns true if the specified user is a player that was accepted in the tournament at the time this object was created.
	 *
	 * @param    user                a User
	 *
	 * @return   a boolean
	 *
	 */
	public boolean isPlayerAccepted(User user) {
		for (int i = 0; i < acceptedPlayers.length; i++) {
			if (acceptedPlayers[i].equals(user)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an array of all users who were accepted in the tournament at the time this object was created.
	 *
	 * @return   an array of Users
	 *
	 */
	public User[] getAcceptedPlayers() {
		return acceptedPlayers;
	}
	
	/**
	 * Returns true if the specified user has applied for the tournament but has not been accepted yet.
	 *
	 * @param    user                a User
	 *
	 * @return   a boolean
	 *
	 */
	public boolean isPlayerInterested(User user) {
		for (int i = 0; i < interestedPlayers.length; i++) {
			if (interestedPlayers[i].equals(user)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an array of all users who applied for the tournament but have not been accepted yet.
	 *
	 * @return   an array of Users
	 *
	 */
	public User[] getInterestedPlayers() {
		return interestedPlayers;
	}

	/**
	 * Returns the number of rounds that have been played so far, including the current one.
	 *
	 * @return   an int
	 *
	 */
	public int getNumRounds() {
		return numRounds;
	}
	
	/**
	 * When the tournament is completed, returns the ranks for this tournament; otherwise, returns null.
	 *
	 * @return   an User[][]
	 *
	 */
	public User[][] getRanks() {
		return ranks;
	}
}
