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
import org.globalse.arena.server.League;
import org.globalse.arena.server.Arena;
import org.globalse.arena.user.User;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.TournamentStyleNotFoundException;

/**
 * This class is a container for transporting information about the league to and
 * from remote methods. Instances are created by the arena server and sent to the
 * match front ends. Once created, the LeagueInfos are not updated as Leagues change.
 *
 * @see RemoteLeague#getInfo
 * @author Michael Nagel
 */
public class LeagueInfo implements Serializable {
	
	private RemoteLeague league;
	private String id;
	private String name;
	private String description;
	private boolean restricted;
	private User owner;
	private String gameName;
	private String styleName;
	
	/**
	 * Creates a LeagueInfo object for the specified league. This constructor
	 * is only invokved by the arena server, right before serializing this object for
	 * the match front end.
	 *
	 * @param    league          a  League
	 *
	 */
	public LeagueInfo(League league) {
		this.league = league;
		this.id = league.getId();
		this.name = league.getName();
		this.owner = league.getOwner();
		this.description = league.getDescription();
		this.restricted = league.isRestricted();
		try {
		this.gameName = Arena.getInstance().getGameName(league.getGame());
		this.styleName = Arena.getInstance().getTournamentStyleName(league.getTournamentStyle());
		} catch (GameNotFoundException e) {} catch (TournamentStyleNotFoundException ex) {};
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
	 * Returns the unique identifier of the associated league.
	 *
	 * @return   a String
	 *
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the name of the associated league at the time this object was created.
	 *
	 * @return   a String
	 *
	 */
    public String getName() {
        return name;
    }

	/**
	 * Returns the description of the associated league at the time this object was created.
	 *
	 * @return   a String
	 *
	 */
    public String getDescription() {
        return description;
    }

	/**
	 * Returns true if this league is restricted; a restricted league can only be seen by players registered with the league.
	 *
	 * @return   a boolean
	 *
	 */
	public boolean isRestricted() {
		return restricted;
	}
	
	/**
	 * Returns the owner of the associated league.
	 *
	 * @return   an User
	 *
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * Returns the name of the associated game; game objects are only referenced by name and not forwarded to the match front end.
	 *
	 * @return   a String
	 *
	 */
    public String getGameName() {
        return gameName;
    }
	
	/**
	 * Returns the name of the associated tournament style; tournament style objects are only referenced by name and not forwarded to the match front end.
	 *
	 * @return   a String
	 *
	 */
	public String getTournamentStyleName() {
		return styleName;
	}
}
