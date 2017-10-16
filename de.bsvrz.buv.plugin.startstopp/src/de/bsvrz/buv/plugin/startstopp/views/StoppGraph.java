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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.util.Util;

public class StoppGraph extends Canvas implements PaintListener {

	private StartStoppSkript skript;
	private List<ApplikationFigur> figuren = new ArrayList<>();
	private StartStoppClient client;

	private Point origin = new Point(0,0);
	private int graphHeight;
	private int graphWidth;


	public StoppGraph(Composite parent) {
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_REDRAW_RESIZE);
		setLayout(null);
		addPaintListener(this);

		final ScrollBar hBar = getHorizontalBar();
		hBar.addListener(SWT.Selection, e -> {
			int hSelection = hBar.getSelection();
			int destX = -hSelection - origin.x;
			scroll(destX, 0, 0, 0, graphWidth, graphHeight, false);
			origin.x = -hSelection;
			redraw();
		});
		final ScrollBar vBar = getVerticalBar();
		vBar.addListener(SWT.Selection, e -> {
			int vSelection = vBar.getSelection();
			int destY = -vSelection - origin.y;
			scroll(0, destY, 0, 0, graphWidth, graphHeight, false);
			origin.y = -vSelection;
			redraw();
		});
		addListener(SWT.Resize, e -> {
			Rectangle client = getClientArea();
			hBar.setMaximum(graphWidth);
			vBar.setMaximum(graphHeight);
			hBar.setThumb(Math.min(graphWidth, client.width));
			vBar.setThumb(Math.min(graphHeight, client.height));
			int hPage = graphWidth - client.width;
			int vPage = graphHeight - client.height;
			int hSelection = hBar.getSelection();
			int vSelection = vBar.getSelection();
			if (hSelection >= hPage) {
				if (hPage <= 0)
					hSelection = 0;
				origin.x = -hSelection;
			}
			if (vSelection >= vPage) {
				if (vPage <= 0)
					vSelection = 0;
				origin.y = -vSelection;
			}
			redraw();
		});
		
		Menu popupMenu = new Menu(this);
		MenuItem refreshItem = new MenuItem(popupMenu, SWT.CASCADE);
		refreshItem.setText("Aktualisieren");
		refreshItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (client != null) {
					setClient(client);
				}
			}
		});
		this.setMenu(popupMenu);

		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (client != null) {
					setClient(client);
				}
			}
		});
	}

	public void setClient(StartStoppClient client) {
		try {
			this.client = client;
			skript = client.getCurrentSkript();
			berechneApplikationsAbhaengigkeiten();
		} catch (StartStoppException e) {
			figuren.clear();
		}
		this.redraw();
	}

	private void berechneApplikationsAbhaengigkeiten() {

		GC gc = new GC(this);

		figuren.clear();

		Map<String, ApplikationFigur> figurenMap = new LinkedHashMap<>();
		List<KernSystem> kernSysteme = skript.getGlobal().getKernsysteme();
		List<ApplikationFigur> otherApps = new ArrayList<>();

		Map<String, Inkarnation> inkarnationen = new LinkedHashMap<>();
		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			inkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
			ApplikationFigur figur = new StoppApplikationFigur(gc, inkarnation.getInkarnationsName());

			for (KernSystem kernSystem : kernSysteme) {
				if (kernSystem.getInkarnationsName().equals(figur.getName())) {
					figur.setKernSystem(true);
					break;
				}
			}
			figurenMap.put(figur.getName(), figur);
			if (!figur.isKernSystem()) {
				otherApps.add(figur);
			}
		}

		for (KernSystem kernSystem : kernSysteme) {
			Inkarnation inkarnation = inkarnationen.remove(kernSystem.getInkarnationsName());
			if (inkarnation == null) {
				continue;
			}

			ApplikationFigur figur = figurenMap.get(kernSystem.getInkarnationsName());
			if (figur == null) {
				continue;
			}

			figuren.add(0, figur);
			StoppBedingung stoppBedingung = inkarnation.getStoppBedingung();
			List<ApplikationFigur> nachFolgerFiguren = new ArrayList<>(otherApps);
			if (stoppBedingung != null) {
				String rechner = stoppBedingung.getRechner();
				if (rechner == null || rechner.trim().isEmpty()) {
					for (String nachfolger : stoppBedingung.getNachfolger()) {
						ApplikationFigur nachFolgerFigur = figurenMap.get(nachfolger);
						if (nachFolgerFigur != null) {
							nachFolgerFiguren.add(nachFolgerFigur);
						} else {
							System.err.println(
									"Sollte in einem gültigen Skript nicht funktionieren, gegebenenfalls Bedingungen vervollständigen");
						}
					}
				} else {
					for (String nachfolger : stoppBedingung.getNachfolger()) {
						ApplikationFigur vgf = figurenMap.get(rechner + ":" + nachfolger);
						if (vgf == null) {
							vgf = new StoppApplikationFigur(gc, nachfolger, rechner, null, null);
							figurenMap.put(rechner + ":" + nachfolger, vgf);
							figuren.add(0, vgf);
						}
						nachFolgerFiguren.add(vgf);
					}
				}
			}

			figur.setReferenzen(nachFolgerFiguren);
		}

		for (Inkarnation inkarnation : inkarnationen.values()) {

			ApplikationFigur figur = figurenMap.get(inkarnation.getInkarnationsName());
			if (figur == null) {
				continue;
			}

			figuren.add(0, figur);

			StoppBedingung stoppBedingung = inkarnation.getStoppBedingung();
			if (stoppBedingung != null) {
				String rechner = stoppBedingung.getRechner();
				if (rechner == null || rechner.trim().isEmpty()) {
					List<ApplikationFigur> referenzFiguren = new ArrayList<>();
					for (String nachfolger : stoppBedingung.getNachfolger()) {
						ApplikationFigur referenzFigur = figurenMap.get(nachfolger);
						if (referenzFigur != null) {
							referenzFiguren.add(referenzFigur);
						}
						figur.setReferenzen(referenzFiguren);
					}
				} else {
					List<ApplikationFigur> referenzFiguren = new ArrayList<>();
					for (String referenz : stoppBedingung.getNachfolger()) {
						ApplikationFigur vgf = figurenMap.get(rechner + ":" + referenz);
						if (vgf == null) {
							vgf = new StoppApplikationFigur(gc, referenz, rechner, null, null);
							figurenMap.put(rechner + ":" + referenz, vgf);
							figuren.add(0, vgf);
						}
						referenzFiguren.add(vgf);
					}

					figur.setReferenzen(referenzFiguren);
				}
			}
		}

		gc.dispose();
		positioniereFiguren();
	}

	private void positioniereFiguren() {

		int y = 0;
		graphHeight = 0;
		graphWidth = 0;
		for (ApplikationFigur figur : figuren) {
			figur.setPosition(figur.getReferenzOffset(), y);
			y += figur.getHeight() + 10;
			graphHeight = Math.max(graphHeight, figur.getY() + figur.getHeight());
			graphWidth = Math.max(graphWidth, figur.getX() + figur.getWidth());
		}
	}

	@Override
	public void paintControl(PaintEvent e) {

		int y = 0;
		for (ApplikationFigur figur : figuren) {
			figur.setPosition(origin.x + figur.getReferenzOffset(), origin.y + y);
			figur.paintControl(e);

			y += figur.getHeight() + 10;

			if (!figur.getReferenzen().isEmpty()) {
				ApplikationFigur vorgaenger = figur.getReferenzen().get(0);

				e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));

				e.gc.drawLine(vorgaenger.getX() + vorgaenger.getWidth() / 2, vorgaenger.getY() + vorgaenger.getHeight(),
						vorgaenger.getX() + vorgaenger.getWidth() / 2, figur.getY() + figur.getHeight() / 2);
				e.gc.drawLine(vorgaenger.getX() + vorgaenger.getWidth() / 2, figur.getY() + figur.getHeight() / 2,
						figur.getX(), figur.getY() + figur.getHeight() / 2);

				StoppBedingung stoppBedingung = figur.getStoppBedingung();
				if (stoppBedingung == null) {
					String vgString = "KS wartet";
					Point textExtent = e.gc.textExtent(vgString);
					e.gc.drawString(vgString, figur.getX() - textExtent.x - 3,
							figur.getY() + figur.getHeight() / 2 + 3);

				} else {

					String vgString = vorgaenger.getName();
					Point textExtent = e.gc.textExtent(vgString);
					e.gc.drawString(vgString, figur.getX() - textExtent.x - 3,
							figur.getY() + figur.getHeight() / 2 + 3);

					try {
						long warteZeitInMsec = Util
								.convertToWarteZeitInMsec(Util.nonEmptyString(stoppBedingung.getWartezeit(), "0"));
						if ((warteZeitInMsec / 1000) > 0) {
							String wsString = "WZ: " + (warteZeitInMsec / 1000);
							textExtent = e.gc.textExtent(wsString);
							e.gc.drawString(wsString, figur.getX() - textExtent.x - 3,
									figur.getY() + figur.getHeight() / 2 - textExtent.y - 3);
						}
					} catch (StartStoppException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

		for (ApplikationFigur figur : figuren) {
			figur.paintControl(e);
		}
		// TODO Auto-generated method stub

	}

}
