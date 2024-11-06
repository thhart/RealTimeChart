package com.itth.os.realtimechart.playground;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BarChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Value");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Series 1");
        series1.getData().add(new XYChart.Data<>("A", 50));
        series1.getData().add(new XYChart.Data<>("B", 80));
        series1.getData().add(new XYChart.Data<>("C", 90));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Series 2");
        series2.getData().add(new XYChart.Data<>("A", 40));
        series2.getData().add(new XYChart.Data<>("B", 60));
        series2.getData().add(new XYChart.Data<>("C", 30));

        barChart.getData().addAll(series1, series2);

        StackPane root = new StackPane(barChart);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bar Chart Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
