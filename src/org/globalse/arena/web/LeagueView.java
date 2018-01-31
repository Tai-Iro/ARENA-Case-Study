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
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.RemoteLeague;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.exceptions.ArenaException;


public class LeagueView extends View {
	
	private static LeagueView instance = new LeagueView();
	public static LeagueView getInstance() {
		return instance;
	}
	
	private LeagueView() {}
	
	private String getBaseHref(LeagueInfo info) {
		return
			Controller.SERVLET_NAME + "?" +
			Controller.ACTION + "=" + Controller.SHOW + "&" +
			Controller.VIEW + "=" + Controller.LEAGUE + "&" +
			Controller.ID + "=" + Controller.getId(info.getLeague());
	}
	
	public String showLink(LeagueInfo info) {
		return HTML.link(getBaseHref(info), "", info.getName());
	}
	
	public String showRow(String ticket, LeagueInfo info, String baseHref, List objectsToExpand) {
		String collapseOrExpand = null;
		String children = "";
		RemoteLeague league = info.getLeague();
		if (objectsToExpand.contains(info.getLeague())) {
			collapseOrExpand = collapseLink(league, baseHref, objectsToExpand);
			children = showChildren(ticket, info, baseHref, objectsToExpand);
		} else {
			collapseOrExpand = expandLink(league, baseHref, objectsToExpand);
		}
		return
			HTML.tableRow(HTML.emptyCells(1) +
							  HTML.tableCell("COLSPAN=\"3\"", collapseOrExpand +
											 HTML.definition(showLink(info) + " owned by " + info.getOwner().getLogin(),
															 info.getDescription())) +
							  HTML.tableCell((info.isRestricted()?HTML.red("restricted"):HTML.green("unrestricted")))) +
			children;
	}
	
	public String showChildren(String ticket, LeagueInfo info, String baseHref, List objectsToExpand) {
		StringBuffer result = new StringBuffer();
		try {
			TournamentInfo [] tournamentInfos = info.getLeague().getTournamentInfos(ticket);
			TournamentView tournamentView = TournamentView.getInstance();
			for (int i = 0; i < tournamentInfos.length; i++) {
				result.append(tournamentView.showRow(ticket, tournamentInfos[i], baseHref, objectsToExpand));
			}
			return result.toString();
		} catch (RemoteException e) {
			return HTML.red("Connection failure occured while retrieving tournaments.");
		} catch (ArenaException e) {
			return HTML.red(e.getMessage());
		}
	}
	
	public void show(String ticket, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ArenaException {
		
		String id = req.getParameter(Controller.ID);
		RemoteLeague league = (RemoteLeague)Controller.getObjectFromId(id);
		List objectsToExpand = Controller.getObjectsFromIdArray(req.getParameterValues(Controller.EXPAND));
		LeagueInfo info = league.getInfo();
		String title = "Arena: " + info.getName() + " League";
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

