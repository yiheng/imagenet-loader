package com.intel.image_loader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URI;

/**
 * Write image data into sequence file
 */
public class Writer {
    public final String seqFilePath;

    private static Configuration conf = new Configuration();
    static {
        System.setProperty("hadoop.home.dir", "/");
    }

    private final SequenceFile.Writer writer;

    private final ArrayWritable value = new ArrayWritable(DoubleWritable.class);

    public Writer(String uri) throws IOException {
        Path path = new Path(uri);
        this.seqFilePath = uri;
        writer = SequenceFile.createWriter(conf,
                SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(ArrayWritable.class)
        );

        path = null;
    }

    public void write(String fileName, String label, BufferedImage img) throws Exception {
        try {
            value.set(convertTo2D(img));
            writer.append(new Text(fileName), value);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
        }
    }

    private static DoubleWritable[] convertTo2D(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        DoubleWritable[] result = null;
        if (hasAlphaChannel) {
            result =  new DoubleWritable[pixels.length / 4];
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row * width + col] = new DoubleWritable(argb);
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            result =  new DoubleWritable[pixels.length / 3];
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row * width + col] = new DoubleWritable(argb);
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
