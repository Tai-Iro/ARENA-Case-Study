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
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.globalse.arena.remote.exceptions.ArenaException;

public abstract class View {
	
	public String expandLink(Object currentObject, String partialLink, List objectsToExpand) {
		for (Iterator i = objectsToExpand.iterator(); i.hasNext();) {
			partialLink += "&" + Controller.EXPAND + "=" + Controller.getId(i.next());
		}
		return HTML.link(partialLink + "&" + Controller.EXPAND + "=" + Controller.getId(currentObject),"",
						 HTML.image("right.gif"));
	}
	
	public String collapseLink(Object currentObject, String partialLink, List objectsToExpand) {
		for (Iterator i = objectsToExpand.iterator(); i.hasNext();) {
			Object object = i.next();
			if (object != currentObject) {
				partialLink += "&" + Controller.EXPAND + "=" + Controller.getId(object);
			}
		}
		return HTML.link(partialLink, "", HTML.image("down.gif"));
	}
	
	public abstract void doAction(String ticket, HttpServletRequest req, HttpServletResponse res) throws IOException, ArenaException;
	
}

