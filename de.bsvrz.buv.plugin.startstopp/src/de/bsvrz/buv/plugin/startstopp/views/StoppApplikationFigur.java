package de.bsvrz.buv.plugin.startstopp.views;

import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;

public class StoppApplikationFigur extends ApplikationFigur {

	public StoppApplikationFigur(String name) {
		super(false, name);
	}

	public StoppApplikationFigur(String name, String rechner, List<ApplikationFigur> vorgaenger,
			Inkarnation inkarnation) {
		super(false, name, rechner, vorgaenger, inkarnation);
	}
}
