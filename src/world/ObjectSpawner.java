package world;

import java.util.ArrayList;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineWorld;
import application.EngineProperties;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import physicsComponents.*;

public class ObjectSpawner {
	
	public static ArrayList<double[]> polygonPoints = new ArrayList<double[]>();	
	
	public static EntityUnit objectA = null;
	
	public static EntityUnit objectB = null;
	
	public static void setChildrenDisable(Pane pane, boolean disabled) {
		for (int i = 0;i < pane.getChildren().size();i++) {
			pane.getChildren().get(i).setDisable(disabled);
		}
	}
	
	public static void spawnCircle(Pane settings, int matIndex, double x, double y) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField radiusField = ((TextField)(settings.lookup("#tfRadius")));
    	TextField vxField = ((TextField)(settings.lookup("#tfVelocityX")));
    	TextField vyField = ((TextField)(settings.lookup("#tfVelocityY")));
    	ColorPicker colorPicker = ((ColorPicker)(settings.lookup("#cpColor")));
    	
		try {     		    
    		String name = nameField.getText();
    		if (name == "") { return; }
    		double radius = Double.parseDouble(radiusField.getText()) * EngineProperties.getPixelsPerMeter();
    		double vx = Double.parseDouble(vxField.getText()) * EngineProperties.getPixelsPerMeter();
    		double vy = Double.parseDouble(vyField.getText()) * EngineProperties.getPixelsPerMeter();
    		Color color = colorPicker.getValue();
    		Material mat = sceneObjects.materials.get(matIndex);
		CircleRigidBody c = new CircleRigidBody(Math.round(x), Math.round(y), vx, vy, mat, radius, color);
		
		sceneObjects.addEntity(name, c);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public static void spawnParticleEntity(Pane settings, int matIndex, double x, double y) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField vxField = ((TextField)(settings.lookup("#tfVelocityX")));
    	TextField vyField = ((TextField)(settings.lookup("#tfVelocityY")));
    	ColorPicker colorPicker = ((ColorPicker)(settings.lookup("#cpColor")));
    	try {     		    
    		String name = nameField.getText();
    		if (name == "") { return; }
    		double vx = Double.parseDouble(vxField.getText()) * EngineProperties.getPixelsPerMeter();
    		double vy = Double.parseDouble(vyField.getText()) * EngineProperties.getPixelsPerMeter();
    		Color color = colorPicker.getValue();
    		Material mat = sceneObjects.materials.get(matIndex);
		ParticleEntity c = new ParticleEntity(Math.round(x), Math.round(y),
				vx, vy, mat, color);
		sceneObjects.addEntity(name, c);
		} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public static void spawnPolygonRigidBody(Pane settings, int matIndex) {
		spawnPolygonRigidBody(settings, matIndex, polygonPoints.get(0)[0], polygonPoints.get(0)[1]);
	}
	
	public static void spawnPolygonRigidBody(Pane settings, int matIndex, double x, double y) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField rotationField = ((TextField)(settings.lookup("#tfRotation")));
    	TextField avField = ((TextField)(settings.lookup("#tfAngularVelocity")));
    	TextField vxField = ((TextField)(settings.lookup("#tfVelocityX")));
    	TextField vyField = ((TextField)(settings.lookup("#tfVelocityY")));
    	ColorPicker colorPicker = ((ColorPicker)(settings.lookup("#cpColor")));
    	
    	try {
			PolygonRigidBody.Builder builder = PolygonRigidBody.Builder.newInstance(); 
			for (int i = 1;i < polygonPoints.size();i++) {
				builder.addPoint(polygonPoints.get(i)[0] - polygonPoints.get(0)[0],
						polygonPoints.get(i)[1] - polygonPoints.get(0)[1]);
			}
			Color color = colorPicker.getValue();
			builder.setColor(color);
			builder.setRotations(Double.parseDouble(avField.getText()), Double.parseDouble(rotationField.getText()));
			builder.setVelocity(Double.parseDouble(vxField.getText()), Double.parseDouble(vyField.getText()));
			Material mat = sceneObjects.materials.get(matIndex);
			PolygonRigidBody object = builder.build(x, y, mat);
			sceneObjects.addEntity(nameField.getText(), object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static EventHandler<MouseEvent> placePolygonPoints(EngineWorld canvas) {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				polygonPoints.add(new double[] {canvas.mouse.getEntry(0), canvas.mouse.getEntry(1)});			
			}
		};
	}
	
	public static void spawnGravity(Pane settings) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField gxField = ((TextField)(settings.lookup("#tfGravityX")));
    	TextField gyField = ((TextField)(settings.lookup("#tfGravityY")));
    	String name = nameField.getText();
    	if (name == "") { return; }
    	try {
    		double gx = Double.parseDouble(gxField.getText())* EngineProperties.getPixelsPerMeter();
    		double gy = Double.parseDouble(gyField.getText())* EngineProperties.getPixelsPerMeter();
    		GlobalGravity gravity = new GlobalGravity(gx,
    				gy, sceneObjects.entities);
    		sceneObjects.addForce(name, gravity);    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	public enum ObjectSelector {
		A,
		B
	}
	public interface SelectorCallback { public void callback(); }
	
	public static EventHandler<MouseEvent> findAndSetObject(ObjectSelector object,
			ToggleButton button, SelectorCallback callback, Canvas canvas) {	
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				SceneObjects sceneObjects = SceneObjects.getInstance();
				RealVector mouseloc = new ArrayRealVector(new double[] {event.getX(), event.getY()});
				double closestDist = Double.POSITIVE_INFINITY;
				int objectIndex = -1;
				for (int i =0; i< sceneObjects.entities.size();i++) {
					double dist = sceneObjects.entities.get(i).position.getDistance(mouseloc);
					if (dist < closestDist) {
						closestDist = dist;
						objectIndex = i;
						if (object == ObjectSelector.A) {
							objectA = sceneObjects.entities.get(i);							
						}
						else {
							objectB = sceneObjects.entities.get(i);
						}						
					}
				}
				button.setSelected(false);
				
				String name = sceneObjects.getEntityName(objectIndex);
				button.setText(name);	
				canvas.setOnMouseClicked(null);
				
				if (callback != null) {					
					callback.callback();
				}
			}        					
		};
	}
	
	public static void spawnSpring(Pane settings) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField restField = ((TextField)(settings.lookup("#tfRestLength")));
    	TextField kField = ((TextField)(settings.lookup("#tfCoefficientK")));
    	TextField dampField = ((TextField)(settings.lookup("#tfDampening")));
    	ToggleButton selectObjectA = ((ToggleButton)(settings.lookup("#btnObjectA")));
    	ToggleButton selectObjectB = ((ToggleButton)(settings.lookup("#btnObjectB")));
    	if (objectA == null || objectB == null || objectA == objectB) { return; }
    	try {
    		String name = nameField.getText();
    		double rest = Double.parseDouble(restField.getText());
    		double k = Double.parseDouble(kField.getText());
    		double damp = Double.parseDouble(dampField.getText());
    		
    		Spring spring = new Spring(objectA, objectB, k, rest, damp);
    		sceneObjects.addForce(name, spring);
    		objectA = null;
    		selectObjectA.setText("Object A");
    		objectB = null;
    		selectObjectB.setText("Object B");
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public static void spawnJoint(Pane settings) {
		SceneObjects sceneObjects = SceneObjects.getInstance();
		
		TextField nameField = ((TextField)(settings.lookup("#tfName")));
    	TextField torqueField = ((TextField)(settings.lookup("#tfTorque")));
    	ToggleButton selectObjectA = ((ToggleButton)(settings.lookup("#btnObjectA")));
    	ToggleButton selectObjectB = ((ToggleButton)(settings.lookup("#btnObjectB")));
    	if (objectA == null || objectB == null || objectA == objectB) { return; }
    	try {
    		String name = nameField.getText();
    		double torque = Double.parseDouble(torqueField.getText());
    		Joint joint = new Joint((ParticleEntity)objectA, (PolygonRigidBody)objectB, torque);
    		sceneObjects.addForce(name, joint);
    		objectA = null;
    		selectObjectA.setText("Object A");
    		objectB = null;
    		selectObjectB.setText("Object B");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
}
