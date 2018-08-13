package com.ktulsyan.curbside.models;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WorkPool<E> {

  private final PriorityBlockingQueue<FifoEntry<E>> queue;
  private final Comparator<? super E> comparator;

  public WorkPool() {
    queue = new PriorityBlockingQueue<>();
    comparator = null;
  }

  public WorkPool(Comparator<? super E> comparator) {
    this.comparator = comparator;
    queue = new PriorityBlockingQueue<>(10, (f1, f2) -> {
      int res = 0;
      if (comparator != null) {
        res = comparator.compare(f1.getEntry(), f2.getEntry());
      }
      if (res == 0) {
        res = f1.compareTo(f2);
      }
      return res;
    });
  }

  public void submit(E entry) {
    FifoEntry<E> fifoEntry = new FifoEntry<>(entry);
    queue.add(fifoEntry);
  }

  public E poll() throws InterruptedException {
    try {
      FifoEntry<E> fifoEntry = queue.poll(15, TimeUnit.SECONDS);
      if (fifoEntry == null) {
        return null;
      }
      return fifoEntry.getEntry();
    } catch (InterruptedException e) {
      throw e;
    }
  }

}
