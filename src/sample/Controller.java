package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;

import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.IntSummaryStatistics;
import java.util.Optional;
import java.util.stream.IntStream;

public class Controller {

    public ImageView imageView;
    public Pane pane;
    public GridPane gridPane;
    public TextField widthTextField;
    public TextField heightTextField;
    public TextField maxPixelValueTextField;
    public TextField minPixelValueField;
    public Label imageLabel;
    public TextField depthTextField;
    public MenuItem crop;
    public MenuItem saveHeighmap;
    private BufferedImage originalBufferedImage;
    private BufferedImage croppedBufferedImage;


    public void openFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterPNG);
        imageView.fitWidthProperty().bind(pane.widthProperty());
        imageView.fitHeightProperty().bind(pane.heightProperty());
        File file = fileChooser.showOpenDialog(gridPane.getScene().getWindow());
        if (file != null) {
            imageLabel.setText(file.getName());
            try {
                originalBufferedImage = ImageIO.read(file);
                setImageAndCalculateLabels(originalBufferedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageAndCalculateLabels(BufferedImage bufferedImage) {
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setImage(image);
        crop.setDisable(false);
        Raster raster = bufferedImage.getData();
        depthTextField.setText(String.valueOf(bufferedImage.getColorModel().getPixelSize()));
        short[] pixels = ((DataBufferUShort) raster.getDataBuffer()).getData();
        IntSummaryStatistics intSummaryStatistics = IntStream.range(0, pixels.length)
                                                             .map(i -> Short.toUnsignedInt(pixels[i]))
                                                             .summaryStatistics();
        widthTextField.setText(String.valueOf(bufferedImage.getWidth()));
        heightTextField.setText(String.valueOf(bufferedImage.getHeight()));
        maxPixelValueTextField.setText(String.valueOf(intSummaryStatistics.getMax()));
        minPixelValueField.setText(String.valueOf(intSummaryStatistics.getMin()));
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void cropImage(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Crop Image");
        textInputDialog.setHeaderText("Crop");
        textInputDialog.setContentText("Enter resolution");

        Optional<String> result = textInputDialog.showAndWait();
        result.ifPresent(resolution -> {
            int a = Integer.parseInt(resolution);
            int diff = originalBufferedImage.getWidth() - a;
            int diffEven = diff / 2;
            int diffOdd = diffEven + diff % 2;

            BufferedImage subBufferedImage = originalBufferedImage.getSubimage(diffEven,
                                                                               diffEven,
                                                                               originalBufferedImage.getWidth() - diffEven - diffOdd,
                                                                               originalBufferedImage.getWidth() - diffEven - diffOdd);

            croppedBufferedImage = new BufferedImage(subBufferedImage.getColorModel(),
                                                     subBufferedImage.getRaster()
                                                                     .createCompatibleWritableRaster(
                                                                             originalBufferedImage.getWidth() - diffEven - diffOdd,
                                                                             originalBufferedImage.getWidth() - diffEven - diffOdd),
                                                     subBufferedImage.isAlphaPremultiplied(),
                                                     null);
            subBufferedImage.copyData(croppedBufferedImage.getRaster());
            setImageAndCalculateLabels(croppedBufferedImage);
            saveHeighmap.setDisable(false);
        });

    }

    public void save() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterPNG);

        File file = fileChooser.showSaveDialog(gridPane.getScene().getWindow());

        try {
            ImageIO.write(croppedBufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
