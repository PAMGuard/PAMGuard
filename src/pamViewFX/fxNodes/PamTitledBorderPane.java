/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package pamViewFX.fxNodes;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * A JavaFX class to emulate the Swing TitledBorder
 * taken from https://stackoverflow.com/questions/14860960/groupbox-titledborder-in-javafx-2
 * 
 * The bordered-titled style can be found in style sheet pamDefaultDialogCSS.css.  It might need some tweaking to
 * get the position of the text correct, but it's really very fiddly.  I came up with -10 after a LOT of trial and error
 * in both the positive and negative directions.  Easy to make the text jump using large values, but when using small
 * numbers in order to just move the text halfway down it was very sensitive - sometimes it jumped a lot, sometimes
 * it didn't move at all.
 * 
 * @author mo55
 *
 */
public class PamTitledBorderPane extends StackPane {

	/**
	 * 
	 */
	public PamTitledBorderPane(String titleString, Node content) {
	    Label title = new Label(" " + titleString + " ");
	    title.getStyleClass().add("bordered-titled-title");
	    StackPane.setAlignment(title, Pos.TOP_LEFT);

	    StackPane contentPane = new StackPane();
	    content.getStyleClass().add("bordered-titled-content");
	    contentPane.getChildren().add(content);

	    getStyleClass().add("bordered-titled-border");
	    getChildren().addAll(title, contentPane);
	  }
}
