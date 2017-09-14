package de.bsvrz.buv.plugin.startstopp.views;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import de.bsvrz.sys.startstopp.api.StartStoppClient;

public class StartStoppView {

	@Inject
	private MPart part;

	private String hostName = "localhost";
	private int port = 3000;

	private ApplikationsListe appListe;
	
	@PostConstruct
	public void createUI(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout(1, false));

		Composite settingsPanel = createSettingsPanel(panel);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(settingsPanel);
		
		CTabFolder folder = new CTabFolder(panel, SWT.BORDER | SWT.BOTTOM);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(folder);
		
		appListe = new ApplikationsListe(folder);
		CTabItem tab = new CTabItem(folder, 0);
		tab.setText("Runtime");
		tab.setControl(appListe);

		Composite scrolledForm = new Composite(folder, SWT.NONE);
		tab = new CTabItem(folder, 0);
		tab.setText("Startbedingungen");
		tab.setControl(scrolledForm);
 
		Canvas canvas = new Canvas(folder, SWT.NONE);
		canvas.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				System.err.println("Paint: " + e);
				e.gc.drawRectangle(10, 10, 100, 100);
				e.gc.drawString("Datenverteiler", 20, 40);
			}
		});
		tab = new CTabItem(folder, 0);
		tab.setText("Stoppbedingungen");
		tab.setControl(canvas);
		folder.setSelection(0);
		
		updateClient();
	}
	




	private Composite createSettingsPanel(Composite parent) {
		
		Composite panel = new Composite(parent, SWT.BORDER);
		panel.setLayout(new GridLayout(4, false));

		new Label(panel, SWT.NONE).setText("Host:");
		
		Text hostNameText = new Text(panel, SWT.BORDER);
		hostNameText.setText(hostName);
		hostNameText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String newHostName = hostNameText.getText();
				if( !newHostName.equals(hostName)) {
					hostName = newHostName;
					updateClient();
				}
			}
		});
		
		new Label(panel, SWT.NONE).setText("Port:");

		Spinner portSpinner = new Spinner(panel, SWT.BORDER);
		portSpinner.setMinimum(0);
		portSpinner.setMaximum(Short.MAX_VALUE);
		portSpinner.setSelection(port);
		portSpinner.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				port = portSpinner.getSelection();
				updateClient();
			}
		});
		portSpinner.addModifyListener((event)->port = portSpinner.getSelection());

		return panel;
	}

	@Inject
	public void initView() {
		final Map<String, String> state = part.getPersistedState();
		hostName = state.getOrDefault("host", hostName);
		port = Integer.parseInt(state.getOrDefault("port", Integer.toString(port)));
	}

	@PersistState
	public void saveState() {
		final Map<String, String> state = part.getPersistedState();
		state.put("host", hostName);
		state.put("port", Integer.toString(port));
	}

	private void updateClient() {
		StartStoppClient client = new StartStoppClient(hostName, port);
		appListe.setClient(client);
	}
}
