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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.globalse.arena.remote.RemoteArena;
import org.globalse.arena.remote.exceptions.ArenaException;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.server.Arena;

public class Controller extends HttpServlet {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.web");
	
	public final static String SERVLET_NAME = "Controller";
	
	// Parameters
	public final static String VIEW = "view";
	public final static String ACTION = "action";
	public final static String NAME = "name";
	public final static String PASSWORD = "password";
	public final static String ID = "id";
	public final static String EXPAND = "expand";
	
	// Attributes
	public final static String USER_ERROR = "user_error";
	
	// Views
	public final static String ARENA = "arena";
	public final static String GAME = "game";
	public final static String LEAGUE = "league";
	public final static String TOURNAMENT = "tournament";
	public final static String MATCH = "match";
	
	// Actions
	public final static String NO_ACTION = "no_action";
	public final static String LOGIN_FORM = "login_form";
	public final static String LOGIN = "login";
	public final static String SHOW = "show";
	public final static String CREATE_FORM = "create_form";
	public final static String CREATE = "create";
	public final static String EDIT_FORM = "edit_form";
	public final static String EDIT = "edit";
	public final static String CONFIRM_DELETE = "confirm_delete";
	public final static String DELETE = "delete";
	
	private static RemoteArena arena = null;
	private static boolean initialized = false;
	private static Map session2ticket = new HashMap();
	private static Map id2object = new HashMap();
	private static Map object2id = new HashMap();
	private static long nextId = 0;
	
	public static RemoteArena getRemoteArena() {
		return arena;
	}
	
	public static String getId(Object object) {
		String id = (String)object2id.get(object);
		if (id == null) {
			id = Long.toString(nextId++);
			object2id.put(object, id);
			id2object.put(id, object);
		}
		return id;
	}
	
	public static Object getObjectFromId(String id) {
		return id2object.get(id);
	}
	
	public static List getObjectsFromIdArray(String [] ids) {
		List result = new ArrayList();
		if (ids != null) {
			for (int i = 0; i < ids.length; i++) {
				Object object = getObjectFromId(ids[i]);
				if (object != null) {
					result.add(object);
				}
			}
		}
		return result;
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		synchronized ("lock") {
			if (!initialized) {
				String portStr = getServletContext().getInitParameter("arenaServerPort");
				int serverPort = 1099;
				try {
					if (portStr != null) {
						serverPort = Integer.parseInt(portStr);
					}
				} catch (NumberFormatException e) {
					logger.warning("Failed to parse port \"" + portStr + "\".");
				}
				String host = getServletContext().getInitParameter("arenaServerHost");
				if (host == null) {
					host = "localhost";
				}
				
				try {
					String url = "rmi://" + host + ":" + serverPort + "/ArenaServer";
					logger.info("Connecting to remote arena with url: " + url);
					arena = (RemoteArena)Naming.lookup(url);
					logger.info("... connected.");
				} catch (Exception e) {
					e.printStackTrace();
				}
				initialized = true;
			}
		}
    }
	
	private String getTicket(HttpServletRequest req) throws RemoteException {
		String ticket = null;
		HttpSession session = req.getSession(false);
		if (session == null) {
			session = req.getSession(true);
			ticket = arena.getGuestTicket();
			session2ticket.put(session, ticket);
		} else {
			ticket = (String)session2ticket.get(session);
			if (ticket == null) {
				ticket = arena.getGuestTicket();
				session2ticket.put(session, ticket);
			}
		}
		return ticket;
	}
	
	private void loginForm(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		String title = "ARENA Login";
		String error = (String)req.getAttribute(USER_ERROR);
		out.println(HTML.header(title));
		out.println(HTML.title(title));
		if (error != null) {
			out.println(HTML.red(error));
		}
		out.println(HTML.beginTable());
		out.println(HTML.tableRow(HTML.tableCell("ALIGN=\"right\"", HTML.bold("Login:")) +
									  HTML.tableCell(HTML.textField(NAME, ""))));
		out.println(HTML.tableRow(HTML.tableCell("ALIGN=\"right\"", HTML.bold("Password:")) +
									  HTML.tableCell(HTML.passwordField(PASSWORD, ""))));
		out.println(HTML.endTable());
		out.println(HTML.footer());
	}
	
	private void login(HttpServletRequest req, HttpServletResponse res) throws RemoteException, IOException, ArenaException {
		String ticket = null;
		String login = req.getParameter(NAME);
		String password = req.getParameter(PASSWORD);
		try {
			ticket = arena.login(login, password);
			HttpSession session = req.getSession(true);
			session2ticket.put(session, ticket);
			ArenaView.getInstance().showGames(ticket, req, res);
		} catch (InvalidLoginException e) {
			req.setAttribute(USER_ERROR, "Invalid user name or password.");
			loginForm(req, res);
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws IOException {
		
		res.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		res.setHeader("Pragma","no-cache"); //HTTP 1.0
		res.setDateHeader("Expires", 0); //prevents caching at the proxy server
		
		// Check for login actions
		String action = req.getParameter(ACTION);
		if (action == null) {
			action = NO_ACTION;
		}
		try {
			if (action.equals(LOGIN_FORM)) {
				loginForm(req, res);
			} else if (action.equals(LOGIN)) {
				login(req, res);
			} else {
				String ticket = getTicket(req);
				
				// Figure out which element is involved
				View view = null;
				String viewName = req.getParameter(VIEW);
				if (viewName == null || viewName.equals(ARENA)) {
					view = ArenaView.getInstance();
				} else if (viewName.equals(GAME)) {
					view = GameView.getInstance();
				} else if (viewName.equals(LEAGUE)) {
					view = LeagueView.getInstance();
				} else if (viewName.equals(TOURNAMENT)) {
					view = TournamentView.getInstance();
				} else if (viewName.equals(MATCH)) {
					view = MatchView.getInstance();
				} else {
					PrintWriter out = res.getWriter();
					out.println(HTML.header("") + HTML.title("Unknown view: \"" + viewName +"\"."));
					out.println(HTML.footer());
				}
				// Dispatch the request to the view
				view.doAction(ticket, req, res);
			}
		} catch (IOException e) {
			PrintWriter out = res.getWriter();
			out.println(HTML.header("") + HTML.title("An IO error occured."));
			out.println(e.getStackTrace());
			out.println(HTML.footer());
		} catch (ArenaException e) {
			PrintWriter out = res.getWriter();
			out.println(HTML.header("") + HTML.title("An ARENA exception occured."));
			out.println(e.getStackTrace());
			out.println(HTML.footer());
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws IOException {
		doGet(req, res);
	}
	
	public void destroy() {
	}
	
	
}

