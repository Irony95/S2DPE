package physicsComponents;

import javafx.scene.canvas.GraphicsContext;

public interface Drawable {
	public void draw(GraphicsContext gc);
	public void drawWireframe(GraphicsContext gc, int index);
}
