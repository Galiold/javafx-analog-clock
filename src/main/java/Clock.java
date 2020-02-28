package main.java;

import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.stage.*;
import org.jblas.DoubleMatrix;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Clock extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static DoubleMatrix rotateClockwise(DoubleMatrix point, double angle) {
        DoubleMatrix rotationMatrixClockwise = new DoubleMatrix(new double[][] {{Math.cos(angle), -Math.sin(angle)}, {Math.sin(angle), Math.cos(angle)}});
        return rotationMatrixClockwise.mmul(point);
    }

    public void start(final Stage stage) throws Exception {
        final Circle face = new Circle(100, 100, 100);
        face.setId("face");

        final Line hourHand = new Line(0, 0, 0, -50);
        hourHand.setTranslateX(100);
        hourHand.setTranslateY(100);
        hourHand.setId("hourHand");

        final Line minuteHand = new Line(0, 0, 0, -75);
        minuteHand.setTranslateX(100);
        minuteHand.setTranslateY(100);
        minuteHand.setId("minuteHand");

        final Line secondHand = new Line(0, 0, 0, -83);
        secondHand.setTranslateX(100);
        secondHand.setTranslateY(100);
        secondHand.setId("secondHand");

        Group ticks = new Group();
        for (int i = 0; i < 60; i++) {
            Line tick = null;
            if (i % 5 == 0) {
                tick = new Line(0, -90, 0, -100);
                tick.getStyleClass().add("tick-heavy");
            } else {
                tick = new Line(0, -93, 0, -100);
                tick.getStyleClass().add("tick");
            }
            tick.setTranslateX(100);
            tick.setTranslateY(100);
            tick.getTransforms().add(new Rotate(i * (float)(360 / 60)));
            ticks.getChildren().add(tick);
        }

        final Label guide = new Label();
        guide.setId("guide");
        guide.setMinWidth(2 * face.getRadius());
        guide.setAlignment(Pos.CENTER);
        guide.setTranslateY(160);
        guide.setText("RClick to close");
        final Group analogueClock = new Group(face, ticks, hourHand, minuteHand, secondHand, guide);

        Calendar calendar = GregorianCalendar.getInstance();
        final double seedSecondDegrees = calendar.get(Calendar.SECOND) * (float)((2 * Math.PI) / 60);
        final double seedMinuteDegrees = (calendar.get(Calendar.MINUTE) + seedSecondDegrees / (2 * Math.PI)) * (float)((2 * Math.PI) / 60);
        final double seedHourDegrees = (calendar.get(Calendar.HOUR) + seedMinuteDegrees / (2 * Math.PI)) * (float)((2 * Math.PI) / 12);

        DoubleMatrix newSecond = rotateClockwise(new DoubleMatrix(2, 1, secondHand.getEndX(), secondHand.getEndY()), seedSecondDegrees);
        secondHand.setEndX(newSecond.get(0, 0));
        secondHand.setEndY(newSecond.get(1, 0));

        DoubleMatrix newMinute = rotateClockwise(new DoubleMatrix(2, 1, minuteHand.getEndX(), minuteHand.getEndY()), seedMinuteDegrees);
        minuteHand.setEndX(newMinute.get(0, 0));
        minuteHand.setEndY(newMinute.get(1, 0));

        DoubleMatrix newHour = rotateClockwise(new DoubleMatrix(2, 1, hourHand.getEndX(), hourHand.getEndY()), seedHourDegrees);
        hourHand.setEndX(newHour.get(0, 0));
        hourHand.setEndY(newHour.get(1, 0));

        final Runnable tickClock = new Runnable() {
            public void run() {
                DoubleMatrix newSecond = rotateClockwise(new DoubleMatrix(2, 1, secondHand.getEndX(), secondHand.getEndY()), (2 * Math.PI) / 60);
                secondHand.setEndX(newSecond.get(0, 0));
                secondHand.setEndY(newSecond.get(1, 0));

                DoubleMatrix newMinute = rotateClockwise(new DoubleMatrix(2, 1, minuteHand.getEndX(), minuteHand.getEndY()), (2 * Math.PI) / (60 * 60));
                minuteHand.setEndX(newMinute.get(0, 0));
                minuteHand.setEndY(newMinute.get(1, 0));

                DoubleMatrix newHour = rotateClockwise(new DoubleMatrix(2, 1, hourHand.getEndX(), hourHand.getEndY()), (2 * Math.PI) / (60 * 60 * 12));
                hourHand.setEndX(newHour.get(0, 0));
                hourHand.setEndY(newHour.get(1, 0));
            }
        };

        final ScheduledExecutorService ticker = Executors.newScheduledThreadPool(1);
        ticker.scheduleAtFixedRate(tickClock, 0, 1, TimeUnit.SECONDS);

        stage.initStyle(StageStyle.TRANSPARENT);

        analogueClock.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    ticker.shutdown();
                    stage.close();
                }
            }
        });

        final TextFlow layout = new TextFlow();
        layout.getChildren().add(analogueClock);
        final Scene scene = new Scene(layout, Color.TRANSPARENT);
        scene.getStylesheets().add(Clock.class.getResource("clock.css").toExternalForm());
        stage.setScene(scene);

        stage.show();
    }
}