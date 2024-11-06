package com.itth.os.realtimechart.playground;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class StackedBarChartWithGaps extends Application {

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();

        StackedBarChart<Number, String> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle("Stacked Bar Chart with Gaps");
        xAxis.setLabel("Value");
        yAxis.setLabel("Category");

        stackedBarChart.setLegendVisible(false);

        XYChart.Series<Number, String> gapSeries = new XYChart.Series<>();
        gapSeries.setName("Gap");

        XYChart.Series<Number, String> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");

        // Category 1 starts at value 10 with a length of 30
        gapSeries.getData().add(new XYChart.Data<>(10, "Category 1"));
        dataSeries.getData().add(new XYChart.Data<>(30, "Category 1"));

        // Category 2 starts at value 20 with a length of 40
        gapSeries.getData().add(new XYChart.Data<>(20, "Category 2"));
        dataSeries.getData().add(new XYChart.Data<>(40, "Category 2"));

        // Category 3 starts at value 15 with a length of 25
        gapSeries.getData().add(new XYChart.Data<>(15, "Category 3"));
        dataSeries.getData().add(new XYChart.Data<>(25, "Category 3"));

        stackedBarChart.getData().addAll(gapSeries, dataSeries);
        gapSeries.getData().forEach(data -> {
data.getNode().setStyle("-fx-bar-fill: transparent;");
});

        Scene scene = new Scene(stackedBarChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Stacked Bar Chart with Gaps");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
