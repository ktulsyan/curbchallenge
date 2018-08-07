package com.ktulsyan.curbside;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ktulsyan.curbside.models.Node;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Solution {

  private static final String baseUrl = "https://challenge.curbside.com/";
  private static final Gson gson = new GsonBuilder().serializeNulls().create();
  private static final boolean VERBOSE = true;
  private static String sessionId;
  private static final OkHttpClient httpClient = new OkHttpClient();

  private static final Queue<String> queue = new ConcurrentLinkedQueue<>();

  static {
    queue.add("start");
  }

  enum Paths {
    start("start"),
    getSession("get-session");

    private final String value;

    Paths(String s) {
      this.value = s;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /**
   * Paths on this url
   *
   * 1. start 2. get-session
   */

  public static void main(String[] args) throws IOException {
    initialize();

    while (!queue.isEmpty()) {
      String id = queue.poll();
      String response = callUrl(baseUrl + id);
      if (VERBOSE) {
        System.out.printf("Response: %s\n", response);
      }
      Node node = gson.fromJson(response, Node.class);
      System.out.printf("%s\n", node);
      queue.addAll(node.getNextIds());
    }
  }


  private static Map<String, String> getSessionHeader() {
    if (sessionId == null) {
      return null;
    }
    Map<String, String> header = new HashMap<>();
    header.put("session", sessionId);
    return header;
  }

  private static String callUrl(String url) {
    Request.Builder builder = new Request.Builder().url(url);

    Map<String, String> headers = getSessionHeader();

    if (headers != null) {
      headers.forEach(builder::addHeader);
    }

    Request req = builder.build();
    Response res;
    try {
      res = httpClient.newCall(req).execute();

      switch (res.code()) {
        case 200:
          return res.body().string();
        default:
          return null;
      }

    } catch (IOException e) {
      return null;
    }
  }

  private static void initialize() {
    if (sessionId == null) {
      String response = callUrl(baseUrl + Paths.getSession);
      JsonObject json = gson.fromJson(response, JsonObject.class);
      sessionId = json.get("session").getAsString();
      System.out.println("-----------SESSION-ID----------");
      System.out.println(sessionId);
      System.out.println("-------------------------------");
    }
  }
}
