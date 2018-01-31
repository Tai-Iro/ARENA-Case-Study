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

/**
 * This class provides utility methods for building a string representing an
 * HTML page. Most methods return a string that includes an opening and a closing
 * tag. The attributes of the tag and the content of the tag are specified with
 * the parameter of the methods. This allows the caller to nest several calls to
 * build a complex HTML statement.
 *
 * For example, the following calls create the HTML statements for a table with
 * two rows of two cells:
 *
 * <code>HTML.beginTable() +
 * HTML.tableRow(HTML.tableCell("A1") + HTML.tableCell("A2")) +
 * HTML.tabelRow(HTML.tableCell("B1") + HTML.tableCell("B2")) +
 * HTML.endTable()</code>
 *
 * This class provides utility methods for tables, text formating (italics, bold,
 * typewriter), forms (textArea, textField), and recurring statements (header,
 * footer, smallButton).
 *
 * Using the methods of this class instead of combining literal strings ensures
 * that the resulting HTML statement is well formed and that each tag is closed.
 *
 * @author Allen Dutoit
 * @author Daniela Ahlisch
 */
public class HTML {
		
    // HTML headers and footers
    public static String header(String title, String bgColor, String linkColor, String vlinkColor) {
		return "<HTML><HEAD><TITLE>" + title +
			"</TITLE></HEAD><BODY BGColor=\"" + bgColor +
			"\" LINK=\"" + linkColor + "\" VLINK=\"" + vlinkColor + "\">";
    }
	
    public static String header(String title, String bgColor) {
		return header(title, bgColor, "#0000FF", "#0000FF");
    }
	
    public static String header(String title) {
		return header(title, "#FFFFFF");
    }
	
    public static String title(String title) {
		return HTML.center("<H3>" + title + "</H3>");
    }
	
    public static String footer() {
		return "</BODY></HTML>";
    }
	
	// inline images
    public static String image(String path) {
		return "<IMG SRC=\"" + path + "\" BORDER=0>";
    }
	
    public static String image(String path, String width, String height) {
		return "<IMG SRC=\"" + path + "\" WIDTH=\"" + width +
			"\" HEIGHT=\"" + height + "\" BORDER=\"0\">";
    }
	
    public static String image(String path, int width, int height) {
		return image(path, String.valueOf(width), String.valueOf(height));
    }

	public static String definition(String term, String definition) {
		return "<DL><DT>" + term + "</DT><DD>" + definition + "</DD><DL>";
	}

    // font size and style
    public static String italics(String contents) {
		return "<I>" + contents + "</I>";
    }
	
    public static String center(String contents) {
		return "<CENTER>" + contents + "</CENTER>";
    }
	
    public static String bold(String contents) {
		return "<B>" + contents + "</B>";
    }
	
	public static String typewriter(String contents) {
		return "<TT>" + contents + "</TT>";
	}
	
    public static String bigger(String contents) {
		return "<FONT SIZE=\"+1\">" + contents + "</FONT>";
    }
	
    public static String smaller(String contents) {
		return "<FONT SIZE=\"-1\">" + contents + "</FONT>";
    }
	
	public static String blue(String contents) {
		return "<FONT COLOR=\"blue\">" + contents + "</FONT>";
	}
	
    public static String red(String contents) {
		return "<FONT COLOR=\"#FF0000\">" + contents + "</FONT>";
    }
	
    public static String orange(String contents) {
		return "<FONT COLOR=\"orange\">" + contents + "</FONT>";
    }

    public static String green(String contents) {
		return "<FONT COLOR=\"darkgreen\">" + contents + "</FONT>";
    }
	
	public static String black(String contents) {
		return "<FONT COLOR=\"black\">" + contents + "</FONT>";
	}
	
    public static String bracket(String str) {
		return "&#91;" + str + "&#93";
    }
	
    public static String smallButton(String str) {
		return smaller(bracket(str));
    }
	
	// forms
    public static String beginForm(String servlet) {
		return "<FORM method=\"POST\" action=\"" + servlet + "\">";
    }
	
    public static String endForm() {
		return "</FORM>";
    }

	// form fields
    public static String textArea(String name, int r, int c, String value) {
		if (value == null) {
			value = "";
		}
		return("<TEXTAREA name=\"" + name + "\" ROWS=\"" + r + "\" COLS=\"" + c + "\" WRAP=\"virtual\">" + value + "</TEXTAREA>");
    }
	
    public static String textField(String name, String value) {
		return textField(name, value, 30);
    }
	
    public static String textField(String name, String value, int size) {
		if (value == null) {
			value = "";
		}
		return "<INPUT TYPE=\"TEXT\" NAME=\"" + name + "\" VALUE=\"" + value + "\" SIZE=\"" + size + "\">";
    }
	
    public static String passwordField(String name, String value) {
		if (value == null) {
			value = "";
		}
		return "<INPUT TYPE=\"PASSWORD\" NAME=\"" + name + "\" VALUE=\"" + value + "\" SIZE=\"30\">";
    }
	
    public static String fileField(String name, int size, String value) {
		if (value == null) {
			value = "";
		}
		return "<INPUT TYPE=\"FILE\" NAME=\"" + name + "\" VALUE=\"" + value + "\" SIZE=\"" + size + "\">";
    }

    public static String button(String name, String value, String onclick) {
		if (value == null) {
			value = "";
		}
		return("<INPUT TYPE=\"BUTTON\" NAME=\"" + name + "\" VALUE=\"" + value + "\" ONCLICK=\"" + onclick + "\">");
    }
	
    public static String hiddenField(String name, String value) {
		return "<INPUT TYPE=\"HIDDEN\" NAME=\"" + name + "\" VALUE=\"" + value + "\">";
    }
	
    public static String checkbox(String name, String value, boolean checked) {
		if (checked) {
			return "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + name + "\" VALUE=\"" + value + "\" CHECKED>";
		}
		return "<INPUT TYPE=\"CHECKBOX\" NAME=\"" + name + "\" VALUE=\"" + value + "\">";
    }
	
    public static String radioButton(String name, String value, boolean selected) {
		if (selected) {
			return "<INPUT TYPE=\"RADIO\" NAME=\"" + name + "\" VALUE=\"" + value + "\" CHECKED>";
		}
		return "<INPUT TYPE=\"RADIO\" NAME=\"" + name + "\" VALUE=\"" + value + "\">";
    }
	
    public static String submitButton(String value) {
		return "<INPUT TYPE=\"SUBMIT\" VALUE=\"" + value + "\">";
    }
	
    public static String resetButton(String value) {
		return "<INPUT TYPE=\"RESET\" VALUE=\"" + value + "\">";
    }
	
    // Tables
    public static String beginTable(String width, String border, String spacing, String padding) {
	    //	return "<P><TABLE WIDTH=\"" + width + "\" BORDER=\"" + border +
		return "<TABLE WIDTH=\"" + width + "\" BORDER=\"" + border +
			"\" CELLSPACING=\"" + spacing + "\" CELLPADDING=\"" + padding + "\">";
    }
	
    public static String beginTable(String params) {
		//	return "<P><TABLE " + params + ">";
		return "<TABLE " + params + ">";
    }
	
    public static String beginTable() {
		return beginTable("100%", "0", "0", "2");
    }
	
    public static String endTable() {
		return "</TABLE></P>";
    }
	
    public static String tableRow(String params, String cells) {
		return "<TR " + params + ">" + cells + "</TR>\n";
    }
	
    public static String tableRow(String cells) {
		return tableRow("", cells);
    }
	
    public static String emptyRow() {
		return tableRow(emptyCells(1));
    }
	
    public static String tableCell(String params, String contents) {
		return "<TD " + params + ">" + contents + "</TD>";
    }
	
    public static String tableCell(String contents) {
		return tableCell("", contents);
    }
	
    public static String emptyCells(int numCells) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < numCells; i++) {
			res.append("<TD>&nbsp;</TD>");
		}
		return res.toString();
    }
	// frames
	public static String frameSet(String params, int frameBorder, String frames) {
		return "<FRAMESET " + params + "\" FRAMEBORDER=\"" + frameBorder + "\">" + frames + "</FRAMESET>";
	}

	public static String frameSet(String params, String frames) {
		return "<FRAMESET " + params + "\" FRAMEBORDER=\"" + 0 +
			"\" BORDER=\"0\" MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\">" + frames + "</FRAMESET>";
	}
	
	public static String frame(String src, String name, boolean scroll) {
		return frame(src, name, scroll, 0, 0, 0);
	}
	
	public static String frame(String src, String name, boolean scroll, int frameBorder, int marginWidth, int marginHeight) {
		String scrollStr = "";
		if (!scroll) {
			scrollStr = "SCROLLING=\"no\"";
		}
		return "<FRAME SRC=\"" + src + "\" NAME=\"" + name + "\" " + scrollStr +
			" FRAMEBORDER=\"" + frameBorder +
			"\" BORDER=\"0\" MARGINWIDTH=\"" + marginWidth +
			"\" MARGINHEIGHT=\"" + marginHeight + "\">";
	}
	// links
	public static String link(String href, String target, String label) {
		return "<A HREF=\"" + href + "\" TARGET=\"" + target + "\">" + label + "</A>";
	}

	public static String lightLink(String href, String target, String label) {
		return "<A STYLE=\"TEXT-DECORATION: NONE\" HREF=\"" + href + "\" TARGET=\"" + target + "\">" + label + "</A>";
	}
	
	// javascript
	public static String script(String code) {
		return "<SCRIPT>" + code + "</SCRIPT>";
	}
	
	// applets
	public static String applet(String code, String codeBase, String archive, String width, String height, String parameters) {
		return "<APPLET CODE=\"" + code +
			"\" CODEBASE=\"" + codeBase +
			"\" ARCHIVE=\"" + archive +
			"\" WIDTH=\"" + width +
			"\" HEIGHT=\"" + height + "\">" + parameters + "</APPLET>";
	}
	
	public static String parameter(String name, String value) {
		return "<PARAM NAME=\"" + name + "\" VALUE=\"" + value + "\">";
	}
}
