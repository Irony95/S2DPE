package application;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;
import javafx.scene.paint.Color;

public class EngineProperties {	
	
	private static int TIME_STEP_COUNT = 100;
	public static int getStepCount() { return TIME_STEP_COUNT; }
	public static void setStepCount(int count) {TIME_STEP_COUNT = (count < 0) ? 0 : count; }
 	
	private static float PIXEL_PER_METER = 100f;
	public static float getPixelsPerMeter() {return PIXEL_PER_METER; }
	public static void setPixelsPerMeter(float _ppm) {PIXEL_PER_METER = (_ppm <=0) ? 1.0f : _ppm; }
	
	public static Color canvasColor = Color.rgb(0, 0, 0);
	
	//gravity is measured in m^2/s
	public static RealVector gravity = new ArrayRealVector(new double[] {0, 9.8});
	
	public static enum ODESolver {
		EULER,
		RK4
	}
	public static ODESolver engineSolver = ODESolver.RK4;
	
	public static float maxSpringConstant =  99999f;
	public static float maxDampingConstant = 99999f;
	
	public static float kToCoils = 2.5f;
	
	public static double airDensity = 1.293 / Math.pow(PIXEL_PER_METER, 2);
	
	public static boolean applyFriction = true;
	public static boolean showWireframe = false;
	public static boolean isPlaying = false;
}
