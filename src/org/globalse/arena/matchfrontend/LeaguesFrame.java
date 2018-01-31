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
package org.globalse.arena.matchfrontend;

import javax.swing.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.globalse.arena.remote.MatchInfo;
import org.globalse.arena.remote.RemoteTournament;
import org.globalse.arena.remote.TournamentInfo;
import org.globalse.arena.remote.exceptions.ArenaException;
import org.globalse.arena.remote.exceptions.GameNotFoundException;
import org.globalse.arena.remote.exceptions.InvalidTicketException;
import org.globalse.arena.user.AccessPolicy;
import org.globalse.arena.user.User;

/**
 * This class is a JFrame containing a JTree displaying the Leagues, Tournaments,
 * Rounds, and Matches for a specific game. This class uses the match panel factory
 * for the specified game from the arena server.
 *
 * @author Michael Nagel
 */
public class LeaguesFrame extends JFrame implements TreeModelListener, TreeSelectionListener {
	
	private String gameName;
    private LeagueTreeModel treeModel;
    private JTree tree;
	private JButton apply;
	private JButton accept;
	private JButton launch;
	private JButton play;
	
    public LeaguesFrame(String gameName) throws RemoteException, InvalidTicketException, GameNotFoundException {
		this.gameName = gameName;
		this.treeModel = new LeagueTreeModel(gameName);
		init();
		pack();
		setLocationRelativeTo(null); //center it
    }
	
    private void init() {
		addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						dispose();
						try {
							treeModel.leaveArena();
						} catch (Exception ex) {
							// TODO: Since we are exiting, no need to handle this exception.
						}
						System.exit(0);
					}
				});
		setTitle("Welcome to ARENA " + MatchFrontEnd.getUser().getLogin());
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		scrollpane.getViewport().add(tree);
		
		panel.add(scrollpane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
													 GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
														 0, 0, 0, 0), 0, 0));
		
		JPanel buttonPanel = new JPanel();
		
		apply = new JButton("Apply for tournament");
		accept = new JButton("Accept player");
		launch = new JButton("Launch tournament");
		play = new JButton("Watch match");
		
		apply.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						applyForTournament();
					}
				});
		
		accept.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						acceptOrRejectPlayer();
					}
				});
		
		launch.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						advanceTournamentState();
					}
				});
		
		play.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						playOrWatchMatch();
					}
				});
		
		apply.setEnabled(false);
		accept.setEnabled(false);
		launch.setEnabled(false);
		play.setEnabled(false);
		
		buttonPanel.add(apply);
		buttonPanel.add(accept);
		buttonPanel.add(launch);
		buttonPanel.add(play);
		
		panel.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
													  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
													  new Insets(0, 0, 0, 0), 0, 0));
		
		getContentPane().add(panel);
		
		try {
			treeModel.loadLeagueInfo();
			treeModel.addTreeModelListener(this);
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "ARENA Error.",
										  JOptionPane.ERROR_MESSAGE);
		}
    }
	
	private void unselectAll() {
		apply.setEnabled(false);
		accept.setEnabled(false);
		launch.setEnabled(false);
		play.setEnabled(false);
	}
	
	private void matchNodeSelected(LeagueTreeNode ltn, MatchInfo matchInfo) {
		apply.setEnabled(false);
		accept.setEnabled(false);
		launch.setEnabled(false);
		play.setEnabled(true);
		try {
			if (matchInfo.hasPlayer(MatchFrontEnd.getUser())) {
				play.setText("Play match");
			} else {
				play.setText("Watch match");
			}
		} catch (Exception ex) {
			play.setText("Watch match");
		}
	}
	
	private void userNodeSelected(LeagueTreeNode ltn, User user) {
		TournamentInfo tournamentInfo = ltn.getTournamentInfo();
		RemoteTournament tournament = tournamentInfo.getTournament();
		if (user != null) {
			apply.setEnabled(false);
			launch.setEnabled(false);
			play.setEnabled(false);
			try {
				if (!tournament.hasAccess(MatchFrontEnd.getTicket(), AccessPolicy.MANAGE)) {
					accept.setEnabled(false);
				} else {
					accept.setEnabled(true);
					if (tournamentInfo.isPlayerAccepted(user)) {
						accept.setText("Reject player");
					} else {
						accept.setText("Accept player");
					}
				}
			} catch (RemoteException ex) {} catch (InvalidTicketException ex) {}
		}
	}
	
	private void tournamentNodeSelected(LeagueTreeNode ltn, TournamentInfo tournamentInfo) {
		RemoteTournament tournament = tournamentInfo.getTournament();
		boolean player = false;
		try {
			player = tournament.hasAccess(MatchFrontEnd.getTicket(), AccessPolicy.READ);
		} catch (RemoteException ex) {} catch (InvalidTicketException ex) {}
		apply.setEnabled(player && tournamentInfo.getState().equals(RemoteTournament.REGISTRATION));
		accept.setEnabled(false);
		boolean manager = false;
		try {
			manager = tournament.hasAccess(MatchFrontEnd.getTicket(), AccessPolicy.MANAGE);
		} catch (RemoteException ex) {} catch (InvalidTicketException ex) {}
		if (manager) {
			launch.setEnabled(true);
			if (tournamentInfo.getState().equals(RemoteTournament.INITIALIZING)) {
				launch.setText("Open registration");
			} else if (tournamentInfo.getState().equals(RemoteTournament.REGISTRATION)) {
				launch.setText("Close registration");
			} else if (tournamentInfo.getState().equals(RemoteTournament.REGISTRATIONFINISHED)) {
				launch.setText("Launch");
			} else {
				launch.setText("Terminate");
			}
		} else {
			launch.setEnabled(false);
		}
		play.setEnabled(false);
	}
	
	public void valueChanged(TreeSelectionEvent e) {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (obj == null || !(obj instanceof LeagueTreeNode)) {
			return;
		}
		
		LeagueTreeNode ltn = (LeagueTreeNode) obj;
		
		MatchInfo matchInfo = ltn.getMatchInfo();
		User user = ltn.getPlayer();
		TournamentInfo tournamentInfo = ltn.getTournamentInfo();
		if (matchInfo != null) {
			matchNodeSelected(ltn, matchInfo);
		} else if (user != null) {
			userNodeSelected(ltn, user);
		} else if (tournamentInfo != null) {
			tournamentNodeSelected(ltn, tournamentInfo);
		}
	}
	
    protected void playOrWatchMatch() {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (!(obj instanceof LeagueTreeNode)) return;
		
		LeagueTreeNode ltn = (LeagueTreeNode) obj;
		
		if (ltn.getMatchInfo() == null) return;
		
		MatchInfo matchInfo = ltn.getMatchInfo();
		try {
			JFrame frame = MatchFrontEnd.createMatchFrame(matchInfo, gameName);
			frame.setVisible(true);
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "ARENA Error", JOptionPane.WARNING_MESSAGE);
		}
    }
	
    protected void applyForTournament() {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (!(obj instanceof LeagueTreeNode)) return;
		
		LeagueTreeNode smtn = (LeagueTreeNode) obj;
		TournamentInfo tournamentInfo = smtn.getTournamentInfo();
		
		if (tournamentInfo == null) return;
		
		try {
			tournamentInfo.getTournament().apply(MatchFrontEnd.getTicket());
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "ARENA Error", JOptionPane.WARNING_MESSAGE);
		}
    }
	
	protected void acceptOrRejectPlayer() {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (!(obj instanceof LeagueTreeNode)) return;
		
		LeagueTreeNode ltn = (LeagueTreeNode) obj;
		TournamentInfo tournamentInfo = ltn.getTournamentInfo();
		User player = ltn.getPlayer();
		if (tournamentInfo == null || player == null) return;
		try {
			String ticket = MatchFrontEnd.getTicket();
			if (tournamentInfo.isPlayerAccepted(player)) {
				tournamentInfo.getTournament().rejectPlayer(ticket, player);
			} else {
				tournamentInfo.getTournament().acceptPlayer(ticket, player);
			}
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "ARENA Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
    protected void advanceTournamentState() {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (!(obj instanceof LeagueTreeNode)) return;
		
		LeagueTreeNode smtn = (LeagueTreeNode) obj;
		TournamentInfo tournamentInfo = smtn.getTournamentInfo();
		
		if (tournamentInfo == null) return;
		
		try {
			String ticket = MatchFrontEnd.getTicket();
			RemoteTournament tournament = tournamentInfo.getTournament();
			if (tournamentInfo.getState().equals(RemoteTournament.INITIALIZING)) {
				tournament.openRegistration(ticket);
			} else if (tournamentInfo.getState().equals(RemoteTournament.REGISTRATION)) {
				tournament.closeRegistration(ticket);
			} else if (tournamentInfo.getState().equals(RemoteTournament.REGISTRATIONFINISHED)) {
				tournament.launch(ticket);
			} else {
				tournament.terminate(ticket);
			}
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "Connection to the arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "ARENA Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	protected void launchTournament() {
		Object obj = tree.getLastSelectedPathComponent();
		
		if (!(obj instanceof LeagueTreeNode)) return;
		
		LeagueTreeNode ltn = (LeagueTreeNode) obj;
		TournamentInfo tournamentInfo = ltn.getTournamentInfo();
		
		if (tournamentInfo == null) return;
		
		try {
			tournamentInfo.getTournament().launch(MatchFrontEnd.getTicket());
		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "Connection to arena server failed.",
										  JOptionPane.ERROR_MESSAGE);
		} catch (ArenaException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
										  "ARENA Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
    public void treeNodesChanged(TreeModelEvent p1) {
		TreeSelectionModel selectionModel = tree.getSelectionModel();
		TreePath p = selectionModel.getSelectionPath();
		selectionModel.clearSelection();
		selectionModel.setSelectionPath(p);
	}
	
    public void treeNodesInserted(TreeModelEvent p1) {
		tree.getSelectionModel().clearSelection();
	}
	
    public void treeNodesRemoved(TreeModelEvent p1) {
		tree.getSelectionModel().clearSelection();
	}
	
    public void treeStructureChanged(TreeModelEvent p1) {
		TreeSelectionModel selectionModel = tree.getSelectionModel();
		TreePath p = selectionModel.getSelectionPath();
		selectionModel.clearSelection();
		selectionModel.setSelectionPath(p);
	}
	
}
