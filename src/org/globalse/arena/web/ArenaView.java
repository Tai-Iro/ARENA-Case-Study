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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.globalse.arena.remote.GameDescriptor;
import org.globalse.arena.remote.exceptions.ArenaException;

public class ArenaView extends View {
	
	private static ArenaView instance = new ArenaView();
	public static ArenaView getInstance() {
		return instance;
	}
	
	private ArenaView() {}
	
	public void showUsers(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException {
		// TODO
	}
	
	public void showGames(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException, RemoteException, ArenaException {
		GameDescriptor [] gameInfos = Controller.getRemoteArena().getGameInfos(ticket);
		GameView gameView = GameView.getInstance();
		PrintWriter out = res.getWriter();
		
		out.println(HTML.header("Arena Games"));
		out.println(HTML.title("Arena Games"));
		out.println(HTML.italics("The following games are registered in this arena. Click on the name of a game to display the leagues currently available in that game."));
		out.println(HTML.beginTable());
		for (int i = 0; i < gameInfos.length; i++) {
			out.println(gameView.showRow(ticket, gameInfos[i]));
		}
		out.println(HTML.endTable());
		out.println(HTML.footer());
	}
	
	public void doAction(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException, RemoteException, ArenaException {
		String action = req.getParameter(Controller.ACTION);
		if (action == null || action.equals(Controller.SHOW)) {
			showGames(ticket, req, res);
		}
	}
}

