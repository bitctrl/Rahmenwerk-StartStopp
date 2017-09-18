package de.bsvrz.buv.plugin.startstopp.views;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;

public class ApplikationInfoDialog extends TitleAreaDialog {

	public ApplikationInfoDialog(Shell shell, StartStoppClient client, Applikation applikation) {
		super(shell);
	}
}
