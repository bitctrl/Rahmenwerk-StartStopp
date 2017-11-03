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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

public class OnlineActionPanel extends Composite {

	private Applikation applikation;
	private StartStoppClient client;

	public OnlineActionPanel(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new GridLayout(2, false));

		createSystemAktionen();
		createApplikationAktionen();
	}

	private void createApplikationAktionen() {

		Group group = new Group(this, SWT.NONE);
		group.setText("Applikation");
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button button = new Button(group, SWT.PUSH);
		button.setText("Starten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				starteApplikation();
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Stoppen");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				stoppeApplikation();
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Neu starten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				restarteApplikation();
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Info");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (applikation != null && client != null) {
					ApplikationInfoDialog dialog = new ApplikationInfoDialog(getShell(), applikation);
					dialog.open();
				}
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Meldungen");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (applikation != null && client != null) {
					ApplikationMeldungenDialog dialog = new ApplikationMeldungenDialog(getShell(), client, applikation);
					dialog.open();
				}
			}
		});
	}

	private void createSystemAktionen() {

		Group group = new Group(this, SWT.NONE);
		group.setText("StartStopp");
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		Button button = new Button(group, SWT.PUSH);
		button.setText("Anhalten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				stopStartStopp();
			}

		});

		button = new Button(group, SWT.PUSH);
		button.setText("Starten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				startStartStopp();
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Neu starten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				restartStartStopp();
			}

		});

		button = new Button(group, SWT.PUSH);
		button.setText("Beenden");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				exitStartStopp();
			}
		});

		button = new Button(group, SWT.PUSH);
		button.setText("Betriebsmeldungen umschalten");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				betriebsMeldungenUmschalten();
			}
		});
	}

	public void setSelection(ISelection selection) {
		applikation = null;
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof Applikation) {
				applikation = (Applikation) element;
			}
		}
	}

	private String buildErrorMsg(StartStoppException e) {
		StringBuilder builder = new StringBuilder(e.getLocalizedMessage());
		for( String message : e.getMessages()) {
			builder.append('\n');
			builder.append(message);
		}
		return builder.toString();
	}
	
	private void starteApplikation() {
		if (applikation != null && client != null) {
			try {
				client.starteApplikation(applikation.getInkarnation().getInkarnationsName());
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void stoppeApplikation() {
		if (applikation != null && client != null) {
			try {
				if (MessageDialog.openConfirm(getShell(), "Applikation stoppen", "Soll die Applikation \""
						+ applikation.getInkarnation().getInkarnationsName() + "\" wirklich angehalten werden?")) {
					client.stoppeApplikation(applikation.getInkarnation().getInkarnationsName());
				}
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void restarteApplikation() {
		if (applikation != null && client != null) {
			try {
				if (MessageDialog.openConfirm(getShell(), "Applikation neu starten", "Soll die Applikation \""
						+ applikation.getInkarnation().getInkarnationsName() + "\" wirklich neu gestartet werden?")) {
					client.restarteApplikation(applikation.getInkarnation().getInkarnationsName());
				}
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void stopStartStopp() {
		if (client != null) {
			try {
				if (MessageDialog.openConfirm(getShell(), "StartStopp anhalten",
						"Soll die StartStopp-Konfiguration wirklich angehalten werden?")) {
					client.stoppStartStopp();
				}
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void startStartStopp() {
		if (client != null) {
			try {
				client.startStartStopp();
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void restartStartStopp() {
		if (client != null) {
			try {
				if (MessageDialog.openConfirm(getShell(), "StartStopp neu starten",
						"Soll die StartStopp-Konfiguration wirklich neu gestartet werden?")) {
					client.restartStartStopp();
				}
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void exitStartStopp() {
		if (client != null) {
			try {
				if (MessageDialog.openConfirm(getShell(), "StartStopp beenden",
						"Soll die StartStopp-SWE wirklich beendet werden?")) {
					client.exitStartStopp();
				}
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	private void betriebsMeldungenUmschalten() {
		if (client != null) {
			try {
				client.betriebsmeldungenUmschalten();
			} catch (StartStoppException e) {
				MessageDialog.openError(getShell(), "Fehler", buildErrorMsg(e));
			}
		}
	}

	void setClient(StartStoppClient client) {
		this.client = client;
	}
}
