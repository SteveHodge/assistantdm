package digital_table.elements;

import java.awt.image.BufferedImage;

// TODO remove - should be internal to animated ImageManager subclasses, if it's needed at all

public class AnimationFrame {
	private int delay;	// delay after this frame become visible before showing the next frame in ms
	private BufferedImage image;

	public AnimationFrame(BufferedImage image, int delay) {
		this.image = image;
		this.delay = delay;
	}

	public BufferedImage getImage() {
		return image;
	}

	public int getDelay() {
		return delay;
	}
}
