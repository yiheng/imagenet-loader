package com.intel.image_loader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.net.URI;

import static com.intel.image_loader.Utils.*;

public class Verify {
    public static void main(String[] args) throws IOException {
        requires(args.length == 2, "Invalid arguments length");
        String file = args[0];
        String target = args[1];
        verify(file, target);
    }

    private static void verify(String uri, String target) throws IOException {
        System.setProperty("hadoop.home.dir", "/");
        Path path = new Path(uri);
        Configuration conf = new Configuration();

        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        Writable key = (Writable)
                ReflectionUtils.newInstance(reader.getKeyClass(), conf);
        Text value = new Text();
        long position = reader.getPosition();
        while (reader.next(key, value)) {
            String syncSeen = reader.syncSeen() ? "*" : "";
            byte[] data = value.getBytes();
            System.out.printf("[%s%s]\t%s\t Array with length(%s)\n", position, syncSeen, key, data.length);
            if(key.toString().equals(target)) {
                System.out.println("Extact " + target + " to " + target + ".jpeg");
                Image.extract(data, target + ".jpeg");
            }
            position = reader.getPosition(); // beginning of next record
        }
    }
}
