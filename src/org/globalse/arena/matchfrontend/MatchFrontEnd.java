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

import javax.swing.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.Level;
import org.globalse.arena.matchfrontend.LeaguesFrame;
import org.globalse.arena.remote.GameDescriptor;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.MatchPanelFactory;
import org.globalse.arena.remote.RemoteArena;
import org.globalse.arena.remote.RemoteLogger;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.ArenaException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.User;
import org.globalse.arena.util.PropertyLoader;

/**
 * This class provides the main method for starting a match front end. It takes a
 * single command line argument which specified the location (i.e., filename or URL) for
 * the properties file. The properties understood by <code>MatchFrontEnd</code> include:
 * <UL>
 *     <LI><code>ArenaHost</code> (default localhost) the hostname on which the arena server is registered</LI>
 *     <LI><code>ArenaPort</code> (default 1099)      the TCP/IP port on which the arena server listens for new RMI connections</LI>
 * </UL>
 *
 * <P>In addition, this class can optionally take an arena ticket, a match id, and a game
 * name as command line arguments, for the case where the match front end is started for a
 * specific match.</P>
 * <P>This class does not use the java.util.logging facilities for logging, so that it can also
 * run in a restricted sandbox (e.g., java web start). Instead, it uses the arena remote
 * logger {@link org.globalse.arena.remote.RemoteLogger}.</P>
 *
 * @author Allen Dutoit
 * @author Michael Nagel
 */
public class MatchFrontEnd {
	
	private static RemoteArena remoteArena;
	private static RemoteLogger logger;
	private static String ticket;
	private static User user;
	
	private static void initArena(PropertyLoader propertyLoader) throws RemoteException, NotBoundException, MalformedURLException {
		String serverName = propertyLoader.getStringProperty("ArenaHost", "localhost");
		int serverPort = propertyLoader.getIntProperty("ArenaPort", 1099);
		remoteArena = (RemoteArena)Naming.lookup("rmi://" + serverName + ":" + serverPort + "/ArenaServer");
		logger = new RemoteLogger(remoteArena);
	}
	
	private static void login() throws RemoteException, InvalidTicketException {
		JTextField userField = new JTextField(15);
		JPasswordField passField = new JPasswordField(15);
		Object [] fields = {"Login:", userField, "Password:", passField};
		String [] options = {"Login", "Exit"};
		
		JOptionPane optionPane =
			new JOptionPane(fields,
							JOptionPane.QUESTION_MESSAGE,
							JOptionPane.YES_NO_OPTION,
							null,
							options,
							options[0]);
		
		JDialog dialog = optionPane.createDialog(null, "ARENA Server Login");
		dialog.setLocationRelativeTo(null); // center the dialog on the screen
		
		boolean loggedIn = false;
		while (!loggedIn) {
			dialog.show();
			if (optionPane.getValue().equals(options[1])) {
				System.out.println("Login canceled, exiting");
				System.exit(1);
			}
			String userName = userField.getText();
			String pass = new String(passField.getPassword());
			try {
				remoteArena = MatchFrontEnd.getRemoteArena();
				ticket = remoteArena.login(userName, pass);
				user = remoteArena.getUser(ticket);
				dialog.dispose();
				loggedIn = true;
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(dialog, "Login failed.",
											  "ARENA Server Login", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	private static String selectGame() throws RemoteException, InvalidTicketException {
		DefaultListModel listModel = new DefaultListModel();
		JList selectionList = new JList(listModel);
		GameDescriptor[] gameInfos = remoteArena.getGameInfos(ticket);
		
		// If there is exactly one game registered with arena, return
		// the game name and skip this step.
		if (gameInfos.length == 1) {
			return gameInfos[0].getName();
		}
		for (int i = 0; i < gameInfos.length; i++) {
			listModel.addElement(gameInfos[i].getName());
		}
		
		Object [] fields = {"Available Games:", new JScrollPane(selectionList)};
		String [] options = {"SELECT", "Exit"};
		
		JOptionPane optionPane =
			new JOptionPane(fields,
							JOptionPane.QUESTION_MESSAGE,
							JOptionPane.YES_NO_OPTION,
							null,
							options,
							options[0]);
		
		JDialog dialog = optionPane.createDialog(null, "ARENA Game Selection");
		dialog.setLocationRelativeTo(null); // center the dialog on the screen
		
		String selectedGameName = null;
		boolean gameSelected = false;
		while (!gameSelected) {
			dialog.show();
			if (optionPane.getValue().equals(options[1])) {
				System.out.println("Game selection canceled, exiting");
				System.exit(1);
			}else{
				selectedGameName = (String)selectionList.getSelectedValue();
				if(selectedGameName != null
				   && selectedGameName.trim().length()>0 ){
					gameSelected = true;
					dialog.dispose();
				}
			}
		}
		return selectedGameName;
	}
	
	public static RemoteArena getRemoteArena() {
		return remoteArena;
	}
	
	public static RemoteLogger getLogger() {
		return logger;
	}
	
	public static User getUser() {
		return user;
	}
	
	public static String getTicket() {
		return ticket;
	}
	
	public static JFrame createMatchFrame(MatchInfo matchInfo, String gameName) throws RemoteException, InvalidTicketException, AccessDeniedException, GameNotFoundException {
		String separator = "";
		String matchLabel = "";
		User[] players = matchInfo.getPlayers();
		for (int i = 0; i < players.length; i++) {
			matchLabel += separator + players[i].getLogin();
			separator = ", ";
		}
		JFrame frame = new JFrame();
		MatchPanelFactory panelFactory = null;
		panelFactory = MatchFrontEnd.getRemoteArena().getMatchPanelFactory(ticket, gameName);
		frame.getContentPane().add(panelFactory.createMatchPanel(MatchFrontEnd.getTicket(), matchInfo));
		if (matchInfo.hasPlayer(MatchFrontEnd.getUser())) {
			frame.setTitle("Playing " + matchLabel);
		} else {
			frame.setTitle("Watching " + matchLabel);
		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		return frame;
	}
	
	public static void main(String[] args) {
		
		try {
			String propertyFileName = null;
			if (args.length > 0) {
				propertyFileName = args[0];
			}
			PropertyLoader propertyLoader = new PropertyLoader(propertyFileName);
			initArena(propertyLoader);
			
			// Set the RMI security manager so that subclasses of Game and Match can be remotely loaded.
			// If a security manager has already been set, do nothing (java web start has already taken care of it).
			if (System.getSecurityManager() == null) {
				
				// TODO: This security manager is too lenient for game-specific classes and should not
				// grant as many permissions. For example, a match panel should not be able to read any file
				// but only the ones served from its own code base.
				System.setSecurityManager(new RMISecurityManager() {
							// This avoids an exception when events are dispatched to the match panel
							public void checkAwtEventQueueAccess() {}
							// This enables the match front end to receive notifications through the remote arena
							// and match listeners, regardless of the current policy
							public void checkAccept(String host, int port) {}
							// This enables the match front end to connect to the arena server
							// and game peers, regardless of the current policy
							public void checkConnect (String host, int port) {}
							public void checkConnect (String host, int port, Object context) {}
							// This enables a game-specific match panel to load resources
							// from its own jar file
							public void checkRead(String name) {}
						});
			}
			
			if (args.length > 1) {
				ticket = args[1];
				logger.log(Level.INFO, "MatchFrontEnd", "Got from command line ticket " + ticket);
			} else {
				login();
				logger.log(Level.INFO, "MatchFrontEnd", "Got from user ticket " + ticket);
			}
			String matchId = null;
			if (args.length > 2) {
				matchId = args[2];
				logger.log(Level.INFO, "MatchFrontEnd", "Got matchId " + matchId);
			}
			String gameName = null;
			if (args.length > 3) {
				gameName = args[3];
				logger.log(Level.INFO, "MatchFrontEnd", "Got game name " + gameName);
			}
			if (gameName == null) {
				gameName = selectGame();
			}
			if (matchId == null) {
				LeaguesFrame leaguesFrame = new LeaguesFrame(gameName);
				leaguesFrame.setVisible(true);
			} else {
				logger.log(Level.INFO, "MatchFrontEnd", "getting match by id " + ticket + " " + matchId);
				MatchInfo matchInfo = remoteArena.getMatchById(ticket, matchId);
				logger.log(Level.INFO, "MatchFrontEnd", "creating match frame ...");
				JFrame matchFrame = createMatchFrame(matchInfo, gameName);
				logger.log(Level.INFO, "MatchFrontEnd", "...successfully");
				matchFrame.setVisible(true);
			}
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
										  "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (NotBoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
										  "Arena server not found.",
										  JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
										  "Exception while loading properties.",
										  JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (ArenaException e) {
			logger.log(Level.SEVERE, "MatchFrontEnd", e.getStackTrace().toString());
			JOptionPane.showMessageDialog(null, e.getMessage(),
										  "ARENA Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
	}
}

