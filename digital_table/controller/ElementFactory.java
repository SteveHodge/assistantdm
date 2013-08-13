package digital_table.controller;

import digital_table.elements.MapElement;

interface ElementFactory<P extends OptionsPanel<?>> {
	P addElement(MapElement parent);

	void removeElement(P panel);
}
