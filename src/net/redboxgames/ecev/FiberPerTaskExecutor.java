package net.redboxgames.ecev;

import java.util.concurrent.Executor;

public final class FiberPerTaskExecutor implements Executor {
    private final FiberFactory threadFactory;

    public FiberPerTaskExecutor(FiberFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command);
    }
}
