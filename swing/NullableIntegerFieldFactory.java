package swing;

import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class NullableIntegerFieldFactory {
	private static DecimalFormat df = new DecimalFormat("#####");
	private static NumberFormatter nf;

	// Factory for JFormattedTextFields that also accept nulls. Note currently sets the formatter minimum to 0.
	@SuppressWarnings("serial")
	public static JFormattedTextField createNullableIntegerField() {
		if (nf == null) {
			nf = new NumberFormatter(df) {
				@Override
				public String valueToString(Object iv) throws ParseException {
					if (iv == null || iv.toString().equals("")) {
						return "";
					}
					else {
						return super.valueToString(iv);
					}
				}

				@Override
				public Object stringToValue(String text) throws ParseException {
					if ("".equals(text)) {
						return null;
					}
					return super.stringToValue(text);
				}
			};
			nf.setMinimum(0);
			nf.setValueClass(Integer.class);
		}

		return new JFormattedTextField(nf);
	}
}
