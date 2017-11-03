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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.bsvrz.buv.plugin.startstopp.Activator;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartArt;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppFehlerVerhalten;
import de.bsvrz.sys.startstopp.api.util.Util;

public class ApplikationInfoDialog extends TitleAreaDialog {

	private static class AppStatusPanel extends Composite {

		AppStatusPanel(Composite parent, Applikation applikation) {
			super(parent, SWT.NONE);

			setLayout(new GridLayout());

			Group group = new Group(this, SWT.NONE);
			group.setText("Status");
			group.setLayout(new GridLayout(2, false));
			GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

			new Label(group, SWT.NONE).setText("Status:");
			Label statusLabel = new Label(group, SWT.NONE);
			statusLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(statusLabel);
			statusLabel.setText(Util.nonEmptyString(applikation.getStatus().name()));

			new Label(group, SWT.NONE).setText("Startzeit:");
			Label startZeitLabel = new Label(group, SWT.NONE);
			startZeitLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			startZeitLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(startZeitLabel);
			startZeitLabel.setText(Util.nonEmptyString(applikation.getLetzteStartzeit()));

			new Label(group, SWT.NONE).setText("Initialisierung:");
			Label initialisierungZeitLabel = new Label(group, SWT.NONE);
			initialisierungZeitLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			initialisierungZeitLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(initialisierungZeitLabel);
			initialisierungZeitLabel.setText(Util.nonEmptyString(applikation.getLetzteInitialisierung()));

			new Label(group, SWT.NONE).setText("StoppZeit:");
			Label stoppZeitLabel = new Label(group, SWT.NONE);
			stoppZeitLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			stoppZeitLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(stoppZeitLabel);
			stoppZeitLabel.setText(Util.nonEmptyString(applikation.getLetzteStoppzeit()));

			Label messageLabel = new Label(group, SWT.NONE);
			messageLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			messageLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(messageLabel);
			messageLabel.setText(Util.nonEmptyString(applikation.getStartMeldung()));
		}
	}

	private static class InkarnationPanel extends Composite {

		InkarnationPanel(Composite parent, Applikation initialApplikation) {
			super(parent, SWT.NONE);

			Inkarnation inkarnation = initialApplikation.getInkarnation();

			setLayout(new GridLayout());

			Group group = new Group(this, SWT.NONE);
			group.setText("Inkarnation");
			group.setLayout(new GridLayout(2, false));
			GridDataFactory.fillDefaults().grab(true, true).applyTo(group);

			new Label(group, SWT.NONE).setText("Applikation:");
			Label label = new Label(group, SWT.NONE);
			label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(label);
			label.setText(Util.nonEmptyString(inkarnation.getApplikation()));

			new Label(group, SWT.NONE).setText("Parameter:");
			ListViewer listViewer = new ListViewer(group, SWT.V_SCROLL | SWT.H_SCROLL);
			listViewer.getList().setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			listViewer.getList().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, true).hint(250, 50).applyTo(listViewer.getControl());
			listViewer.setContentProvider(new ArrayContentProvider());
			listViewer.setInput(inkarnation.getAufrufParameter());

			new Label(group, SWT.NONE).setText("Inkarnationstyp:");
			label = new Label(group, SWT.NONE);
			label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(label);
			label.setText(Util.nonEmptyString(inkarnation.getInkarnationsTyp().name()));

			new Label(group, SWT.NONE).setText("Startart:");
			label = new Label(group, SWT.NONE);
			label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(label);
			StartArt startArt = inkarnation.getStartArt();
			String startArtName = startArt.getOption().name();
			if (startArt.getNeuStart()) {
				startArtName = startArtName + " mit Neustart";
			}
			label.setText(startArtName);

			switch (startArt.getOption()) {

			case INTERVALLABSOLUT:
			case INTERVALLRELATIV:
				new Label(group, SWT.NONE).setText("Intervall:");
				label = new Label(group, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).hint(250, 0).applyTo(label);
				label.setText(Util.nonEmptyString(startArt.getIntervall()));
				break;
			case AUTOMATISCH:
			case MANUELL:
			default:
				break;
			}

			Button button = new Button(group, SWT.CHECK);
			button.setText("Mit Inkarnationsname");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					((Button) e.getSource()).setSelection(inkarnation.getMitInkarnationsName());
				}
			});
			button.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			button.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().applyTo(button);
			button.setSelection(inkarnation.getMitInkarnationsName());

			button = new Button(group, SWT.CHECK);
			button.setText("Initialisieren");
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					((Button) e.getSource()).setSelection(inkarnation.getInitialize());
				}
			});
			button.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			button.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			GridDataFactory.fillDefaults().applyTo(button);
			button.setSelection(inkarnation.getInitialize());

			StartBedingung startBedingung = inkarnation.getStartBedingung();
			if (startBedingung != null) {
				Group bedingungsGroup = new Group(group, SWT.NONE);
				bedingungsGroup.setText("Startbedingung");
				GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(bedingungsGroup);
				bedingungsGroup.setLayout(new GridLayout(4, false));

				new Label(bedingungsGroup, SWT.NONE).setText("Vorgänger:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(label);
				String vorgaengerStr = String.join(",", startBedingung.getVorgaenger());
				if (startBedingung.getRechner() != null && !startBedingung.getRechner().trim().isEmpty()) {
					vorgaengerStr = startBedingung.getRechner() + ": " + vorgaengerStr;
				}
				label.setText(vorgaengerStr);

				new Label(bedingungsGroup, SWT.NONE).setText("Warteart:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(startBedingung.getWarteart().name());

				new Label(bedingungsGroup, SWT.NONE).setText("Wartzeit:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(Util.nonEmptyString(startBedingung.getWartezeit()));
			}

			StartFehlerVerhalten startFehlerverhalten = inkarnation.getStartFehlerVerhalten();
			if (startFehlerverhalten != null) {
				Group bedingungsGroup = new Group(group, SWT.NONE);
				bedingungsGroup.setText("Startfehlerverhalten");
				GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(bedingungsGroup);
				bedingungsGroup.setLayout(new GridLayout(4, false));

				new Label(bedingungsGroup, SWT.NONE).setText("Option:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(startFehlerverhalten.getOption().name());

				new Label(bedingungsGroup, SWT.NONE).setText("Wiederholungen:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(Util.nonEmptyString(startFehlerverhalten.getWiederholungen()));
			}

			StoppBedingung stoppBedingung = inkarnation.getStoppBedingung();
			if (stoppBedingung != null) {
				Group bedingungsGroup = new Group(group, SWT.NONE);
				bedingungsGroup.setText("Stoppbedingung");
				GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(bedingungsGroup);
				bedingungsGroup.setLayout(new GridLayout(4, false));

				new Label(bedingungsGroup, SWT.NONE).setText("Nachfolger:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(label);
				String referenzStr = String.join(",", stoppBedingung.getNachfolger());
				if (stoppBedingung.getRechner() != null && !stoppBedingung.getRechner().trim().isEmpty()) {
					referenzStr = stoppBedingung.getRechner() + ": " + referenzStr;
				}
				label.setText(referenzStr);

				new Label(bedingungsGroup, SWT.NONE).setText("Wartezeit:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(label);
				label.setText(Util.nonEmptyString(stoppBedingung.getWartezeit()));
			}

			StoppFehlerVerhalten stoppFehlerverhalten = inkarnation.getStoppFehlerVerhalten();
			if (stoppFehlerverhalten != null) {
				Group bedingungsGroup = new Group(group, SWT.NONE);
				bedingungsGroup.setText("Stoppfehlerverhalten");
				GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(bedingungsGroup);
				bedingungsGroup.setLayout(new GridLayout(4, false));

				new Label(bedingungsGroup, SWT.NONE).setText("Option:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(stoppFehlerverhalten.getOption().name());

				new Label(bedingungsGroup, SWT.NONE).setText("Wiederholungen:");
				label = new Label(bedingungsGroup, SWT.NONE);
				label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
				label.setText(Util.nonEmptyString(stoppFehlerverhalten.getWiederholungen()));
			}

		}
	}

	private Applikation applikation;

	public ApplikationInfoDialog(Shell shell, Applikation applikation) {
		super(shell);
		this.applikation = applikation;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		String imageFilePath = "icons/agent_prop_wiz.gif";

		Image image = imageRegistry.get(imageFilePath);
		if (image == null) {
			imageRegistry.put(imageFilePath, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imageFilePath));
			image = imageRegistry.get(imageFilePath);
		}
		setTitleImage(image);

		setTitle("Applikation: " + applikation.getInkarnation().getInkarnationsName());
		setMessage("Startparameter und weitere Informationen zum Starten und Beenden der Applikation.");

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		AppStatusPanel appStatusPanel = new AppStatusPanel(panel, applikation);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(appStatusPanel);

		InkarnationPanel inkarnationPanel = new InkarnationPanel(panel, applikation);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(inkarnationPanel);

		return super.createDialogArea(parent);
	}
}
