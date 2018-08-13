package com.ktulsyan.curbside.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class WorkPoolTest {

  @Test
  public void testComparables() throws InterruptedException {
    WorkPool<Integer> wp = new WorkPool<>();
    wp.submit(1);
    wp.submit(0);

    assertEquals(wp.poll().intValue(), 0);
    assertEquals(wp.poll().intValue(), 1);
  }

  @Test
  public void testIncomparables() throws InterruptedException {
    List<Integer> l1 = new ArrayList<>();
    List<Integer> l2 = new ArrayList<>();
    WorkPool<List<Integer>> wp = new WorkPool<>();
    wp.submit(l1);
    wp.submit(l2);
    assertEquals(wp.poll(), l1);
    assertEquals(wp.poll(), l2);

  }

}