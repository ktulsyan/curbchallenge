package com.ktulsyan.curbside.models;

import java.util.concurrent.atomic.AtomicLong;

public class FifoEntry<E> implements Comparable<FifoEntry<E>> {
  private static final AtomicLong eidSeq = new AtomicLong();

  private final long eventId;
  private final E entry;

  public FifoEntry(E entry) {
    eventId = eidSeq.incrementAndGet();
    this.entry = entry;
  }


  public E getEntry() {
    return entry;
  }

  @Override
  public int compareTo(FifoEntry<E> that) {
    int res = 0;
    if (entry instanceof Comparable) {
      res = ((Comparable<E>) this.entry).compareTo(that.entry);
    }
    if (res == 0) {
      res = this.eventId < that.eventId ? -1 : 1;
    }
    return res;
  }
}
