package com.ktulsyan.curbside.models;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FifoEntryTest {

  @Test
  public void testComparableObjects() {
    int a = 1;
    int b = 0;

    FifoEntry<Integer> a1 = new FifoEntry<>(a);
    FifoEntry<Integer> b1 = new FifoEntry<>(b);
    assertTrue(a1.compareTo(b1) > 0);
  }

  @Test
  public void testIncomparables() {
    Object o = new Object();
    FifoEntry<Object> a1 = new FifoEntry<>(o);
    FifoEntry<Object> b1 = new FifoEntry<>(o);
    assertTrue(a1.compareTo(b1) < 0);
  }

}