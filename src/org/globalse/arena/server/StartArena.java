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
package org.globalse.arena.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClassLoader;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.globalse.arena.remote.LeagueInfo;
import org.globalse.arena.remote.MatchPanelFactory;
import org.globalse.arena.remote.RemoteLeague;
import org.globalse.arena.remote.RemoteTournament;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.exceptions.ArenaException;
import org.globalse.arena.remote.exceptions.GameAlreadyExistsException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.user.DefaultAccessPolicy;
import org.globalse.arena.user.User;
import org.globalse.arena.util.PropertyLoader;

/**
 * This class provides the main method for starting an arena server. It takes an
 * optional argument on the command line which specified a filename or a URL for
 * a properties file. The properties understood by StartArena include:
 * <UL>
 *   <LI><code>ArenaPort</code> (default 1099)      The TCP/IP port on which the arena server should accept RMI connections</LI>
 *   <LI><code>CodeBase</code>  (no default)        A list of space-separated URLs specifying where to load game-specified and tournament style specific classes.</LI>
 *   <LI><code>TournamentStyles</code> (no default) A space-separated list of fully qualified class names of tournament styles to be loaded into this arena.</LI>
 *   <LI><code>Games</code> (no default)            A space-separated list of fully qualified class names of games to be loaded into this arena.</LI>
 *   <LI><code>SetupDemo</code> (default false)     A flag specifying whether test users, leagues, tournaments, and matches should be created.</LI>
 * </UL>
 * <P>In addition, this class will use the properties file to initialize the loggers. See the
 * documentation on java.util.logging for information about logging properties.</P>
 *
 * <P>Once it creates and intializes the arena, this class dynamically loads the specified tournament styles
 * and games and registers them. If at any point the initialization or loading fails, the main method
 * exists with status 1.</P>
 *
 * @author Allen Dutoit
 */
public class StartArena {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.server");
	
	private static Arena arena = Arena.getInstance();
	private static String codeBase = null;
	
	// Constants used by the setupDemo method for loading the tic tac toe example
	private static final String TICTACTOE = "TicTacToe";
	private static final String TICTACTOE_CLASSNAME = "org.globalse.arena.ttt.TicTacToe";
	private static final String TICTACTOEFACTORY_CLASSNAME = TICTACTOE_CLASSNAME + "MatchPanelFactory";
	private static final String KNOCK_OUT = "KnockOutStyle";
	private static final String KNOCKOUT_CLASSNAME = "org.globalse.arena.styles.KnockOutStyle";
	
	private static void tellUser(String message) {
		System.out.println(message);
	}
	
	private static PropertyLoader loadProperties(String fileName) {
		PropertyLoader propertyLoader = null;
		try {
			propertyLoader = new PropertyLoader(fileName);
		} catch (FileNotFoundException e) {
			tellUser("Properties file \"" + fileName + "\" not found.");
			System.exit(1);
		} catch (IOException e) {
			tellUser("An exception occured while reading from properties file \"" + fileName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return propertyLoader;
	}
	
	private static void initLogger(String fileName) {
		if (fileName != null) {
			LogManager logManager = LogManager.getLogManager();
			try {
				logManager.readConfiguration(new FileInputStream(new File(fileName)));
			} catch (Exception e) {
				tellUser("An exception occured while configuring the logger from properties file \"" + fileName + "\": " + e.getMessage());
			}
		}
	}
	
	private static void initArena(PropertyLoader propertyLoader) {
		try {
			Arena.init();
			arena = Arena.getInstance();
			// TODO: Use properties to determine the class name of the access policy.
			arena.setAccessPolicy(new DefaultAccessPolicy());
			// set the admnistrator
			String operatorName = propertyLoader.getStringProperty("Operator", "admin");
			String operatorPassword = propertyLoader.getStringProperty("OperatorPassword", "adminpass");
			User operator = arena.createUser(operatorName, operatorPassword);
			arena.setOperator(operator);
			int serverPort = propertyLoader.getIntProperty("ArenaPort", 1099);
			LocateRegistry.createRegistry(serverPort);
			tellUser("Registering arena on port " + serverPort + " ...");
			Naming.rebind("//localhost:" + serverPort + "/ArenaServer", arena);
			tellUser("... arena successfully registered as a remote object.");
		} catch (Exception e) {
			tellUser("Failed to initialize arena: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static Object getInstanceOfClass(String className) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, SecurityException, IllegalAccessException, IllegalArgumentException, MalformedURLException {
		Class result = null;
		try {
			tellUser("Loading class " + className + " from classpath.");
			result = Class.forName(className);
		} catch (ClassNotFoundException e) {
			tellUser(className + " not found in classpath, trying remote loading from codebase (" + codeBase + ").");
			result = RMIClassLoader.loadClass(codeBase, className);
		}
		return result.getMethod("getInstance", null).invoke(null, null);
	}
	
	private static void registerTournamentStyles(PropertyLoader propertyLoader) {
		String [] styleClassNames = propertyLoader.getStringArrayProperty("TournamentStyles");
		String styleClassName = null;
		boolean loadingSucceeded = false;
		try {
			for (int i = 0; i < styleClassNames.length; i++) {
				styleClassName = styleClassNames[i];
				String defaultStyleName = styleClassName.substring(styleClassName.lastIndexOf(".")+1);
				String styleName = propertyLoader.getStringProperty(styleClassName + ".name", defaultStyleName);
				TournamentStyle style = (TournamentStyle)getInstanceOfClass(styleClassName);
				arena.registerTournamentStyle(styleName, style);
			}
			loadingSucceeded = true;
		} catch (ClassNotFoundException e) {
			tellUser("Tournament style class \"" + styleClassName + "\" not found (neither in class path nor in codebase).");
		} catch (MalformedURLException e) {
			tellUser("Failed to remotely load tournament style class \"" + styleClassName +
						 "\" because codebase URL is not well-formed (\"" + codeBase + "\").");
		} catch (NoSuchMethodException e) {
			tellUser("Tournament style class \"" + styleClassName +
						 "\" does not define a public static method getInstance().");
		} catch (Exception e) {
			tellUser("Tournament style class \"" + styleClassName +
						 "\" static method getInstance() threw exception: " + e.getMessage());
			e.printStackTrace();
		}
		if (!loadingSucceeded) {
			System.exit(1);
		}
	}
	
	private static String registerOneGame(PropertyLoader propertyLoader, String gameClassName) throws MalformedURLException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, GameAlreadyExistsException {
		String defaultStyleName = gameClassName.substring(gameClassName.lastIndexOf(".")+1);
		String gameName = propertyLoader.getStringProperty(gameClassName + ".name", defaultStyleName);
		String gameDescription = propertyLoader.getStringProperty(gameClassName + ".description", "");
		Game game = (Game)getInstanceOfClass(gameClassName);
		String panelFactoryClassName = propertyLoader.getStringProperty(gameClassName + ".factory", null);
		if (panelFactoryClassName == null) {
			panelFactoryClassName = gameClassName + "MatchPanelFactory";
		}
		MatchPanelFactory matchPanelFactory = (MatchPanelFactory)getInstanceOfClass(panelFactoryClassName);
		arena.registerGame(game, gameName, gameDescription, matchPanelFactory);
		return gameName;
	}
	
	private static void registerGames(PropertyLoader propertyLoader) {
		String [] gameClassNames = propertyLoader.getStringArrayProperty("Games");
		String gameClassName = null;
		boolean loadingSucceeded = false;
		try {
			for (int i = 0; i < gameClassNames.length; i++) {
				gameClassName = gameClassNames[i];
				registerOneGame(propertyLoader, gameClassName);
			}
			loadingSucceeded = true;
		} catch (ClassNotFoundException e) {
			tellUser("Failed to load game class or associated match panel factory \"" + gameClassName + "\":" + e.getMessage());
		} catch (NoSuchMethodException e) {
			tellUser("Game class \"" + gameClassName + "\" does not define a static method getInstance().\n" + e.getMessage());
		} catch (Exception e) {
			tellUser("Game class \"" + gameClassName + "\" static method getInstance() threw exception: " + e.getMessage());
		}
		if (!loadingSucceeded) {
			System.exit(1);
		}
	}
	
	
	private static void setupDemo(PropertyLoader propertyLoader) {
		if (!propertyLoader.getBooleanProperty("SetupDemo", false)) {
			return;
		}
		String gameName = propertyLoader.getStringProperty("DemoGameName", "TicTacToe");
		tellUser("Setting up demo for game " + gameName);
		
		try {
			arena.getGameByName(gameName);
		} catch (GameNotFoundException e) {
			tellUser("Demo game \"" + gameName + "\" is not registered with arena.");
			tellUser("The Games and <game class>.name properties should be set for each game. For example:");
			tellUser("Games=org.globalse.arena.ttt.TicTacToe");
			tellUser("org.globalse.arena.ttt.TicTacToe.name=TicTacToe");
			System.exit(1);
		}
		
		User alice = null, joe = null, mike = null, mark = null, mary = null, bob = null;
		try {
			// Create demo users
			alice = arena.createUser("alice", "alicepass");
			joe = arena.createUser("joe", "joepass");
			mike = arena.createUser("mike", "mikepass");
			mark = arena.createUser("mark", "markpass");
			mary = arena.createUser("mary", "marypass");
			bob = arena.createUser("bob", "bobpass");
			
		} catch (Exception e) {
			tellUser("Failed to create demo users.");
			e.printStackTrace();
			System.exit(1);
		}
		
		// register the KO tournament style if needed
		try {
			if (arena.getTournamentStyleByName(KNOCK_OUT) == null) {
				TournamentStyle ko = (TournamentStyle)getInstanceOfClass(KNOCKOUT_CLASSNAME);
				arena.registerTournamentStyle(KNOCK_OUT, ko);
			}
		} catch (Exception e) {
			tellUser("Failed to load knock out tournament style.");
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			String operatorName = propertyLoader.getStringProperty("Operator", "admin");
			String operatorPassword = propertyLoader.getStringProperty("OperatorPassword", "adminpass");
			String operatorTicket = arena.login(operatorName, operatorPassword);
			tellUser("Operator logged in with ticket: " + operatorTicket);

			String bobTicket = arena.login("bob", "bobpass");
			tellUser("Bob logged in with ticket: " + bobTicket);
			LeagueInfo linfo;
			RemoteLeague l;
			TournamentInfo tinfo;
			RemoteTournament t;
			
			linfo = arena.createLeague(operatorTicket, bob, "Expert " + gameName + " League", "A restricted league for insiders.", gameName, KNOCK_OUT);
			l = linfo.getLeague();
			l.addPlayer(bobTicket, alice);
			l.addPlayer(bobTicket, joe);
			tinfo = linfo.getLeague().createTournament(bobTicket, "2003 Championship", "");
			t = tinfo.getTournament();
			t.openRegistration(bobTicket);
			t.acceptPlayer(bobTicket, alice);
			t.acceptPlayer(bobTicket, joe);
			t.closeRegistration(bobTicket);
			t.launch(bobTicket);
			tinfo = linfo.getLeague().createTournament(bobTicket, "2004 Championship", "");
			t = tinfo.getTournament();
			t.openRegistration(bobTicket);
			
			linfo = arena.createLeague(operatorTicket, bob, "Novice " + gameName + " League", "A simple, unrestricted league for beginners.", gameName, KNOCK_OUT);
			linfo.getLeague().unrestrict(bobTicket);
			tinfo = linfo.getLeague().createTournament(bobTicket, "Paper Cup", "An adhoc knockout tournament.");
			t = tinfo.getTournament();
			t.openRegistration(bobTicket);
			t.acceptPlayer(bobTicket, joe);
			t.acceptPlayer(bobTicket, mike);
			t.acceptPlayer(bobTicket, alice);
			t.acceptPlayer(bobTicket, mark);
			t.acceptPlayer(bobTicket, mary);
			t.closeRegistration(bobTicket);
			t.launch(bobTicket);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ArenaException e) {
			e.printStackTrace();
		}
		tellUser("Demo setup finished.");
	}
	
	public static void main(String[] args) {
		
		// Load properties from the arena specified file or URL
		String propertiesFileName = null;
		if (args.length > 0) {
			propertiesFileName = args[0];
		}
		PropertyLoader propertyLoader = loadProperties(propertiesFileName);
		
		// The code base property is used for loading games and tournament styles
		// If the CodeBase property is specified, initialize the corresponding
		// RMI code base property so that RMI clients can also dynamically load
		// the same objects.
		codeBase = propertyLoader.getStringProperty("CodeBase", null);
		if (codeBase != null) {
			System.setProperty("java.rmi.server.codebase", codeBase);
		}
		
		// Initialize the logger using the properties file.
		initLogger(propertiesFileName);
		
		// The RMI secutiry manager prevents dynamically loaded classes to access
		// local resources (e.g., the file system). Allow the creation of sockets
		// so that match front ends and game peers can still connect to this server,
		// overriding the site policy.
		System.setSecurityManager(new RMISecurityManager() {
					// This enables the arena server to accept RMI calls from match front ends
					// and game peers, regardless of the current policy
					public void checkAccept(String host, int port) {}
					// This enables the arena server to make connections to match front ends
					// to send notification events over the listener interfaces
					public void checkConnect (String host, int port) {}
					public void checkConnect (String host, int port, Object context) {}
				});
		
		// Create an arena and register it as a remote object
		initArena(propertyLoader);
		
		// Register tournament styles
		registerTournamentStyles(propertyLoader);
		
		// Register games
		registerGames(propertyLoader);
		
		// Create test objects if the SetupDemo property is set to true.
		setupDemo(propertyLoader);
		
		// Wait for RMI connections. Connections will be handeled in separate
		// threads created by RMI.
		while (true) {
			try { Thread.sleep(5000); } catch (InterruptedException e) {}
		}
	}
}

