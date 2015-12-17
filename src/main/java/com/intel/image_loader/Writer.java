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
import java.nio.ByteBuffer;

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

    public ByteBuffer preBuffer = ByteBuffer.allocate(4 * 2);

    public Writer(String uri) throws IOException {
        Path path = new Path(uri);
        this.seqFilePath = uri;
        writer = SequenceFile.createWriter(conf,
                SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(Text.class),
                SequenceFile.Writer.valueClass(Text.class)
        );
    }

    public void write(String fileName, byte[] img, int width, int height) throws Exception {
        try {
            preBuffer.putInt(width);
            preBuffer.putInt(height);
            byte[] data = new byte[preBuffer.capacity() + img.length];
            System.arraycopy(preBuffer.array(), 0, data, 0, preBuffer.capacity());
            System.arraycopy(img, 0, data, preBuffer.capacity(), img.length);
            preBuffer.clear();
            writer.append(new Text(fileName), new Text(data));
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
