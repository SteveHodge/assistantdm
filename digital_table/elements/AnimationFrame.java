package digital_table.elements;

import java.awt.image.BufferedImage;

public class AnimationFrame {
	private final int delay;	// delay after this frame become visible before showing the next frame in ms
	private final BufferedImage image;

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
