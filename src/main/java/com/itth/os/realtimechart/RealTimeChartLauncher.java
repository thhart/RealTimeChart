package com.itth.os.realtimechart;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.*;

public class RealTimeChartLauncher extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initialize(primaryStage);
	}

	public RealTimeChart initialize(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("RealTimeChart.fxml"));
		Scene scene = loader.load();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		double stageX = screenBounds.getMaxX() - scene.getWidth();
		double stageY = screenBounds.getMinY();
		primaryStage.setTitle("Time Chart");
		primaryStage.setAlwaysOnTop(false);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setX(stageX);
		primaryStage.setY(stageY);
		primaryStage.setScene(scene);
		primaryStage.show();
		return loader.getController();
	}

}
