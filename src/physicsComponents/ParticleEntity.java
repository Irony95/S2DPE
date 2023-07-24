package physicsComponents;

import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineProperties;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ParticleEntity extends EntityUnit implements Drawable {
	private static final long serialVersionUID = 1056985444995341511L;
	
	public ParticleEntity(double x, double y, double vx, double vy, Material mat, Color c) {
		
		super(x, y, vx, vy, mat);
		if (mat.customMass >= 0) {
			this.mass = mat.customMass;
			invMass = mat.customMass == 0 ? 0 : 1/mat.customMass;
		}
		color = c;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void draw(GraphicsContext gc) {
		
		gc.setFill(color);
		gc.fillOval(position.getEntry(0) - 5, position.getEntry(1) - 5, 10, 10);
		
	}
	
	@Override
	public void update(double deltaTime) {
		
		super.update(deltaTime);
	}

	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		draw(gc);
	}
}
