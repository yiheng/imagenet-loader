package com.intel.image_loader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

/**
 * Write image data into sequence file
 */
public class Writer {
    public final String seqFilePath;

    private static Configuration conf = new Configuration();
    private static FileSystem fs = null;
    static {
        try {
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            throw new RuntimeException("Can't initial file system");
        }
    }

    private final SequenceFile.Writer writer;

    public Writer(String filePath) throws IOException {
        this.seqFilePath = filePath;
        Path path = new Path(filePath);
        writer = SequenceFile.createWriter(conf,
                SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(String.class),
                SequenceFile.Writer.valueClass(double[].class)
        );
    }

    public void write(String fileName, String label, BufferedImage img) throws IOException {
        try {
            writer.append(fileName, convertTo2D(img));
        } catch (IOException e) {
            System.err.println("Can't write img " + img + " to sequence file " + this.seqFilePath);
            throw e;
        }
    }

    private static double[] convertTo2D(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        double[] result = new double[height * width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row * width + col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row * width + col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }
}
