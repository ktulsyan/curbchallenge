package com.ktulsyan.curbside;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ktulsyan.curbside.models.Node;
import com.ktulsyan.curbside.models.WorkPool;
import com.sun.tools.javac.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

  private static final WorkPool<Pair<Long, String>> workPool = new WorkPool<>(Comparator.comparingLong(p -> p.fst));
  private static final ListeningExecutorService networkExecutorService = MoreExecutors
      .listeningDecorator(Executors.newFixedThreadPool(60));
  private static final ListeningExecutorService callbackExecutorService = MoreExecutors
      .listeningDecorator(Executors.newFixedThreadPool(5));

  static {
    workPool.submit(new Pair<>(0L, "start"));
  }

  public static void main(String[] args) throws Exception {
    initialize();
    boolean shutDown = false;
    List<Pair<Long, String>> results = new ArrayList<>();
    while (!shutDown) {
      final Pair<Long, String> p = workPool.poll();
      if(p == null) {
        System.out.println("Recd null object from work queue, initiating shutdown");
        networkExecutorService.shutdown();
        System.out.println("Shutdown initiated, awaiting termination...");
        networkExecutorService.awaitTermination(60, TimeUnit.SECONDS);
        System.out.println("Terminated..");
        shutDown = true;
        continue;
      }
      ListenableFuture<Node> nodeFuture = fetchNode(p.snd);
      Futures.addCallback(nodeFuture, new FutureCallback<Node>() {
        @Override
        public void onSuccess(@Nullable Node node) {
          if (!Strings.isNullOrEmpty(node.getSecret())) {
            results.add(Pair.of(p.fst, node.getSecret()));
          }
          if (VERBOSE) {
            System.out.println(node);
          }
          long base = p.fst * 10;
          int i = 1;
          for(String s : node.getNextIds()) {
            workPool.submit(Pair.of(base + i++, s));
          }
        }

        @Override
        public void onFailure(Throwable throwable) {
          throwable.printStackTrace(System.out);
        }
      }, callbackExecutorService);
    }
    callbackExecutorService.shutdown();
    callbackExecutorService.awaitTermination(15, TimeUnit.SECONDS);

    System.out.println("----Finished Graph Traversal----");
    results.sort(Comparator.comparingLong(p -> p.fst));
    results.forEach(p -> System.out.print(p.snd));
    System.out.println();
  }

  private static Map<String, String> getSessionHeader() {
    if (sessionId == null) {
      return null;
    }
    return Collections.singletonMap("session", sessionId);
  }

  private static ListenableFuture<Node> fetchNode(String nodeId) throws Exception {
    return Futures.transform(callUrl(baseUrl + nodeId), new Function<String, Node>() {
      @Nullable
      @Override
      public Node apply(@Nullable String response) {
        if (VERBOSE) {
          System.out.printf("Response: %s\n", response);
        }
        Node node = gson.fromJson(response, Node.class);
        if (node == null) {
          System.out.printf("Null node!!! Response: %s\n", response);
        }
        return node;
      }
    }, MoreExecutors.directExecutor());
  }

  private static ListenableFuture<String> callUrl(final String url) {
    return networkExecutorService.submit(() -> {
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
            System.out.printf("Recd http %d with body %s for request %s\n", res.code(),
                res.body().string(), req.toString());
            return null;
        }

      } catch (IOException e) {
        return null;
      }
    });
  }

  private static String callUrlSync(final String url) {
    try {
      return callUrl(url).get();
    } catch (Exception e) {
      System.out.println(e);
      return null;
    }
  }

  private static void initialize() {
    if (sessionId == null) {
      String response = callUrlSync(baseUrl + "get-session");
      JsonObject json = gson.fromJson(response, JsonObject.class);
      sessionId = json.get("session").getAsString();
      System.out.println("-----------SESSION-ID----------");
      System.out.println(sessionId);
      System.out.println("-------------------------------");
    }
  }
}
