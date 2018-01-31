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
package org.globalse.arena.remote;

import java.util.logging.Level;

/**
 * This class provides a simple remote logging facility for an arena. Clients such as
 * MatchFrontEnds need such a logging facility when they are started in a restricted sandbox.
 * Instances of this class are created in the client and forward logs to the arena.
 * All exceptions thrown by the arena server are caught so that the logging method does not
 * interrupt the normal control flow of the client.
 *
 * @author Allen Dutoit
 */
public class RemoteLogger {
	
	private RemoteArena arena;
	
	public RemoteLogger(RemoteArena arena) {
		this.arena = arena;
	}
	
	public void log(Level level, String className, String message) {
		try {
			arena.log(level, className, message);
		} catch(Exception e){}
	}
}

