package com.ktulsyan.curbside;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Solution {

  private static final String baseUrl = "https://challenge.curbside.com/";
  private static final Gson gson = new GsonBuilder().serializeNulls().create();
  private static String sessionId;
  private static final OkHttpClient httpClient = new OkHttpClient();


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

  class Node {

    @SerializedName("depth")
    int depth;
    @SerializedName("id")
    String id;
    @SerializedName("message")
    String message;
    @SerializedName("next")
    List<String> nextIds;
  }

  /**
   * Paths on this url:
   * - start
   * - get-session
   */

  public static void main(String[] args) throws IOException {
    initialize();

    Node start = gson.fromJson(callUrl(baseUrl + Paths.start, getSessionHeader()), Node.class);

    System.out.println(start);
  }


  private static Map<String, String> getSessionHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put("session", sessionId);

    return headers;
  }

  private static String callUrl(String url, Map<String, String> headers) {
    Request.Builder builder = new Request.Builder().url(url);
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
      String response = callUrl(baseUrl + Paths.getSession, null);
      JsonObject json = gson.fromJson(response, JsonObject.class);
      sessionId = json.get("session").getAsString();
    }
  }
}
