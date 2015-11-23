package com.intel.image_loader;
import java.io.IOException;

import static com.intel.image_loader.Utils.*;

public class Verify {
    public static void main(String[] args) throws IOException {
        requires(args.length == 1, "Invalid arguments length");
        String file = args[0];

        Hadoop reader = new Hadoop();
        reader.verify(file);
    }
}
