package net.redboxgames.ecev;

import java.util.concurrent.ForkJoinPool;

class FiberFactory  {
    public ForkJoinPool forkJoinPool = new ForkJoinPool(8);

    public Fiber newThread(Runnable r) {

        return Fiber.schedule(forkJoinPool,r);
    }
}