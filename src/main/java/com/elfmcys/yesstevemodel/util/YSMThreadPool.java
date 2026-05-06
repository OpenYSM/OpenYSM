package com.elfmcys.yesstevemodel.util;

import java.util.concurrent.*;

public final class YSMThreadPool {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(Math.max(2, Runtime.getRuntime().availableProcessors() / 2), Math.max(2, Runtime.getRuntime().availableProcessors() / 2), 30, TimeUnit.SECONDS, new LinkedBlockingQueue(), runnable -> {
        Thread thread = new Thread(runnable, "YSM Worker");
        thread.setPriority(5);
        thread.setDaemon(true);
        return thread;
    });

    public static Future<?> submit(Runnable runnable) {
        return EXECUTOR.submit(runnable);
    }

    public static <T> Future<T> submitCallable(Callable<T> callable) {
        return EXECUTOR.submit(callable);
    }

    public static boolean awaitTermination(int i) {
        try {
            Thread.sleep(i);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}