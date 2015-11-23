package com.intel.image_loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Parallel worker to write image file into sequence files
 */
public class Worker {
    private final DataSet dataSet;
    private final int parallel;
    private final ExecutorService threadPool;


    public Worker(DataSet dataSet, int parallel) {
        this.dataSet = dataSet;
        this.parallel = parallel;
        this.threadPool = Executors.newFixedThreadPool(parallel);
    }

    public int process(final String target) {
        Future<Integer>[] results = new Future[parallel];

        for(int i = 0; i < parallel; i++) {
            final int tid = i;
            results[i] = threadPool.submit(new Callable<Integer>() {
                public Integer call() throws Exception {
                    String file = String.format("%s-%d.seq", target, tid);
                    Hadoop hadoop = new Hadoop(file);
                    Optional<Image> imgOpt = dataSet.fetch();
                    int processed = 0;
                    while(imgOpt.isPresent()) {
                        Image img = imgOpt.get();
                        Optional<BufferedImage> dataOpt = img.load();
                        if(dataOpt.isPresent()) {
                            try {
                                hadoop.write(img.fileName, img.label, dataOpt.get());
                                processed++;
                            } catch (Exception e) {
                                System.err.println("Can't write img " + img.path + " to sequence file " + file);
                            }
                        }
                        imgOpt = dataSet.fetch();
                    }
                    hadoop.close();
                    return processed;
                }
            });
        }

        threadPool.shutdown();
        int processed = 0;
        for(int i = 0; i < parallel; i++) {
            try {
                processed += results[i].get();
            } catch (InterruptedException e) {
                System.err.println("Processing is interrupted");
                return processed;
            } catch (ExecutionException e) {
                System.err.println("Processing meet error, exit");
                return processed;
            }
        }
        return processed;
    }
}
