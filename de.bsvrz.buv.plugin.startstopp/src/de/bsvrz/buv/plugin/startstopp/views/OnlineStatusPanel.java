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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;

public class OnlineStatusPanel extends Composite {

	private Label statusLabel;
	private Label bmLabel;
	private Label abfrageLabel;

	public OnlineStatusPanel(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new FillLayout());
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Status");
		group.setLayout(new GridLayout());
		
		abfrageLabel = new Label(group, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(abfrageLabel);

		statusLabel = new Label(group, SWT.NONE);
		statusLabel.setText("NICHT VERBUNDEN");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusLabel);
		
		bmLabel = new Label(group, SWT.NONE);
		bmLabel.setText("BM: ???");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(bmLabel);
	}

	public void refresh(StartStoppClient client) {

		try {
			StartStoppStatus startStoppStatus = client.getStartStoppStatus();
			updateStatus(startStoppStatus);
		} catch (StartStoppException e) {
			updateStatus(null);
		}
	}

	private void updateStatus(StartStoppStatus startStoppStatus) {
		new UIJob("StatusUpdate") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if( !abfrageLabel.isDisposed()) {
					abfrageLabel.setText(DateFormat.getDateTimeInstance().format(new Date()));
				}
				
				if (!statusLabel.isDisposed()) {
					if (startStoppStatus == null) {
						statusLabel.setText("NICHT VERBUNDEN");
					} else {
						statusLabel.setText(startStoppStatus.getStatus().name());
					}
				}
				if (!bmLabel.isDisposed()) {
					if (startStoppStatus == null) {
						bmLabel.setText("");
					} else {
						if (startStoppStatus.getBetriebsmeldungen()) {
							bmLabel.setText("BM: EIN");
						} else {
							bmLabel.setText("BM: AUS");
						}
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
