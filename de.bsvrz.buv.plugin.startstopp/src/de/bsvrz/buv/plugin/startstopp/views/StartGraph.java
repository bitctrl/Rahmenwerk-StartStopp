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
import java.util.Iterator;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.KernSystem;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class StartGraph extends Composite implements PaintListener {

	private StartStoppSkript skript;
	private List<StartApplikationFigur> figuren = new ArrayList<>();
	private StartStoppClient client;

	public StartGraph(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(null);
		addPaintListener(this);

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

		figuren.clear();

		Map<String, StartApplikationFigur> figurenMap = new LinkedHashMap<>();

		List<KernSystem> kernSysteme = skript.getGlobal().getKernsysteme();
		Map<String, Inkarnation> inkarnationen = new LinkedHashMap<>();
		for (Inkarnation inkarnation : skript.getInkarnationen()) {
			inkarnationen.put(inkarnation.getInkarnationsName(), inkarnation);
		}

		List<ApplikationFigur> kernSystemFiguren = new ArrayList<>();
		for (KernSystem kernSystem : kernSysteme) {
			Inkarnation inkarnation = inkarnationen.remove(kernSystem.getInkarnationsName());
			if (inkarnation == null) {
				continue;
			}

			StartBedingung startBedingung = inkarnation.getStartBedingung();
			if (startBedingung == null) {
				StartApplikationFigur figur = new StartApplikationFigur(inkarnation.getInkarnationsName());
				figur.setKernSystem(true);
				figurenMap.put(inkarnation.getInkarnationsName(), figur);
				figuren.add(figur);
				kernSystemFiguren.add(figur);
			} else {
				String rechner = startBedingung.getRechner();
				if (rechner == null || rechner.trim().isEmpty()) {
					List<ApplikationFigur> vorgaengerFiguren = new ArrayList<>();
					for (String vorgaenger : startBedingung.getVorgaenger()) {
						StartApplikationFigur vorgaengerFigur = figurenMap.get(vorgaenger);
						if (vorgaengerFigur != null) {
							vorgaengerFiguren.add(vorgaengerFigur);
						} else {
							System.err.println(
									"Sollte in einem gültigen Skript nicht funktionieren, gegebenenfalls Bedingungen vervollständigen");
						}
					}
					StartApplikationFigur figur = new StartApplikationFigur(inkarnation.getInkarnationsName(), null,
							vorgaengerFiguren, inkarnation);
					figur.setKernSystem(true);
					figurenMap.put(inkarnation.getInkarnationsName(), figur);
					figuren.add(figur);
					kernSystemFiguren.add(figur);
				} else {
					List<ApplikationFigur> vorgaengerFiguren = new ArrayList<>();
					for (String vorgaenger : startBedingung.getVorgaenger()) {
						StartApplikationFigur vgf = figurenMap.get(rechner + ":" + vorgaenger);
						if (vgf == null) {
							vgf = new StartApplikationFigur(vorgaenger, rechner, null, null);
							figurenMap.put(rechner + ":" + vorgaenger, vgf);
							figuren.add(vgf);
						}
						vorgaengerFiguren.add(vgf);
					}
					StartApplikationFigur figur = new StartApplikationFigur(inkarnation.getInkarnationsName(), null,
							vorgaengerFiguren, inkarnation);
					figurenMap.put(inkarnation.getInkarnationsName(), figur);
					figuren.add(figur);
					kernSystemFiguren.add(figur);
				}
			}
		}

		while (!inkarnationen.isEmpty()) {
			Iterator<Inkarnation> iterator = inkarnationen.values().iterator();
			while (iterator.hasNext()) {
				Inkarnation inkarnation = iterator.next();

				StartBedingung startBedingung = inkarnation.getStartBedingung();
				if (startBedingung == null) {
					StartApplikationFigur figur = new StartApplikationFigur(inkarnation.getInkarnationsName(), null,
							kernSystemFiguren, null);
					figurenMap.put(inkarnation.getInkarnationsName(), figur);
					figuren.add(figur);
					iterator.remove();
				} else {
					String rechner = startBedingung.getRechner();
					if (rechner == null || rechner.trim().isEmpty()) {
						List<ApplikationFigur> vorgaengerFiguren = new ArrayList<>(kernSystemFiguren);
						boolean vorgaenderVollstaendig = true;
						for (String vorgaenger : startBedingung.getVorgaenger()) {
							StartApplikationFigur vorgaengerFigur = figurenMap.get(vorgaenger);
							if (vorgaengerFigur != null) {
								vorgaengerFiguren.add(vorgaengerFigur);
							} else {
								vorgaenderVollstaendig = false;
							}
							if (vorgaenderVollstaendig) {
								StartApplikationFigur figur = new StartApplikationFigur(
										inkarnation.getInkarnationsName(), null, vorgaengerFiguren, inkarnation);
								figurenMap.put(inkarnation.getInkarnationsName(), figur);
								figuren.add(figur);
								iterator.remove();
							}
						}
					} else {
						List<ApplikationFigur> vorgaengerFiguren = new ArrayList<>(kernSystemFiguren);
						for (String vorgaenger : startBedingung.getVorgaenger()) {
							StartApplikationFigur vgf = figurenMap.get(rechner + ":" + vorgaenger);
							if (vgf == null) {
								vgf = new StartApplikationFigur(vorgaenger, rechner, null, null);
								figurenMap.put(rechner + ":" + vorgaenger, vgf);
								figuren.add(vgf);
							}
							vorgaengerFiguren.add(vgf);
						}
						StartApplikationFigur figur = new StartApplikationFigur(inkarnation.getInkarnationsName(), null,
								vorgaengerFiguren, inkarnation);
						figurenMap.put(inkarnation.getInkarnationsName(), figur);
						figuren.add(figur);
						iterator.remove();
					}
				}
			}
		}
	}

	@Override
	public void paintControl(PaintEvent e) {

		int y = 0;
		for (StartApplikationFigur figur : figuren) {
			figur.setPosition(figur.getXOffset(), y);
			figur.paintControl(e);

			y += figur.getHeight() + 10;

			if (!figur.getReferenzen().isEmpty()) {
				ApplikationFigur vorgaenger = figur.getReferenzen().get(figur.getReferenzen().size() - 1);

				e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));

				e.gc.drawLine(vorgaenger.getX() + vorgaenger.getWidth() / 2, vorgaenger.getY() + vorgaenger.getHeight(),
						vorgaenger.getX() + vorgaenger.getWidth() / 2, figur.getY() + figur.getHeight() / 2);
				e.gc.drawLine(vorgaenger.getX() + vorgaenger.getWidth() / 2, figur.getY() + figur.getHeight() / 2,
						figur.getXOffset(), figur.getY() + figur.getHeight() / 2);

				StartBedingung startBedingung = figur.getStartBedingung();
				if (startBedingung == null) {
					String vgString = "Kernsystem";
					Point textExtent = e.gc.textExtent(vgString);
					e.gc.drawString(vgString, figur.getXOffset() - textExtent.x - 3,
							figur.getY() + figur.getHeight() / 2 + 3);

				} else {

					String vgString = vorgaenger.getName();
					Point textExtent = e.gc.textExtent(vgString);
					e.gc.drawString(vgString, figur.getXOffset() - textExtent.x - 3,
							figur.getY() + figur.getHeight() / 2 + 3);

					try {
						long warteZeitInMsec = Util
								.convertToWarteZeitInMsec(Util.nonEmptyString(startBedingung.getWartezeit(), "0"));
						if ((warteZeitInMsec / 1000) > 0) {
							String wsString = startBedingung.getWarteart().name() + ": " + (warteZeitInMsec / 1000)
									+ " s";
							textExtent = e.gc.textExtent(wsString);
							e.gc.drawString(wsString, figur.getXOffset() - textExtent.x - 3,
									figur.getY() + figur.getHeight() / 2 - textExtent.y - 3);
						}
					} catch (StartStoppException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

		for (StartApplikationFigur figur : figuren) {
			figur.paintControl(e);
		}
		// TODO Auto-generated method stub

	}
}
