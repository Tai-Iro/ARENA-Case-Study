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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.globalse.arena.remote.GameDescriptor;
import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.exceptions.ArenaException;


public class GameView extends View {
	
	private static GameView instance = new GameView();
	public static GameView getInstance() {
		return instance;
	}
	
	private GameView() {}
	
	private String getBaseHref(String gameName) {
		return Controller.SERVLET_NAME + "?" +
			Controller.ACTION + "=" + Controller.SHOW + "&" +
			Controller.VIEW + "=" + Controller.GAME + "&" +
			Controller.NAME + "=" + gameName;
	}
	
	public String showLink(GameDescriptor info) {
		String gameName = info.getName();
		return HTML.link(getBaseHref(gameName), "", gameName);
	}
	
	public String showRow(String ticket, GameDescriptor info) {
		return HTML.tableRow(HTML.tableCell("COLSPAN=\"4\"",
											HTML.definition(showLink(info), info.getDescription())) +
								 HTML.emptyCells(1));
	}
	
	public void show(String ticket, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ArenaException {
		
		String gameName = req.getParameter(Controller.NAME);
		List objectsToExpand = Controller.getObjectsFromIdArray(req.getParameterValues(Controller.EXPAND));
		String title = "Arena: " + gameName + " Leagues";
		LeagueInfo [] leagueInfos = Controller.getRemoteArena().getLeagueInfosByGame(ticket, gameName);
		PrintWriter out = res.getWriter();
		LeagueView leagueView = LeagueView.getInstance();
		String baseHref = getBaseHref(gameName);
		
		out.println(HTML.header(title));
		out.println(HTML.title(title));
		out.println(HTML.italics("The following " + gameName + " leagues are accessible to you. Expand any league to display its tournaments."));
		out.println(HTML.beginTable());
		for (int i = 0; i < leagueInfos.length; i++) {
			out.println(leagueView.showRow(ticket, leagueInfos[i], baseHref, objectsToExpand));
		}
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

