package application;

import java.util.ArrayList;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import physicsComponents.Drawable;
import world.ObjectSpawner;
import world.SceneObjects;

public class EngineWorld extends Application {
	SceneObjects sceneObjects = SceneObjects.getInstance();
	
	EngineMasterController masterController;
	Canvas canvas;
	GraphicsContext gc;
	public RealVector mouse = new ArrayRealVector(new double[] {0, 0});
	
	
	public class DrawUtils {
		public static void drawArrow(GraphicsContext gct, RealVector position, RealVector direction, double len) {
			RealVector start = position.add(direction.mapMultiply(10));
			RealVector end = direction.mapMultiply(len).add(start);

			gct.setStroke(Color.LIGHTBLUE);
			gct.setLineWidth(2);
			gct.strokeLine(start.getEntry(0), start.getEntry(1), end.getEntry(0), end.getEntry(1));
			
			RealVector tangent = new ArrayRealVector(new double[] {-direction.getEntry(1), direction.getEntry(0)});
			RealVector pointOne = tangent.mapMultiply(5).add(direction.mapMultiply(len-5).add(start));
			gct.strokeLine(pointOne.getEntry(0), pointOne.getEntry(1), end.getEntry(0), end.getEntry(1));
			RealVector pointTwo = tangent.mapMultiply(-5).add(direction.mapMultiply(len-5).add(start));
			gct.strokeLine(pointTwo.getEntry(0), pointTwo.getEntry(1), end.getEntry(0), end.getEntry(1));
		}
		
		public static void writeVelocity(GraphicsContext gct, RealVector position, RealVector velocity) {
			String text = String.format("%.2f", velocity.getNorm()/EngineProperties.getPixelsPerMeter()) + "m/s";
			RealVector speedLocation = position.add(velocity.unitVector().mapMultiply(50));
//			speedLocation.setEntry(0, Math.floor(speedLocation.getEntry(0)/20)* 20);
//			speedLocation.setEntry(1, Math.floor(speedLocation.getEntry(1)/20)* 20);
			gct.setFill(Color.LIGHTBLUE);
			gct.fillText(text, speedLocation.getEntry(0) - 30, speedLocation.getEntry(1) + 10);
		}
	}

	public void setMasterController(EngineMasterController controller) {
		masterController = controller;		
		
	}
	
	public void drawFrame(float fps) {
		//set background color
		gc.setFill(EngineProperties.canvasColor);
		gc.fillRect(0, 0, canvas.widthProperty().get(), canvas.heightProperty().get());
		
		gc.setFill(Color.WHITE);
		gc.fillText(String.valueOf(Math.round(fps)), 10, 10);
		
		if (EngineProperties.isPlaying) {
			gc.setFill(Color.GREEN);	
			gc.fillPolygon(
					new double[] {30, 30, 35},
					new double[] {2,  12, 7}, 3);
		}
		else {
			gc.setStroke(Color.RED);
			gc.setLineWidth(2);
			gc.strokeLine(30, 2, 30, 12);
			gc.strokeLine(35, 2, 35, 12);
		}
		
		
		for (int obj = 0;obj < sceneObjects.entities.size();obj++) {
			if (sceneObjects.entities.get(obj) instanceof Drawable) {
				Drawable object = (Drawable) sceneObjects.entities.get(obj);
				if (EngineProperties.showWireframe) {
					object.drawWireframe(gc, obj);
				}
				else {
					object.draw(gc);					
				}
			}
		}
		
		for (int obj = 0;obj < sceneObjects.forceActors.size();obj++) {
			if (sceneObjects.forceActors.get(obj) instanceof Drawable) {
				Drawable object = (Drawable) sceneObjects.forceActors.get(obj);
				if (EngineProperties.showWireframe) {
					//gc.fillText(STYLESHEET_CASPIAN, obj, fps);
					object.drawWireframe(gc, obj);
				}
				else {					
					object.draw(gc);
				}
			}
		}
		gc.setTextAlign(TextAlignment.LEFT);
		String text = "X:" + Math.round(mouse.getEntry(0)) + "("
		+ String.format("%.2f", mouse.getEntry(0) / EngineProperties.getPixelsPerMeter()) + 
				"m), Y:" + Math.round(mouse.getEntry(1)) +
				"(" + String.format("%.2f",mouse.getEntry(0) / EngineProperties.getPixelsPerMeter()) + "m)";
        gc.setFill(Color.WHITE);
        gc.fillText(text, mouse.getEntry(0)+ 10, mouse.getEntry(1) - 10);
        
        //draws the skeleton of polygon objects when they are being created
        if (masterController != null) {
        	drawPolygonSkeleton(ObjectSpawner.polygonPoints);        	
        	if (masterController.mouseSpring != null && !masterController.tbPlace.isSelected() &&
        			canvas.getOnMouseClicked() == null) {
        		masterController.mouseSpring.draw(gc);
        	}
        }
        
	}
	
	public void drawPolygonSkeleton(ArrayList<double[]> points) {
		if (points == null || points.size() == 0) { return; } 
		double[] x = new double[points.size()];
		double[] y = new double[points.size()];
		for (int i = 0;i < points.size();i++) {
			x[i] = points.get(i)[0];
			y[i] = points.get(i)[1];		
		}
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(2);		
		gc.strokePolygon(x, y, points.size());
		gc.fillOval(x[0],y[0], 2, 2);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		StackPane pane = new StackPane();
		canvas = new Canvas();
		gc = canvas.getGraphicsContext2D();
		
		canvas.widthProperty().bind(pane.widthProperty());
		canvas.heightProperty().bind(pane.heightProperty());
		pane.getChildren().add(canvas);
		
		Scene scene2 = new Scene(pane);
		scene2.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				
		scene2.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	drawFrame(0);
		    }
		});
		scene2.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	drawFrame(0);
		    }
		});
		
		EventHandler<MouseEvent> updateMousePosition = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				int mouseSteps = 5;
				mouse.setEntry(0, mouseSteps * Math.round(arg0.getX()/ mouseSteps));
				mouse.setEntry(1, mouseSteps * Math.round(arg0.getY()/ mouseSteps));				
			}			
		};
		
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, updateMousePosition);
		
		canvas.addEventHandler(MouseEvent.MOUSE_MOVED, updateMousePosition);
		
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setScene(scene2);
		stage.setTitle("World");
		stage.show();
	}
}
