package com.intel.image_loader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Optional;
import static com.intel.image_loader.Utils.*;

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
        requires(tokens.length == 2);
        this.label = tokens[0];
        requires(this.label.equals(this.path.getParent().getFileName().toString()));
        String[] nameAndType = this.fileName.split("\\.");
        requires(nameAndType.length == 2);
        if (nameAndType[1].equals("JPEG")) {
            type = ImageType.JPEG;
        } else {
            throw new RuntimeException("Unsupported image type " + nameAndType[1]);
        }
    }

    public Optional<BufferedImage> load() {
        try {
            FileInputStream fis = new FileInputStream(path.toString());
            FileChannel channel = fis.getChannel();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            channel.transferTo(0, channel.size(), Channels.newChannel(byteArrayOutputStream));
            return Optional.of(ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
        } catch (Exception ex) {
            System.err.println("Can't read file " + fileName);
            return Optional.empty();
        }
    }
}
