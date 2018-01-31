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
package org.globalse.arena.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteTournament;
import org.globalse.arena.remote.RoundInfo;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.ArenaException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.User;

public class TournamentView extends View {
	
	private static TournamentView instance = new TournamentView();
	public static TournamentView getInstance() {
		return instance;
	}
	
	private TournamentView() {}
	
	private String getBaseHref(TournamentInfo info) {
		return
			Controller.SERVLET_NAME + "?" +
			Controller.ACTION + "=" + Controller.SHOW + "&" +
			Controller.VIEW + "=" + Controller.TOURNAMENT + "&" +
			Controller.ID + "=" + Controller.getId(info.getTournament());
	}
	
	public String showLink(TournamentInfo info) {
		return HTML.link(getBaseHref(info), "", info.getName());
	}
	
	public String showRow(String ticket, TournamentInfo info, String baseHref, List objectsToExpand) {
		String collapseOrExpand = null;
		String children = "";
		RemoteTournament tournament = info.getTournament();
		if (objectsToExpand.contains(tournament)) {
			collapseOrExpand = collapseLink(tournament, baseHref, objectsToExpand);
			children = showChildren(ticket, info, baseHref, objectsToExpand);
		} else {
			collapseOrExpand = expandLink(tournament, baseHref, objectsToExpand);
		}
		return
			HTML.tableRow(HTML.emptyCells(2) +
							  HTML.tableCell("COLSPAN=\"2\"", collapseOrExpand +
												 HTML.definition(showLink(info) + (info.isFacilitated()?HTML.red("facilitated"):HTML.green("adhoc")),
																 info.getDescription())) +
							  HTML.tableCell(info.getState())) +
			children;
	}
	
	private String showPlayerRow(String ticket, User player, boolean accepted) {
		return
			HTML.tableRow(HTML.tableCell(player.getLogin()) +
							  HTML.tableCell(accepted?HTML.green("accepted"):HTML.orange("interested")));
	}
	
	private String showPlayers(String ticket, TournamentInfo info) {
		StringBuffer result = new StringBuffer();
		User [] players = info.getAcceptedPlayers();
		for (int i = 0; i < players.length; i++) {
			result.append(showPlayerRow(ticket, players[i], true));
		}
		players = info.getInterestedPlayers();
		for (int i = 0; i < players.length; i++) {
			result.append(showPlayerRow(ticket, players[i], false));
		}
		return result.toString();
	}
	
	private String showMatches(String ticket, TournamentInfo info, String baseHref, List objectsToExpand) throws RemoteException, InvalidTicketException, AccessDeniedException {
		StringBuffer result = new StringBuffer();
		RoundInfo roundInfo = info.getTournament().getCurrentRoundInfo(ticket);
		MatchView matchView = MatchView.getInstance();
		MatchInfo [] matchInfos = roundInfo.getMatchInfos();
		for (int i = 0; i < matchInfos.length; i++) {
			result.append(matchView.showRow(ticket, matchInfos[i], info.getGameName()));
		}
		return result.toString();
	}
	
	private String showRanks(String ticket, TournamentInfo info) {
		StringBuffer result = new StringBuffer();
		User[][] ranks = info.getRanks();
		for (int i = 0; i < ranks.length; i++) {
			String rankStr = "" + (i+1) + ". ";
			String separator = "";
			for (int j = 0; j < ranks[i].length; j++) {
				rankStr += separator + ranks[i][j].getLogin();
				separator = ", ";
			}
			result.append(HTML.tableRow(HTML.tableCell(rankStr) + HTML.emptyCells(1)));
		}
		return result.toString();
	}
	
	public String showChildren(String ticket, TournamentInfo info, String baseHref, List objectsToExpand) {
		StringBuffer result = new StringBuffer();
		try {
			String state = info.getState();
			if (state.equals(RemoteTournament.REGISTRATION) ||
				state.equals(RemoteTournament.REGISTRATIONFINISHED)) {
				result.append(showPlayers(ticket, info));
			} else if (state.equals(RemoteTournament.PLAYING) ||
					   state.equals(RemoteTournament.ROUNDFINISHED)) {
				result.append(showMatches(ticket, info, baseHref, objectsToExpand));
			} else if (state.equals(RemoteTournament.FINISHED)) {
				result.append(showRanks(ticket, info));
			}
			
			return result.toString();
		} catch (RemoteException e) {
			return HTML.red("Connection failured occured while retrieving round.");
		} catch (ArenaException e) {
			return HTML.red(e.getMessage());
		}
	}
	
	public void show(String ticket, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ArenaException {
		
		String id = req.getParameter(Controller.ID);
		RemoteTournament tournament = (RemoteTournament)Controller.getObjectFromId(id);
		List objectsToExpand = Controller.getObjectsFromIdArray(req.getParameterValues(Controller.EXPAND));
		TournamentInfo info = tournament.getInfo();
		String title = "Arena Tournament: " + info.getName();
		String baseHref = getBaseHref(info);
		PrintWriter out = res.getWriter();
		
		out.println(HTML.header(title));
		out.println(HTML.title(title));
		out.println(HTML.italics("Click on any tournament to display the matches currently available."));
		out.println(HTML.beginTable());
		out.println(showChildren(ticket, info, baseHref, objectsToExpand));
		out.println(HTML.endTable());
		out.println(HTML.footer());
	}
	
	public void doAction(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException, ArenaException {
		String action = req.getParameter(Controller.ACTION);
		if (action == null || action.equals(Controller.SHOW)) {
			show(ticket, req, res);
		}
	}
	
}

