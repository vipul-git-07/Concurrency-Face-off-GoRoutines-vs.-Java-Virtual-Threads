package org.vatsuvaksi.executor;
import java.util.concurrent.*;

public class CustomExecutor {
    private static final int THREADS = Runtime.getRuntime().availableProcessors() - 1;
    private static  final ExecutorService executorService  = new ThreadPoolExecutor(
            THREADS,
            THREADS,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>()
    );;


    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
