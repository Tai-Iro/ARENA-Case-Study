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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.globalse.arena.remote.GameDescriptor;
import org.globalse.arena.remote.MatchPanelFactory;
import org.globalse.arena.remote.exceptions.GameAlreadyExistsException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;

/**
 * This class manages a map of games for an arena, including games, game names
 * descriptions, and match panel class names. The names and descriptions of games are kept
 * separate from the games themselves, so that: 1. the arena operator (as opposed
 * to the game developer) can resolve name clashes between games, and 2. different names,
 * descriptions, and match panels for the same game can be stored in different arenas (e.g.,
 * an English speaking and a German speaking arena).
 *
 * Assumptions:
 * - games are registered with the arena before leagues can be created for them
 * - match panel classes are remotely loadable from codebase.
 * - games are unregistered after all corresponding leagues have been archived
 *
 * @author Allen Dutoit
 */
public class GameManager {
	
	private Map entriesByName = new HashMap();
	private Map entriesByGame = new HashMap();
	
	private GameManager() {}
	private static GameManager instance = new GameManager();
	public static GameManager getInstance() {
		return instance;
	}
	
	private class GameEntry {
		private Game game;
		private String name;
		private String description;
		private MatchPanelFactory matchPanelFactory;
		
		GameEntry(Game game, String name, String description, MatchPanelFactory matchPanelFactory) {
			this.game = game;
			this.name = name;
			this.description = description;
			this.matchPanelFactory = matchPanelFactory;
		}
		
		String getName() {
			return name;
		}
		
		String getDescription() {
			return description;
		}
		
		Game getGame() {
			return game;
		}
		
		MatchPanelFactory getMatchPanelFactory() {
			return matchPanelFactory;
		}
	}
	
	synchronized public GameDescriptor [] getGameInfos() {
		List result = new ArrayList();
		for (Iterator i = entriesByName.values().iterator(); i.hasNext();) {
			GameEntry entry = (GameEntry)i.next();
			result.add(new GameDescriptor(entry.getName(), entry.getDescription()));
		}
		return (GameDescriptor[])result.toArray(new GameDescriptor[result.size()]);
	}
	
	synchronized public Game getGameByName(String name)
		throws GameNotFoundException {
		GameEntry entry = (GameEntry)entriesByName.get(name);
		if (entry == null) {
			throw new GameNotFoundException("Game " + name + " not found.");
		}
		return entry.getGame();
	}
	
	synchronized public String getGameName(Game game) throws GameNotFoundException {
		GameEntry entry = (GameEntry)entriesByGame.get(game);
		if (entry == null) {
			throw new GameNotFoundException("Game not registered in arena.");
		}
		return entry.getName();
	}
	
	synchronized public String getGameDescription(Game game) throws GameNotFoundException {
		GameEntry entry = (GameEntry)entriesByGame.get(game);
		if (entry == null) {
			throw new GameNotFoundException("Game not registered in arena.");
		}
		return entry.getDescription();
	}
	
	synchronized public MatchPanelFactory getMatchPanelFactory(String name) throws GameNotFoundException {
		GameEntry entry = (GameEntry)entriesByName.get(name);
		if (entry == null) {
			throw new GameNotFoundException("Game " + name + " not found.");
		}
		return entry.getMatchPanelFactory();
	}
	
	synchronized public void registerGame(Game game, String name, String description, MatchPanelFactory matchPanelFactory) throws GameAlreadyExistsException {
		GameEntry entry = (GameEntry)entriesByName.get(name);
		if (entry != null) {
			throw new GameAlreadyExistsException("Game with name \"" + name + "\" already exists.");
		}
		entry = new GameEntry(game, name, description, matchPanelFactory);
		entriesByName.put(name, entry);
		entriesByGame.put(game, entry);
	}
	
}


