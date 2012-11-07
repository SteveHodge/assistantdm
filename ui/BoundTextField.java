package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import party.Creature;

// TODO this can be done with beans binding (JGoodies/JFace) - it's a more general solution. the alternative would be to have character supply models for properties/statistics

@SuppressWarnings("serial")
public class BoundTextField extends JTextField {
	protected Creature creature;
	protected String property;
	protected boolean modifying = false; 

	public BoundTextField(Creature c, String prop, int columns) {
		creature = c;
		property = prop;
		getDocument().addDocumentListener(new DocumentListener() {
			protected void update(DocumentEvent e,String op) {
				try {
					//System.out.println("Document "+op+" for "+BoundTextField.this.hashCode()+", setting character to "+e.getDocument().getText(0,e.getDocument().getLength()));
					modifying = true;
					creature.setProperty(property, e.getDocument().getText(0,e.getDocument().getLength()));
					modifying = false;
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}

			public void changedUpdate(DocumentEvent e) {
				update(e,"change");
			}

			public void insertUpdate(DocumentEvent e) {
				update(e,"insert");
			}

			public void removeUpdate(DocumentEvent e) {
				update(e,"remove");
			}
			
		});

		creature.addPropertyChangeListener(property, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				//System.out.println("Notified "+BoundTextField.this.hashCode()+", setting value to "+creature.getProperty(property));
				// we might get notified from the result of our own update (e.g. if there is another BoundTextField bound to the same property)
				// if we try to set the text in this case it will cause an exception, so we set a flag
				// TODO this is not threadsafe
				if (!modifying) {
					setText(creature.getProperty(property).toString());
				}
			}
		});
		setColumns(columns);
		if (creature.getProperty(property) != null) {
			setText(creature.getProperty(property).toString());
		}
	}
}
