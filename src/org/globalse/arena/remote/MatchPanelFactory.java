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

import org.globalse.arena.remote.MatchInfo;
import java.io.Serializable;
import javax.swing.JPanel;

/**
 * Interface implemented by game specific factories for creating match
 * panels. Match panel factories are registered with games in the arena server
 * when the arena server is started up.
 *
 * @author Allen Dutoit
 */
public interface MatchPanelFactory extends Serializable {
	
	public JPanel createMatchPanel(String ticket, MatchInfo matchInfo);
}

