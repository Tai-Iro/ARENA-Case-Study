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

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.globalse.arena.remote.exceptions.AccessDeniedException;
import org.globalse.arena.remote.exceptions.InvalidMoveException;
import org.globalse.arena.remote.exceptions.InvalidStateException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.User;

/**
 * This is the public interface for remotely accessing a match.
 *
 * A RemoteMatch is a remote object accessible via RMI that sends notification
 * events to a arbitrary number of subscribing RemoteMatchListeners. In the observer
 * pattern, RemoteMatch corresponds to the Subject superclass while
 * RemoteMatchListener correspond to the Observer interface.
 *
 * Tournaments and Rounds on the arena server use RemoteMatch to set up
 * matches which may be running in a GamePeer in a different virtual machine. This
 * interface is also used when matches are created locally in the arena server
 * (e.g., because no game peers are registered).
 *
 * A MatchPanel also uses this interface to register RemoteMatchListeners
 * and access the current state of the Match.
 *
 * @author Allen Dutoit
 * @author Michael Nagel
 */
public interface RemoteMatch extends Remote {

    public final static String INITIALIZING = "initializing";
    public final static String CONNECTING = "waiting for opponents";
    public final static String PLAYING = "match playing";
    public final static String FINISHED = "match ended";
	public final static String TERMINATED = "match terminated before end";

	/**
	 * This method returns a snapshot of the current state of the match. The returned
	 * MatchInfo is not updated as the remote match changes.
	 *
	 * @return   a MatchInfo
	 *
	 * @exception   RemoteException
	 *
	 */
	public MatchInfo getInfo()
		throws RemoteException;

	/**
	 * This method returns a partial ranking of players after the completion of a
	 * match. The first element of the returned array returns the winners, which can
	 * be more than one if there is a tie. This method throws an InvalidStateException
	 * if it is called before the normal completion of the match.
	 *
	 * @return   an User[][]
	 *
	 * @exception   RemoteException
	 * @exception   InvalidStateException
	 *
	 */
    public User[][] getRanks()
		throws RemoteException, InvalidStateException;

	/**
	 * This method sets the state of the match to CONNECTING, allowing players and
	 * spectators to register to view the match. This method is typically invoked by
	 * the round class when the round is opened.
	 *
	 * This method throws an InvalidStateException if the state of the match was not INITIALIZING,
	 * an InvalidTicketException if the specified ticket has expired,  and
	 * an AccessDeniedException if the ticket does not correspond to a user who
	 * is allowed to open matches.
	 *
	 * @param    ticket              a  string representing the logged in user.
	 *
	 * @exception   RemoteException
	 * @exception   InvalidTicketException
	 * @exception   AccessDeniedException
	 * @exception   InvalidStateException
	 *
	 */
    public void open(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;

	/**
	 * This method registers a listener for a player, indicating that the player is
	 * ready to compete the match. When all players have joined the match, the match
	 * is started and its state is set to PLAYING. The method returns a match ticket that
	 * the player should use subsequently when playing moves.
	 *
	 * This method throws an InvalidStateException if the state of the match was not
	 * CONNECTING, an InvalidTicketException if the specified ticket has expired, and
	 * an AccessDeniedException if the ticket does not correspond to a player. This method
	 * is typically invoked by the match front end of the player.
	 *
	 * @param    ticket              a  string representing the logged in player
	 * @param    listener            a  RemoteMatchListener to which notification events should be sent
	 *
	 * @return   a match ticket to be used when playing moves
	 *
	 * @exception   RemoteException
	 * @exception   InvalidTicketException
	 * @exception   AccessDeniedException
	 * @exception   InvalidStateException
	 *
	 */
	public String join(String ticket, RemoteMatchListener listener)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidStateException;
	
	/**
	 * This method registers a listener for a spectator. The starting match event,
	 * each move, and the end of the match will be sent to the specified listener. Only the
	 * order of the events is guaranteed, it is possible that spectator listeners lag
	 * behind player listeners to make sure players are notified in the best possible time. This method
	 * is typically invoked by the match front end of the spectator.
	 *
	 * @param    listener            a  RemoteMatchListener to which notification events should be sent
	 *
	 * @return   an array representing the sequence of moves that have been played so far.
	 *
	 * @exception   RemoteException
	 *
	 */
	public Move[] watch(RemoteMatchListener listener)
		throws RemoteException;
	
	/**
	 * This method unregisters a listener for this match. After invoking this method, the
	 * specified listener will not receive events about this match anymore. If the specified
	 * listener corresponds to a player, the concrete match class may decide that the player
	 * has forfeited the match and rank the player accordingly.
	 *
	 * This method is typically invoked by the match front end. The match itself invokes
	 * this method to unregister misbehaving listeners that have thrown exceptions.
	 *
	 * @param    listener            a  RemoteMatchListener that was registered using either the join or watch method
	 *
	 * @exception   RemoteException
	 *
	 */
	public void leave(RemoteMatchListener listener)
		throws RemoteException;
	
	/**
	 * This method terminates the match, regardless the state it is currently in. The
	 * player ranking after a terminated match can vary from game to game. This
	 * method can only be invoked by the owner of the tournament.
	 *
	 * @param    ticket              a  string representing a logged in user
	 *
	 * @exception   RemoteException
	 * @exception   InvalidTicketException
	 * @exception   AccessDeniedException
	 * @exception   InvalidStateException
	 *
	 */
	public void terminate(String ticket)
		throws RemoteException, InvalidTicketException, AccessDeniedException;
	
	/**
	 * This method specifies the next move of the player corresponding to the
	 * specified match ticket. The match ticket is the string returned by the join
	 * method when the player joined the match.
	 *
	 * This method throws an InvalidTicketException if the ticket is not well-formed,
	 * an AccessDeniedException, if the corresponding user is not a player, and an
	 * InvalidMoveException if the move is not allowed by the game rules.
	 *
	 * @param    matchTicket         a  String
	 * @param    move                a  Move
	 *
	 * @exception   RemoteException
	 * @exception   InvalidTicketException
	 * @exception   AccessDeniedException
	 * @exception   InvalidMoveException
	 *
	 */
	public void playMove(String matchTicket, Move move)
		throws RemoteException, InvalidTicketException, AccessDeniedException, InvalidMoveException;
}

