package gamesystem;

import gamesystem.core.PropertyCollection;
import gamesystem.core.PropertyEvent;

/*
 * It is debatable as to whether modifiers to attack should apply to grapple. Skip Williams wrote an article on grapple for wizards.com that implied that they do and
 * also answered a question (in his capacity as "the Sage") that included applying the -4 penalty for being prone to the grapple check.
 * On the other hand the wording of the Rules Compendium suggests that only grapple specific modifiers apply (the only examples given are racial modifiers and the modifier
 * from the Improved Grapple feat). Excluding attack modifiers make grapple less prone to abuse (e.g. use of true strike) so that it the interpretation implemented here.
 * This class could be moved in Attacks to implement the other interpretation.
 */

public class GrappleModifier extends Statistic {
	BAB bab;

	public GrappleModifier(PropertyCollection parent, BAB bab, Size size, AbilityScore str) {
		super("grapple", "Grapple", parent);

		if (bab != null) {
			this.bab = bab;
			bab.addPropertyListener(e -> {
				fireEvent(createEvent(PropertyEvent.VALUE_CHANGED));
			});
		}

		if (size != null) {
			addModifier(size.getGrappleSizeModifier());
		}

		if (str != null) {
			addModifier(str.getModifier());
		}

	}

	@Override
	public Integer getValue() {
		int grapple = bab.getValue() + super.getValue();
		return grapple;
	}

	@Override
	public String getSummary() {
		StringBuilder text = new StringBuilder();
		text.append(bab.getValue()).append(" BAB<br/>");
		text.append(super.getSummary());
		return text.toString();
	}
}
