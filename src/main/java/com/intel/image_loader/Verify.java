package com.intel.image_loader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.net.URI;

import static com.intel.image_loader.Utils.*;

public class Verify {
    public static void main(String[] args) throws IOException {
        requires(args.length == 1, "Invalid arguments length");
        String file = args[0];
        verify(file);
    }

    private static void verify(String uri) throws IOException {
        System.setProperty("hadoop.home.dir", "/");
        Path path = new Path(uri);
        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        Writable key = (Writable)
                ReflectionUtils.newInstance(reader.getKeyClass(), conf);
        Writable value = new ArrayWritable(DoubleWritable.class);
        long position = reader.getPosition();
        while (reader.next(key, value)) {
            String syncSeen = reader.syncSeen() ? "*" : "";
            System.out.printf("[%s%s]\t%s\t%s\n", position, syncSeen, key, value);
            position = reader.getPosition(); // beginning of next record
        }
    }
}
