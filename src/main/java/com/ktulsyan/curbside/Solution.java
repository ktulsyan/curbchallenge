package com.ktulsyan.curbside;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ktulsyan.curbside.models.Node;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Solution {

  /**
   * available paths: \<id> get-session
   */
  private static final String baseUrl = "https://challenge.curbside.com/";

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(Node.class, Node.TYPE_ADAPTER)
      .serializeNulls()
      .create();
  private static final boolean VERBOSE = !true;
  private static String sessionId;
  private static final OkHttpClient httpClient = new OkHttpClient();

  private static final Queue<String> queue = new ConcurrentLinkedQueue<>();
  private static final ListeningExecutorService executorService = MoreExecutors
      .listeningDecorator(Executors.newFixedThreadPool(5));

  static {
    queue.add("start");
  }

  public static void main(String[] args) throws Exception {
    initialize();

    while (!queue.isEmpty()) {
      String id = queue.poll();
      Node node = fetchNode(id);
      queue.addAll(node.getNextIds());
    }
  }


  private static Map<String, String> getSessionHeader() {
    if (sessionId == null) {
      return null;
    }
    return Collections.singletonMap("session", sessionId);
  }

  private static String callUrl_orig(String url) {
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

  private static Node fetchNode(String nodeId) throws Exception {
    return Futures.transform(callUrl(baseUrl + nodeId), new Function<String, Node>() {
      @Nullable
      @Override
      public Node apply(@Nullable String response) {
        if (VERBOSE) {
          System.out.printf("Response: %s\n", response);
        }
        Node node = gson.fromJson(response, Node.class);
        if (node == null) {
          System.out.printf("Response: %s\n", response);
        }
        System.out.printf("%s\n", node);
        return node;
      }
    }, MoreExecutors.directExecutor()).get();
  }

  private static ListenableFuture<String> callUrl(final String url) {
    return executorService.submit(() -> {
      Request.Builder builder = new Request.Builder();
      Map<String, String> headers = getSessionHeader();
      if (headers != null) {
        headers.forEach(builder::addHeader);
      }
      Request req = builder.url(url).build();
      Response res;
      try {
        res = httpClient.newCall(req).execute();

        switch (res.code()) {
          case 200:
            return res.body().string();
          default:
            System.out.printf("Recd http %d with body %s", res.code(), res.body().string());
            return null;
        }

      } catch (IOException e) {
        return null;
      }
    });
  }

  private static void initialize() {
    if (sessionId == null) {
      String response = callUrl_orig(baseUrl + "get-session");
      JsonObject json = gson.fromJson(response, JsonObject.class);
      sessionId = json.get("session").getAsString();
      System.out.println("-----------SESSION-ID----------");
      System.out.println(sessionId);
      System.out.println("-------------------------------");
    }

    if (sessionId == null) {
      Futures.addCallback(callUrl(baseUrl + "get-session"), new FutureCallback<String>() {
        @Override
        public void onSuccess(@Nullable String response) {
          JsonObject json = gson.fromJson(response, JsonObject.class);
          sessionId = json.get("session").getAsString();
          System.out.println("-----------SESSION-ID----------");
          System.out.println(sessionId);
          System.out.println("-------------------------------");
        }

        @Override
        public void onFailure(Throwable throwable) {
          System.out.println("Unable to fetch sessionId!!!");
          System.out.println(throwable);
        }
      }, MoreExecutors.directExecutor());
    }
  }
}
