package ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import party.Character;

import ui.PartyPanel.SummaryDisplay;

@SuppressWarnings("serial")
public class CharacterSubPanel extends JPanel {
	protected List<SummaryDisplay> summaries = new ArrayList<SummaryDisplay>();
	protected Character character;
	protected String summary;

	public CharacterSubPanel(Character c) {
		character = c;
	}

	public void addSummaryDisplay(SummaryDisplay summaryDisplay) {
		summaries.add(summaryDisplay);
		summaryDisplay.updateSummary(summary);
	}

	public void removeSummaryDisplay(SummaryDisplay s) {
		summaries.remove(s);
	}

	protected void  updateSummaries() {
		for (SummaryDisplay d : summaries) {
			d.updateSummary(summary);
		}
	}

	protected void  updateSummaries(String s) {
		summary = s;
		updateSummaries();
	}
}
