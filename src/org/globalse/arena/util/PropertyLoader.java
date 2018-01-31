/*
 * Copyright 2004 (C) Applied Software Engineering--TU Muenchen
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
package org.globalse.arena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * This class is a wrapper around a properites object, hiding the complexity of
 * loading properties from a file or a url, and dealing with default values.
 * 
 * @author Timo Wolf
 * @author Allen Dutoit
 */
public class PropertyLoader {

	private Properties properties = null;

	public PropertyLoader(String url) throws IOException {
		if (url == null) {
			throw new NullPointerException("Parameter url cannot be null.");
		}
		InputStream in = null;
		if (url.startsWith("http:")) {
			in = (new URL(url)).openStream();
		} else {
			in = new FileInputStream(new File(url));
		}
		properties = new Properties();
		properties.load(in);
	}

	public String getStringProperty(String name, String defaultValue) {
		String ret = properties.getProperty(name, defaultValue);
		if (ret != null) {
			return ret.trim();
		} else {
			return ret;
		}
	}

	public String[] getStringArrayProperty(String name) {
		if (properties.containsKey(name)) {
			String values = properties.getProperty(name);
			return values.split(" ");
		}
		return new String[0];
	}

	public int getIntProperty(String name, int defaultValue) {
		return Integer.parseInt(properties.getProperty(name, Integer
				.toString(defaultValue)));
	}

	public boolean getBooleanProperty(String name, boolean defaultValue) {
		if (properties.containsKey(name)) {
			String valueStr = properties.getProperty(name);
			return valueStr.equals("true") || valueStr.equals("yes");
		}
		return defaultValue;
	}

}
