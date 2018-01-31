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
package org.globalse.arena.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.globalse.arena.remote.exceptions.InvalidLoginException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.remote.exceptions.UnknownUserException;
import org.globalse.arena.remote.exceptions.UserAlreadyExistsException;
import org.globalse.arena.user.User;

/**
 * This class is responsible for keeping track of user/password pairs within a
 * virtual machine. Users are authenticated by checking passwords and issuing
 * a timestampted and signed session. The session (or in its string form, a ticket)
 * is used to identify a user across several virtual machines and calls.
 *
 * @author Michael Nagel
 */

// TODO: store seed so that bboard posted tickets can still be validated.
public final class GateKeeper {
	
	private static Logger logger = Logger.getLogger("org.globalse.arena.user");
	private static Random random = new Random();
    private TokenSigner signer = new TokenSigner();
	private User guest = null;
	private Map usersById = new HashMap();
    private Map usersByName = new HashMap();
	private Map sessions = new HashMap();
	private Map passwords = new HashMap();
		
	public GateKeeper() {
		try {
			this.guest = createUser("guest", "guest");
		} catch (UserAlreadyExistsException e) {
			// this never happens since this is the first user created.
		}
	}
	
	synchronized public final User createUser(String username, String password)
		throws UserAlreadyExistsException
	{
		long id;
		do {
			// Use positive values only, because Long.toHexString() encodes the id
			// as an unsigned value. Using negative values would cause Long.parseLong()
			// to raise an exception when decoding the ticket, because of overflow.
			id = Math.abs(random.nextLong());
		} while (usersById.get(Long.toString(id)) != null);
		return createUser(username, password, id);
	}
	
	synchronized public final User createUser(String username, String password, long id)
		throws UserAlreadyExistsException {
		User user = (User)usersByName.get(username);
		if (user != null) {
			throw new UserAlreadyExistsException("A user with name \"" + username + "\" already exists.");
		}
		String idStr = Long.toString(id);
		user = (User)usersById.get(idStr);
		if (user != null) {
			throw new UserAlreadyExistsException("A user with id \"" + id + "\" already exists.");
		}
		user = new User(id, username);
		usersById.put(Long.toString(id), user);
		usersByName.put(username, user);
		passwords.put(username, password);
		return user;
	}
	
	synchronized public final void deleteUser(String username) throws UnknownUserException {
		User user = (User)usersByName.get(username);
		if (user == null) {
			throw new UnknownUserException("User with name \"" + username + "\" is not known.");
		}
		usersById.remove(Long.toString(user.getId()));
		usersByName.remove(username);
	}
	
    synchronized public final String login(String username, String password)
		throws InvalidLoginException {
		User user = (User)usersByName.get(username);
		if (user == null) {
			throw new InvalidLoginException("Login failed.");
		}
		String storedPassword = (String)passwords.get(username);
		if (storedPassword == null || !storedPassword.equals(password)) {
			throw new InvalidLoginException("Login failed.");
		}
		return getSession(user).getTicket();
    }

	synchronized public final String getGuestTicket() {
		return getSession(guest).getTicket();
	}

	synchronized public final boolean isUserGuest(User user) {
		return user.equals(guest);
	}
	
	private Session getSession(User user) {
		String username = user.getLogin();
		Session session = (Session)sessions.get(username);
		if (session == null) {
			session = new Session(user.getId());
			signer.signToken(session);
			sessions.put(username, session);
		}
		return session;
	}
	
    synchronized public final User getUser(String ticket) throws InvalidTicketException {
		Session session = new Session(ticket);
		// TODO: this should also time out sessions, at least the ones given to the players.
		if (!verifySession(session)) {
			logger.warning("Ticket could not be verified.");
			return null;
		}
		return (User)usersById.get(Long.toString(session.getUserId()));
    }
	
    private final boolean verifySession(Session session) {
		return signer.verifyToken(session);
    }
	
}
