package application;

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
import javafx.stage.Modality;
import javafx.stage.Stage;
import physicsComponents.Drawable;

public class EngineCanvas extends Application {
	SceneObjects sceneObjects = SceneObjects.getInstance();
	
	EngineMasterController masterController;
	Canvas canvas;
	GraphicsContext gc;
	RealVector mousePosition = new ArrayRealVector(new double[] {0, 0});
	
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
		
		String text = "X:" + Math.round(mousePosition.getEntry(0)) + ", Y:" + Math.round(mousePosition.getEntry(1));
        gc.setFill(Color.WHITE);
        gc.fillText(text, mousePosition.getEntry(0)+ 10, mousePosition.getEntry(1) - 10);

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
		
		canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
        	@Override public void handle(MouseEvent event) {          
                mousePosition.setEntry(0, event.getX());
                mousePosition.setEntry(1, event.getY());
                if (!EngineProperties.isPlaying) {                	
                	drawFrame(0);
                }
              }
        });
		
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setScene(scene2);
		stage.setTitle("Canvas");
		stage.show();
	}
}
