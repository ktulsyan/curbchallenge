package com.ktulsyan.curbside.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import org.junit.Test;

public class NodeTest {

  @Test
  public void EqualsAndHashCodeTest() {
    Node a = new Node(0, "id", "msg", null);
    Node b = new Node(0, "id", "msg", new ArrayList<>());

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}