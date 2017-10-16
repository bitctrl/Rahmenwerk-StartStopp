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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.util.Util;

public class ApplikationsListe extends Composite {

	public static class ApplikationTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Applikation) {

				Applikation applikation = (Applikation) element;

				switch (columnIndex) {
				case 0:
					return applikation.getInkarnation().getInkarnationsName();
				case 1:
					return applikation.getInkarnation().getStartArt().getOption().toString();
				case 2:
					return applikation.getStatus().name();
				case 3:
					return applikation.getStartMeldung();
				default:
					break;
				}
			}
			return getText(element);
		}

	}

	private ScheduledExecutorService updater;
	private boolean disposed;
	private StartStoppClient client;
	private TableViewer applikationViewer;
	private Label messageLabel;
	private OnlineStatusPanel statusPanel;
	private OnlineActionPanel actionPanel;

	public ApplikationsListe(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new GridLayout());

		Composite headerPanel = new Composite(this, SWT.NONE);
		headerPanel.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(headerPanel);
		
		actionPanel = new OnlineActionPanel(headerPanel);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(actionPanel);

		statusPanel = new OnlineStatusPanel(headerPanel);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.CENTER).applyTo(statusPanel);

		Composite tablePanel = new Composite(this, SWT.NONE);
		tablePanel.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tablePanel);

		applikationViewer = new TableViewer(tablePanel, SWT.FULL_SELECTION | SWT.BORDER);
		applikationViewer.getTable().setHeaderVisible(true);

		TableLayout tableLayout = new TableLayout();
		TableColumn column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Name");
		tableLayout.addColumnData(new ColumnWeightData(2));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Typ");
		tableLayout.addColumnData(new ColumnWeightData(1));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Status");
		tableLayout.addColumnData(new ColumnWeightData(1));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Meldung");
		tableLayout.addColumnData(new ColumnWeightData(4));

		applikationViewer.getTable().setLayout(tableLayout);

		applikationViewer.setContentProvider(new ArrayContentProvider());
		applikationViewer.setLabelProvider(new ApplikationTableLabelProvider());
// TODO enable with OXYGEN		applikationViewer.getTable().setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		applikationViewer.addSelectionChangedListener((event) -> actionPanel.setSelection(event.getSelection()));

		messageLabel = new Label(this, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageLabel);

		addDisposeListener((event) -> dispose(event));
		updater = Executors.newSingleThreadScheduledExecutor();
		updater.scheduleAtFixedRate(() -> refresh(), 1, 3, TimeUnit.SECONDS);
	}

	public void setClient(StartStoppClient client) {
		this.client = client;
		actionPanel.setClient(client);
	}

	private void dispose(DisposeEvent event) {
		disposed = true;
		updater.shutdown();
	}

	private void refresh() {
		if (disposed || client == null) {
			return;
		}

		try {
			List<Applikation> applikationen = client.getApplikationen();
			updater.schedule(() -> aktualisiereAnsicht(applikationen), 0, TimeUnit.MICROSECONDS);
		} catch (StartStoppException e) {
			updater.schedule(() -> aktualisiereAnsicht(Collections.emptyList(), e.getLocalizedMessage()), 0,
					TimeUnit.MICROSECONDS);
		}

		updater.schedule(() -> statusPanel.refresh(client), 0, TimeUnit.MICROSECONDS);
	}

	private void aktualisiereAnsicht(List<Applikation> applikationen) {
		aktualisiereAnsicht(applikationen, "");
	}

	private void aktualisiereAnsicht(List<Applikation> applikationen, String message) {
		new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!applikationViewer.getTable().isDisposed()) {
					IStructuredSelection selection = (IStructuredSelection) applikationViewer.getSelection();
					Object element = selection.getFirstElement();
					applikationViewer.setInput(applikationen.toArray());

					if( element instanceof Applikation) {
						for( Applikation applikation : applikationen) {
							if( applikation.getInkarnation().getInkarnationsName().equals(((Applikation) element).getInkarnation().getInkarnationsName())) {
								applikationViewer.setSelection(new StructuredSelection(applikation));
							}
						}
					}
				}
				if (!messageLabel.isDisposed()) {
					messageLabel.setText(Util.nonEmptyString(message));
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	StartStoppClient getClient() {
		return client;
	}

}
