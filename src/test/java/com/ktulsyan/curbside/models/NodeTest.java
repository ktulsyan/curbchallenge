package com.ktulsyan.curbside.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

  @Test
  public void TypeAdapterTest() {
    final String json1 = "{\"id\":\"test-id\",\"depth\":0,\"message\":\"test-message\",\"next\":\"single-node\"}";
    final String json2 = "{\"id\":\"test-id\",\"depth\":0,\"message\":\"test-message\",\"next\":[\"multinode-1\",\"multinode-2\"]}";
    Gson gson = new GsonBuilder().registerTypeAdapter(Node.class, Node.TYPE_ADAPTER).create();

    assertNotNull(gson.fromJson(json1, Node.class));
    assertNotNull(gson.fromJson(json2, Node.class));

  }
}
