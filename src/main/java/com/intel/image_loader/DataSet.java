package com.intel.image_loader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Read imageNet data structure
 */
public class DataSet {
    private ConcurrentLinkedQueue<Image> imageList = new ConcurrentLinkedQueue<Image>();

    public DataSet(String pathName) throws IOException {
        Path path = Paths.get(pathName);

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
        System.out.println("Start to read directories...");
        for(Path p : directoryStream) {
            System.out.println("Read " + p.getFileName());
            DirectoryStream<Path> subDirectoryStream = Files.newDirectoryStream(p);
            for(Path image : subDirectoryStream) {
                imageList.add(new Image(image));
            }
        }
        System.out.println("Done");
    }

    public Optional<Image> fetch() {
        return Optional.ofNullable(imageList.poll());
    }
}
