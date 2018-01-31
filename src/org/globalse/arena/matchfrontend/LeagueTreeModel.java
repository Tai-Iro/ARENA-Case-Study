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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.User;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class is a tree model for displaying a hierarchy of Leagues, Tournaments,
 * Rounds, and Matches in a JTree.
 *
 * @see LeaguesFrame
 * @see LeagueTreeNode
 *
 * @author Michael Nagel
 */
public class LeagueTreeModel implements TreeModel, LocalArenaListener {
	
    private String root = null;
    private LeagueTreeNode rootNode = null;
	private Map nodeMap= new HashMap();
    private List listeners = new Vector();
	private String gameName;
	private ArenaListenerAdapter adapter;
	
	public LeagueTreeModel(String gameName) throws RemoteException, InvalidTicketException, GameNotFoundException {
		this.root = gameName + " Leagues";
		this.rootNode = new LeagueTreeNode(root);
		this.gameName = gameName;
		adapter = new ArenaListenerAdapter(this);
		String ticket = MatchFrontEnd.getTicket();
		MatchFrontEnd.getRemoteArena().addListener(ticket, gameName, adapter);
	}
	
	public void leaveArena() throws RemoteException, InvalidTicketException, GameNotFoundException {
		MatchFrontEnd.getRemoteArena().removeListener(gameName, adapter);
	}
	
    public Object getRoot() {
		return rootNode;
    }
	
    protected void fireTreeStructureChanged() {
		fireTreeStructureChanged(new Object[] { root});
	}
	
	protected void fireTreeStructureChanged(Object [] path) {
		TreeModelEvent e = new TreeModelEvent(this, path);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeStructureChanged(e);
		}
    }
	
	protected void fireTreeNodesChanged(LeagueTreeNode node) {
		TreeModelEvent e = new TreeModelEvent(this, new Object[]{node});
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			((TreeModelListener) it.next()).treeNodesChanged(e);
		}
	}
	
    public Object getChild(Object parent, int index) {
		if (!(parent instanceof LeagueTreeNode)) {
			return null;
		}
		try {
			return ((LeagueTreeNode) parent).getChildren().get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
    }
	
    public int getChildCount(Object parent) {
		if (!(parent instanceof LeagueTreeNode)) {
			return 0;
		}
		return ((LeagueTreeNode) parent).getChildren().size();
    }
	
    public boolean isLeaf(Object node) {
		if (!(node instanceof LeagueTreeNode)) {
			return true;
		}
		return ((LeagueTreeNode) node).getChildren().size() == 0;
    }
	
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
	
    public int getIndexOfChild(Object parent, Object child) {
		if (!(parent instanceof LeagueTreeNode)) {
			return 0;
		}
		return ((LeagueTreeNode) parent).getChildren().indexOf(child);
    }
	
    public void addTreeModelListener(TreeModelListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
    }
	
    public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
    }
	
	private void loadPlayers(LeagueTreeNode tournamentNode, TournamentInfo tournamentInfo, User [] players, String status) {
		for (int k = 0; k < players.length; k++) {
			LeagueTreeNode playerNode =
				new LeagueTreeNode(tournamentInfo, players[k], players[k].getLogin() + "- " + status);
			tournamentNode.addChild(playerNode);
		}
	}
	
	private void loadRound(LeagueTreeNode tournamentNode, RoundInfo roundInfo) {
		MatchInfo[] matchInfos = roundInfo.getMatchInfos();
		for (int k = 0; k < matchInfos.length; k++) {
			MatchInfo matchInfo = matchInfos[k];
			LeagueTreeNode matchNode = new LeagueTreeNode(matchInfo, getMatchLabel(matchInfo));
			tournamentNode.addChild(matchNode);
			nodeMap.put(matchInfo.getMatch(), matchNode);
		}
		User[] byes = roundInfo.getByes();
		for (int i = 0; i < byes.length; i++) {
			LeagueTreeNode playerNode =	new LeagueTreeNode(byes[i].getLogin() + " - bye");
			tournamentNode.addChild(playerNode);
		}
	}
	
	private void loadRanks(LeagueTreeNode tournamentNode, TournamentInfo tournamentInfo) {
		User[][] ranks = tournamentInfo.getRanks();
		if (ranks != null) {
			for (int i = 0; i < ranks.length; i++) {
				String rankString = "" + (i+1) + ". ";
				String separator = "";
				for (int j = 0; j < ranks[i].length; j++) {
					rankString += separator + ranks[i][j].getLogin();
					separator = ",";
				}
				LeagueTreeNode textNode = new LeagueTreeNode(rankString);
				tournamentNode.addChild(textNode);
			}
		}
	}
	
	private void loadTournamentChildren(LeagueTreeNode tournamentNode, TournamentInfo tournamentInfo) throws RemoteException, InvalidTicketException, AccessDeniedException {
		RemoteTournament tournament = tournamentInfo.getTournament();
		String state = tournamentInfo.getState();
		if (state.equals(RemoteTournament.REGISTRATION) ||
			state.equals(RemoteTournament.REGISTRATIONFINISHED)) {
			loadPlayers(tournamentNode, tournamentInfo, tournamentInfo.getAcceptedPlayers(), "accepted");
			loadPlayers(tournamentNode, tournamentInfo, tournamentInfo.getInterestedPlayers(), "interested");
		} else if (state.equals(RemoteTournament.FINISHED)) {
			loadRanks(tournamentNode, tournamentInfo);
		} else if (!state.equals(RemoteTournament.TERMINATED)) {
			loadRound(tournamentNode, tournament.getCurrentRoundInfo(MatchFrontEnd.getTicket()));
		}
	}
	
	private String getTournamentLabel(TournamentInfo tournamentInfo) {
		String state = tournamentInfo.getState();
		if (state.equals(RemoteTournament.PLAYING)) {
			switch (tournamentInfo.getNumRounds()) {
				case 1:
					state += " first ";
					break;
				case 2:
					state += " second ";
					break;
				case 3:
					state += " third ";
					break;
				default:
					state += tournamentInfo.getNumRounds() + "th ";
			}
			state += "round.";
		}
		return tournamentInfo.getName() + " - " + state;
	}
	
	private void loadTournamentInfo(LeagueTreeNode leagueNode, TournamentInfo tournamentInfo) throws RemoteException, InvalidTicketException, AccessDeniedException {
		LeagueTreeNode tournamentNode =	new LeagueTreeNode(tournamentInfo, getTournamentLabel(tournamentInfo));
		leagueNode.addChild(tournamentNode);
		nodeMap.put(tournamentInfo.getTournament(), tournamentNode);
		loadTournamentChildren(tournamentNode, tournamentInfo);
		
	}
	
	private String getLeagueLabel(LeagueInfo leagueInfo) {
		String leagueAccess = "";
		if (leagueInfo.isRestricted()) {
			leagueAccess = "restricted, ";
		}
		leagueAccess += "owner: " + leagueInfo.getOwner().getLogin();
		return leagueInfo.getName() + " [" + leagueAccess  + "] - " + leagueInfo.getDescription();
	}
	
    public void loadLeagueInfo()
		throws InvalidTicketException, AccessDeniedException, GameNotFoundException, RemoteException {
		
		LeagueTreeNode newRootNode = new LeagueTreeNode(root);
		LeagueInfo[] leagueInfos = MatchFrontEnd.getRemoteArena().getLeagueInfosByGame(MatchFrontEnd.getTicket(), gameName);
		for (int i = 0; i < leagueInfos.length; i++) {
			LeagueInfo leagueInfo = leagueInfos[i];
			RemoteLeague league = leagueInfo.getLeague();
			LeagueTreeNode leagueNode =
				new LeagueTreeNode(leagueInfo, getLeagueLabel(leagueInfo));
			newRootNode.addChild(leagueNode);
			nodeMap.put(leagueInfo.getLeague(), leagueNode);
			TournamentInfo[] tournamentInfos = league.getTournamentInfos(MatchFrontEnd.getTicket());
			for (int j = 0; j < tournamentInfos.length; j++) {
				TournamentInfo tournamentInfo = tournamentInfos[j];
				loadTournamentInfo(leagueNode, tournamentInfo);
			}
		}
		rootNode = newRootNode;
		fireTreeStructureChanged();
	}
	
	public void leagueInfoChanged(LeagueInfo leagueInfo) {
		LeagueTreeNode node = (LeagueTreeNode)nodeMap.get(leagueInfo.getLeague());
		if (node != null) {
			node.setLeagueInfo(leagueInfo);
			node.setText(getLeagueLabel(leagueInfo));
			fireTreeNodesChanged(node);
		}
	}
	
	public void tournamentCreated(TournamentInfo tournamentInfo) {
		RemoteLeague league = tournamentInfo.getLeague();
		LeagueTreeNode leagueNode = (LeagueTreeNode)nodeMap.get(league);
		if (leagueNode != null) {
			try {
				loadTournamentInfo(leagueNode, tournamentInfo);
				LeagueTreeNode tournamentNode = (LeagueTreeNode)nodeMap.get(tournamentInfo.getTournament());
				fireTreeStructureChanged(new Object[]{rootNode, leagueNode, tournamentNode});
			} catch (Exception e) {
				try {
					MatchFrontEnd.getLogger().log(Level.SEVERE, getClass().getName(),
													   "Exception while adding tournament node for " + tournamentInfo.getName());
				} catch (Exception e1){}
			}
		}
		
	}
	
	public void tournamentInfoChanged(TournamentInfo tournamentInfo) {
		LeagueTreeNode leagueNode = (LeagueTreeNode)nodeMap.get(tournamentInfo.getLeague());
		LeagueTreeNode tournamentNode = (LeagueTreeNode)nodeMap.get(tournamentInfo.getTournament());
		if (tournamentNode != null) {
			try {
				tournamentNode.setText(getTournamentLabel(tournamentInfo));
				tournamentNode.setTournamentInfo(tournamentInfo);
				tournamentNode.resetChildren();
				loadTournamentChildren(tournamentNode, tournamentInfo);
				fireTreeStructureChanged(new Object[]{rootNode, leagueNode, tournamentNode});
			} catch (Exception e) {
				try {
					MatchFrontEnd.getLogger().log(Level.SEVERE, getClass().getName(),
													   "Exception while updating tournament node for " + tournamentInfo.getName());
				} catch (Exception e1){}
			}
		}
	}
	
	public void roundCreated(RoundInfo roundInfo) {
		RemoteLeague league = roundInfo.getLeague();
		RemoteTournament tournament = roundInfo.getTournament();
		LeagueTreeNode leagueNode = (LeagueTreeNode)nodeMap.get(league);
		LeagueTreeNode tournamentNode = (LeagueTreeNode)nodeMap.get(tournament);
		if (tournamentNode != null) {
			tournamentNode.resetChildren();
			loadRound(tournamentNode, roundInfo);
			fireTreeStructureChanged(new Object[]{rootNode, leagueNode, tournamentNode});
		}
	}
	
	private String getMatchLabel(MatchInfo matchInfo) {
		String playersStr = "";
		User[] players = matchInfo.getPlayers();
		String separator = "";
		for (int m = 0; m < players.length; m++) {
			playersStr += separator;
			separator = ", ";
			playersStr += players[m].getLogin();
		}
		String state = matchInfo.getState();
		if (state.equals(RemoteMatch.FINISHED)) {
			User[][] ranks = matchInfo.getRanks();
			if (ranks.length > 0) {
				if (ranks[0].length == 1) {
					state += ", " + ranks[0][0].getLogin() + " won.";
				} else {
					state += " in tie.";
				}
			}
		}
		playersStr += "- " + state;
		return playersStr;
	}
	
	public void matchInfoChanged(MatchInfo matchInfo) {
		LeagueTreeNode node = (LeagueTreeNode)nodeMap.get(matchInfo.getMatch());
		if (node != null) {
			node.setText(getMatchLabel(matchInfo));
			fireTreeNodesChanged(node);
		}
	}
}
