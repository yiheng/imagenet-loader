package com.intel.image_loader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.zookeeper.KeeperException;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.URI;

import static com.intel.image_loader.Utils.*;

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

    private DoubleWritable[] buffer = null;

    private final ArrayWritable value = new ArrayWritable(DoubleWritable.class);

    public Writer(String uri) throws IOException {
        Path path = new Path(uri);
        this.seqFilePath = uri;
        writer = SequenceFile.createWriter(conf,
                SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(ArrayWritable.class)
        );
    }

    public void write(String fileName, double[] img) throws Exception {
        if(buffer == null) {
            buffer = new DoubleWritable[img.length];
            for(int i = 0; i < img.length ; i++) {
                buffer[i] = new DoubleWritable();
            }
            value.set(buffer);
        }

        requires(buffer.length == img.length);
        for(int i = 0; i < img.length; i++) {
            buffer[i].set(img[i]);
        }

        try {
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
}
