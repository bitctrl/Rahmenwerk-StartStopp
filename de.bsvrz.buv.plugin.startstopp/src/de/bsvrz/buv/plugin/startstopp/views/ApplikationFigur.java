/*
 * Segment 10 System (Sys), SWE 10.1 StartStopp - Plugin
 * Copyright (C) 2007-2017 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.buv.plugin.startstopp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung.Warteart;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.util.Util;

public abstract class ApplikationFigur implements PaintListener {

	private boolean start;
	private String name;
	private String rechner;
	private List<ApplikationFigur> referenzen = new ArrayList<>();
	private Inkarnation inkarnation;
	private boolean kernSystem;

	private int x;
	private int y;
	private final int textWidth;
	private final int textHeight;

	protected ApplikationFigur(GC gc, boolean start, String name) {
		this(gc, start, name, null, null, null);
	}

	protected ApplikationFigur(GC gc, boolean start, String name, String rechner, List<ApplikationFigur> vorgaenger,
			Inkarnation inkarnation) {
		this.start = start;
		this.name = name;
		this.rechner = rechner;
		if (vorgaenger != null) {
			this.referenzen.addAll(vorgaenger);
		}
		this.inkarnation = inkarnation;
		textWidth = gc.stringExtent(name).x;
		textHeight = gc.stringExtent(name).y;

	}

	@Override
	public void paintControl(PaintEvent e) {

		if (kernSystem) {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		} else if (rechner != null) {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		} else {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		}

		e.gc.fillRectangle(x, y, getWidth(), getHeight());
		e.gc.drawRectangle(x, y, getWidth(), getHeight());
		e.gc.drawString(name, x + (getWidth() - textWidth) / 2, y + 10);

		String rechnerStr;
		if (rechner == null) {
			rechnerStr = "lokal";
		} else {
			rechnerStr = rechner;
		}
		
		int rechnerStrWidth = e.gc.stringExtent(rechnerStr).x;
		e.gc.drawString(rechnerStr, x + (getWidth() - rechnerStrWidth) / 2, y + 10 + rechnerStrWidth + 5);
	}

	void setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
	}

	int getWidth() {
		return Math.max(150, textWidth + 20);
	}

	int getHeight() {
		return textHeight * 2 + 25;
	}

	List<ApplikationFigur> getReferenzen() {
		return referenzen;
	}

	public void setReferenzen(List<ApplikationFigur> referenzen) {
		this.referenzen.clear();
		this.referenzen.addAll(referenzen);
	}

	public int getReferenzOffset() {
		int result = 0;

		if (referenzen.isEmpty()) {
			return result;
		}

		boolean startOnBeginn = false;
		if (start && inkarnation != null && inkarnation.getStartBedingung() != null) {
			startOnBeginn = inkarnation.getStartBedingung().getWarteart() == Warteart.BEGINN;
		}
		for (ApplikationFigur figur : referenzen) {
			if (startOnBeginn) {
				result = Math.max(result, figur.getReferenzOffset());
			} else {
				result = Math.max(result, figur.getReferenzOffset() + figur.getWidth());
			}
		}

		long warteZeit = 0;
		if (start && inkarnation != null && inkarnation.getStartBedingung() != null) {
			try {
				warteZeit = Util.convertToWarteZeitInMsec(inkarnation.getStartBedingung().getWartezeit()) / 1000;
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (!start && inkarnation != null && inkarnation.getStoppBedingung() != null) {
			try {
				warteZeit = Util.convertToWarteZeitInMsec(inkarnation.getStoppBedingung().getWartezeit()) / 1000;
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result + (int) warteZeit;
	}

	void setKernSystem(boolean kernSystem) {
		this.kernSystem = kernSystem;
	}

	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	boolean isKernSystem() {
		return kernSystem;
	}

	String getName() {
		return name;
	}

	StartBedingung getStartBedingung() {
		if (inkarnation == null) {
			return null;
		}
		return inkarnation.getStartBedingung();
	}

	StoppBedingung getStoppBedingung() {
		if (inkarnation == null) {
			return null;
		}
		return inkarnation.getStoppBedingung();
	}

}
