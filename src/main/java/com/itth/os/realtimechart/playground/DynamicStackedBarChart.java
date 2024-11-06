package com.itth.os.realtimechart.playground;

import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DynamicStackedBarChart extends Application {

    private final XYChart.Series<Number, String> gapSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, String> dataSeries = new XYChart.Series<>();
    private int categoryCounter = 1;

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();

        StackedBarChart<Number, String> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle("Dynamic Stacked Bar Chart");
        xAxis.setLabel("Value");
        yAxis.setLabel("Category");

        gapSeries.setName("Gap");
        dataSeries.setName("Data");

        stackedBarChart.getData().addAll(gapSeries, dataSeries);

        // Button to add new bars with random gap size and bar value
        Button addButton = new Button("Add Bar");
        addButton.setOnAction(event -> {
            Random random = new Random();
            int gapSize = random.nextInt(50);
            int barValue = random.nextInt(50) + 1;
            addBar("Category " + categoryCounter, gapSize, barValue);
            categoryCounter++;
        });

        VBox controlBox = new VBox(addButton);
        controlBox.setPadding(new Insets(10));
        controlBox.setSpacing(10);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(stackedBarChart);
        borderPane.setRight(controlBox);

        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dynamic Stacked Bar Chart with Gaps");
        primaryStage.show();
    }

    private void addBar(String category, int gapSize, int barValue) {
        XYChart.Data<Number, String> gapData = new XYChart.Data<>(gapSize, category);
        gapSeries.getData().add(gapData);
        gapData.getNode().setStyle("-fx-bar-fill: transparent;");

        XYChart.Data<Number, String> data = new XYChart.Data<>(barValue, category);
        dataSeries.getData().add(data);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
