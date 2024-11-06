package com.itth.os.realtimechart.playground;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SubdividedBarChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();

        StackedBarChart<Number, String> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setStyle("-fx-bar-gap: 2px; -fx-category-gap: 10px;");

        XYChart.Series<Number, String> series1 = createSeries("Segment 1", new int[]{30, 50, 10});
        XYChart.Series<Number, String> series2 = createSeries("Segment 2", new int[]{40, 20, 20});
        XYChart.Series<Number, String> series3 = createSeries("Segment 3", new int[]{30, 30, 70});

        stackedBarChart.getData().addAll(series1, series2, series3);

        primaryStage.setScene(new Scene(stackedBarChart, 600, 400));
        primaryStage.setTitle("Horizontal Subdivided Bar Chart with Labels");
        primaryStage.show();
    }

    private XYChart.Series<Number, String> createSeries(String name, int[] values) {
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName(name);

        String[] categories = {"Category 1", "Category 2", "Category 3"};
        for (int i = 0; i < values.length; i++) {
            XYChart.Data<Number, String> data = new XYChart.Data<>(values[i], categories[i]);
            series.getData().add(data);
        }

        // Add labels after series are added to the chart
        series.getData().forEach(data -> {
            Label label = new Label(String.valueOf(data.getXValue()));
            label.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
            StackPane.setAlignment(label, javafx.geometry.Pos.CENTER);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.parentProperty().addListener((obs2, oldParent, newParent) -> {
                        if (newParent != null && newParent instanceof StackPane) {
                            StackPane parentStackPane = (StackPane) newParent;
                            parentStackPane.getChildren().add(label);
                        }
                    });
                }
            });
        });

        return series;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
