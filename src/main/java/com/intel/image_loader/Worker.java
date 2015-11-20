package com.intel.image_loader;

import java.awt.image.BufferedImage;
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

    public void process(final String target) {
        Future<Integer>[] results = new Future[parallel];

        for(int i = 0; i < parallel; i++) {
            final int tid = i;
            results[i] = threadPool.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    String file = String.format("%s-%d.seq", target, tid);
                    Writer writer = new Writer(target);
                    Optional<Image> imgOpt = dataSet.fetch();
                    int processed = 0;
                    while(imgOpt.isPresent()) {
                        Image img = imgOpt.get();
                        Optional<BufferedImage> dataOpt = img.load();
                        if(dataOpt.isPresent()) {
                            writer.write(img.fileName, img.label, dataOpt.get());
                            processed++;
                        }
                    }
                    return processed;
                }
            });
        }

        threadPool.shutdown();
        for(int i = 0; i < parallel; i++) {
            try {
                results[i].get();
            } catch (InterruptedException e) {
                System.err.println("Processing is interrupted");
            } catch (ExecutionException e) {
                System.err.println("Processing meet error, exit");
            }
        }
    }
}
