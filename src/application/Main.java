	package application;
	
import javafx.application.Application;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			//creating the Editor Window
			FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EngineMaster.fxml"));
			AnchorPane root = (AnchorPane)loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Editor");
			primaryStage.show();
			
			Stage engineCanvasWindow = new Stage();
			EngineWorld canvas = new EngineWorld();
			canvas.start(engineCanvasWindow);
			
			primaryStage.setOnCloseRequest(event -> {
				engineCanvasWindow.close();
			});
			engineCanvasWindow.setOnCloseRequest(event -> {
				primaryStage.close();
			});
			
			EngineMasterController masterController = loader.getController();
			masterController.setCanvas(canvas);
			canvas.setMasterController(masterController);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
}	
