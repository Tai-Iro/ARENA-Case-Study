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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author Michael Nagel
 */
public class TokenSigner {
	
	private static Random random = new Random();
	private String secret = null;
	
	public TokenSigner() {
		long sec = random.nextLong();
		secret = Long.toHexString(sec);
		while (secret.length() < 16)
			secret = "0" + secret;
	}
	
	private String getDigest(String message){
		if ( ( message == null ) || ( message.length() < 5 ) )
			return null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA");
		}
		catch ( NoSuchAlgorithmException e) {
			return null;
		}
		String ms = message + secret;
		byte[] digest = md.digest(ms.getBytes());
		String res = "";
		for ( int i = 0; i < digest.length; i++) {
			String comp = Integer.toHexString(digest[i]+256);
			if (comp.length() < 2)
				comp = "0"+comp;
			res = res + comp;
		}
		return res;
	}
	
	public boolean signToken(Token t) {
		String code = t.getCode();
		if ( code == null )
			return false;
		t.setSignature(getDigest(code));
		return true;
	}
	
	public boolean verifyToken(Token token) {
		String localDigest = getDigest(token.getCode());
		return localDigest.equalsIgnoreCase( token.getSignature() );
	}
	
}
