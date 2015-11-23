package com.intel.image_loader;

import java.io.IOException;
import static com.intel.image_loader.Utils.*;

/**
 * Main
 */
public class Main {
    public static void main(String[] args) throws IOException {
        requires(args.length == 3, "Invalid parameter length");
        String base = args[0];
        int parallel = Integer.valueOf(args[1]);
        String output = args[2];

        DataSet dataSet = new DataSet(base);
        Worker worker = new Worker(dataSet, parallel);
        int processed = worker.process(output);
        System.out.println("Processed " + processed + " files");
    }
}
