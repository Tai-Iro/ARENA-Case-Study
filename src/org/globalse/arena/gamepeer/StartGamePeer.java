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
package org.globalse.arena.gamepeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.util.PropertyLoader;

/**
 * This is class provides the main method for starting a game peer. It takes a
 * single command line argument which specified the location (i.e., filename or URL) for
 * the properties file. The properties understood by <code>StartGamePeer</code> include:
 * <UL>
 *     <LI><code>ArenaHost</code> (default localhost) the hostname on which the arena server is registered</LI>
 *     <LI><code>ArenaPort</code> (default 1099)      the TCP/IP port on which the arena server listens for new RMI connections</LI>
 *     <LI><code>Operator</code>  (default admin)     the user name of the operator</LI>
 *     <LI><code>OperatorPassword</code> (no default) the operator password</LI>
 * </UL>
 * In addition, this class will use the properties file to initialize the loggers. See the
 * documentation on <code>java.util.logging</code> for information about logging properties.
 *
 * @author Allen Dutoit
 */
public class StartGamePeer {
	
	private static Logger logger = null;
	
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
		logger = Logger.getLogger("org.globalse.arena.gamepeer");
	}
	
	private static void initGamePeer(PropertyLoader propertyLoader) {
		String arenaHost = propertyLoader.getStringProperty("ArenaHost", "localhost");
		int arenaPort = propertyLoader.getIntProperty("ArenaPort", 1099);
		String operatorName = propertyLoader.getStringProperty("Operator", "admin");
		String operatorPassword = propertyLoader.getStringProperty("OperatorPassword", null);
		boolean initializationSucceeded = false;
		
		try {
			new GamePeer(arenaHost, arenaPort, operatorName, operatorPassword);
			initializationSucceeded = true;
		} catch (NotBoundException e) {
			logger.severe("Not such server at specified host (" + arenaHost + ") and port (" + arenaPort + ").");
		} catch (MalformedURLException e) {
			logger.severe("Malformed URL: rmi://" + arenaHost + ":" + arenaPort + "/ArenaServer");
		} catch (InvalidLoginException e) {
			logger.severe("Invalid operator name or password.");
		} catch (RemoteException e) {
			logger.severe("Connection to arena failed: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.severe("Failed to start game peer: " + e.getMessage());
			e.printStackTrace();
		}
		if (!initializationSucceeded) {
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length != 1) {
			tellUser("Expected exactly one argument, either a file name or a url specifying the game peer properties file.");
			System.exit(1);
		}
		// Load the properties file
		String propertiesFileName = args[0];
		PropertyLoader propertyLoader = loadProperties(propertiesFileName);
		
		// Configure the logger from the same properties file
		initLogger(propertiesFileName);
		
		// Set the RMI security manager so that Game and Match classses can be dynamically loaded.
		System.setSecurityManager(new RMISecurityManager() {
					// TODO: Check if this makes sense.
					public void checkConnect (String host, int port) {}
					public void checkConnect (String host, int port, Object context) {}
				});
		
		// Create game peer
		initGamePeer(propertyLoader);
		
		// Wait for create match requests from the arena server.
		while (true) {
			try { Thread.sleep(5000); } catch (InterruptedException e) {}
		}
	}
}

