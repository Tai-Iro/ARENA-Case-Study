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
package org.globalse.arena.ttt;

import javax.swing.JPanel;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.MatchPanelFactory;

public class TicTacToeMatchPanelFactory implements MatchPanelFactory {
	
	private TicTacToeMatchPanelFactory(){}
	private static TicTacToeMatchPanelFactory instance = new TicTacToeMatchPanelFactory();
	public static MatchPanelFactory getInstance() {
		return instance;
	}
	
	public JPanel createMatchPanel(String ticket, MatchInfo matchInfo) {
		return new TicTacToeMatchPanel(ticket, matchInfo);
	}
	
}

