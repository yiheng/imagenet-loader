package com.intel.image_loader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

enum ImageType {
    JPEG
}

/**
 * Image file
 */
public class Image {
    public final String fileName;
    public final Path path;
    public final String label;
    public final ImageType type;

    public Image(Path path) {
        this.path = path;
        this.fileName = this.path.getFileName().toString();
        String[] tokens = this.fileName.split("_");
        assert(tokens.length == 2);
        this.label = tokens[0];
        assert(this.label == this.path.getParent().getFileName().toString());
        String[] nameAndType = this.fileName.split("\\.");
        assert(nameAndType.length == 2);
        if (nameAndType[2].equals("JPEG")) {
            type = ImageType.JPEG;
        } else {
            throw new RuntimeException("Unsupported image type");
        }
    }

    public Image(String path) {
        this(Paths.get(path));
    }
}
