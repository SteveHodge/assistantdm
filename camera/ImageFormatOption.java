package camera;
import canon.cdsdk.ImageFormat;


public class ImageFormatOption extends ImageFormat implements Comparable<ImageFormat> {
	public static String sizeNames[] = {"Large","Medium","Small","","","Medium 1","Medium 2","Medium 3"};
	public static String qualityNames[] = {"","Economy","Normal","Fine","Raw","Superfine"};
	protected static int sizeOrder[] = {1,5,6,7,7,2,3,4};
	protected static int qualityOrder[] = {7,5,4,3,1,2};

	public ImageFormatOption(ImageFormat f) {
		size = f.size;
		quality = f.quality;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ImageFormat) {
			ImageFormat b = (ImageFormat)obj;
			return (quality == b.quality && size == b.size);
		}
		return super.equals(obj);
	}

	public int hashCode() {
		return (quality << 16) ^ size;
	}

	public String toString() {
		StringBuffer s = new StringBuffer(sizeNames[size]);
		s.append(" / ");
		s.append(qualityNames[quality]);
		return s.toString();
	}

//	public static ImageFormatOption[] getOptions(Source source) {
//		ImageFormat format;
//		Enum<ImageFormat> formats = source.getImageFormatEnum();
//		ImageFormatOption[] options = new ImageFormatOption[formats.getCount()]; 
//		for (int i=0; i<options.length; i++) {
//			format = formats.next();
//			options[i] = new ImageFormatOption(format);
//		}
//		formats.release();
//		Arrays.sort(options);
//		return options;
//	}

	public int compareTo(ImageFormat o) {
		if (sizeOrder[size] != sizeOrder[o.size]) return sizeOrder[size] - sizeOrder[o.size];
		return qualityOrder[quality] - qualityOrder[o.quality];
	}
}
