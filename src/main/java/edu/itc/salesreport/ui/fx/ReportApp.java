package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

public class ReportApp extends Application {

    @Override
    public void start(Stage stage) {
        var parser = new CsvTransactionParser();
        var reader = new CsvMonthReader(parser);
        var aggregator = new DefaultSalesAggregator();
        var model = new SalesModel(reader, aggregator);
        var view = new ReportView(model);

        var controller = new ReportController(model, Path.of("data"), Clock.systemDefaultZone(), view);
        view.bind(controller);

        Scene scene = new Scene(view.root(), 960, 600);
        stage.setTitle("Angkor Mart · Monthly sales (MVC)");
        stage.setScene(scene);
        stage.show();

        controller.monthEntered("2026-09");

        if (Boolean.getBoolean("takeScreenshots")) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        try {
                            Files.createDirectories(Path.of("lab02", "screenshots"));
                            saveSnapshot(scene, new File("lab02/screenshots/mvc-viewer.png"));

                            controller.monthEntered("2026/09");
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    Platform.runLater(() -> {
                                        try {
                                            saveSnapshot(scene, new File("lab02/screenshots/validation.png"));
                                            Platform.exit();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Platform.exit();
                                        }
                                    });
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    Platform.exit();
                                }
                            }).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.exit();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private static void saveSnapshot(Scene scene, File targetFile) throws IOException {
        WritableImage image = scene.snapshot(null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(bufferedImage, "png", targetFile);
        System.out.println("Saved screenshot to " + targetFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
