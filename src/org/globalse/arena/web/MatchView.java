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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteMatch;
import org.globalse.arena.user.User;
import org.globalse.arena.remote.exceptions.ArenaException;
import java.io.File;

public class MatchView extends View {
	
	private static MatchView instance = new MatchView();
	public static MatchView getInstance() {
		return instance;
	}
	
	private MatchView() {}
	
	// TODO: Simplify
	public String showRow(String ticket, MatchInfo info, String gameName) {
		User [] players = info.getPlayers();
		String label = "";
		String separator = "";
		String state = info.getState();
		if (state.equals(RemoteMatch.FINISHED)) {
			User[][] ranks = info.getRanks();
			boolean noWinners = true;
			int i = 0;
			while (noWinners) {
				if (ranks[i].length > 0) {
					noWinners = false;
					for (int j = 0; j < ranks[i].length; j++) {
						label += separator + HTML.bold(ranks[i][j].getLogin());
						separator = ", ";
					}
				}
				i++;
			}
			while (i < ranks.length) {
				for (int j = 0; j < ranks[i].length; j++) {
					label += separator + ranks[i][j].getLogin();
					separator = ", ";
				}
				i++;
			}
		} else {
			for (int i = 0; i < players.length; i++) {
				label += separator + players[i].getLogin();
				separator = ", ";
			}
		}
		return
			HTML.tableRow(HTML.emptyCells(3) + HTML.tableCell(label) + HTML.tableCell(showLink(info, gameName)));
	}
	
	public String showLink(MatchInfo info, String gameName) {
		String href =
			Controller.SERVLET_NAME + "?" +
			Controller.ACTION + "=" + Controller.SHOW + "&" +
			Controller.VIEW + "=" + Controller.MATCH + "&" +
			Controller.ID + "=" + Controller.getId(info.getMatch()) + "&" +
			Controller.GAME + "=" + gameName;
		return HTML.link(href, "", info.getState());
	}
	
	public void sendMatchFrontEndJNLP(String ticket, HttpServletRequest req, HttpServletResponse res)
		throws IOException, ArenaException {
		String id = req.getParameter(Controller.ID);
		RemoteMatch match = (RemoteMatch)Controller.getObjectFromId(id);
		String gameName = req.getParameter(Controller.GAME);

		System.out.println("server name: " + req.getServerName());
		System.out.println("server port: " + req.getServerPort());
		System.out.println("context path: " + req.getContextPath());
		System.out.println("ticket: " + ticket);
		System.out.println("game name: " + gameName);
		System.out.println("match id: " + match.getInfo().getMatchId());
		
		res.setContentType("application/x-java-jnlp-file");
		PrintWriter out = res.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<!-- JNLP File for launching ARENA MatchFrontEnd with WebStart -->");
		out.println("<jnlp");
		out.println("spec=\"1.0+\"");
		out.println("codebase=\"http://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath() + "/\"");
//		out.println("href=\"matchfrontend.jnlp\"");
		out.println(">");
		out.println("  <information>");
		out.println("    <title>ARENA MatchFrontEnd</title>");
		out.println("    <vendor>GlobalSE</vendor>");
		out.println("    <homepage href=\"http://oose.globalse.org/\"/>");
		out.println("    <description>Arena Match Front End Application</description>");
		out.println("    <description kind=\"short\">ARENA version 1.0</description>");
		out.println("    <icon href=\"arena.png\"/>");
		out.println("    <offline-allowed/>");
		out.println("  </information>");
		out.println("  <resources>");
		out.println("    <j2se version=\"1.4 1.4.0-beta3 1.4.0-beta2 1.4.1 1.4.2 1.3 1.2\"/>");
		out.println("    <jar href=\"arena.jar\" main=\"true\" download=\"eager\"/>");
		out.println("  </resources>");
		out.println("  <application-desc main-class=\"org.globalse.arena.matchfrontend.MatchFrontEnd\">");
		out.println("    <argument>http://localhost:8080/arena/matchfrontend.properties</argument>");
		out.println("    <argument>" + ticket + "</argument>");
		out.println("    <argument>" + match.getInfo().getMatchId() + "</argument>");
		out.println("    <argument>" + gameName + "</argument>");
		out.println("  </application-desc>");
		out.println("</jnlp>");
	}
	
	public void doAction(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException, ArenaException {
		String action = req.getParameter(Controller.ACTION);
		if (action == null || action.equals(Controller.SHOW)) {
			sendMatchFrontEndJNLP(ticket, req, res);
		}
	}
	
}

