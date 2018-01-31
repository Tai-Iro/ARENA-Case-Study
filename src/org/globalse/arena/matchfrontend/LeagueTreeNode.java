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

import java.util.ArrayList;
import java.util.List;
import org.globalse.arena.remote.RemoteLeague;
import org.globalse.arena.remote.RemoteTournament;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.user.User;
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.MatchInfo;

/**
 * Data structure for nodes in <code>LeagueTreeModel</code>s.
 *
 * @author Michael Nagel
 */
public class LeagueTreeNode {
	
    private LeagueInfo leagueInfo = null;
    private TournamentInfo tournamentInfo = null;
    private MatchInfo matchInfo = null;
	private User player =  null;
    private String text = null;
    private List children = new ArrayList();
	
	public LeagueTreeNode(String text) {
		this.text = text;
	}
	
    public LeagueTreeNode(LeagueInfo league, String text) {
		this.leagueInfo = league;
		this.text = text;
    }
	
    public LeagueTreeNode(TournamentInfo tournament, String text) {
		this.tournamentInfo = tournament;
		this.text = text;
    }
	
    public LeagueTreeNode(TournamentInfo tournament, User player, String text) {
		this.tournamentInfo = tournament;
		this.player = player;
		this.text = text;
    }
	
    public LeagueTreeNode(MatchInfo match, String text) {
		this.matchInfo = match;
		this.text = text;
    }

    public LeagueInfo getLeagueInfo() {
		return leagueInfo;
    }

	public void setLeagueInfo(LeagueInfo info) {
		leagueInfo = info;
	}
	
	public User getPlayer() {
		return player;
	}

    public MatchInfo getMatchInfo() {
		return matchInfo;
    }
	
    public String getText() {
		return text;
    }
	
	public void setText(String newText) {
		text = newText;
	}

    public TournamentInfo getTournamentInfo() {
		return tournamentInfo;
    }
	
	public void setTournamentInfo(TournamentInfo info) {
		tournamentInfo = info;
	}
    public List getChildren() {
		return children;
    }
	
    public void addChild(LeagueTreeNode child) {
		children.add(child);
    }
	
    public void resetChildren() {
		children = new ArrayList();
	}
	
    public String toString() {
		return text;
    }
    
}
