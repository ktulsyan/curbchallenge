package com.ktulsyan.curbside;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.Nullable;

public class sol2 {

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
  @AllArgsConstructor
  static class NodeEntity {
    String id;
    int index;
  }
  private static final Queue<NodeEntity> queue = new ConcurrentLinkedQueue<>();
  private static final ListeningExecutorService executorService = MoreExecutors
      .listeningDecorator(Executors.newFixedThreadPool(2));

  static {
    queue.add(new NodeEntity("start", 0));
  }

  public static void main(String[] args) throws Exception {
    initialize();
    List<String> secretKeys = new ArrayList<>();
    Map<Integer, List<String>> treeMap = new TreeMap<>();
    Table<Integer, Integer, String> table = HashBasedTable.create();
    while (true) {
      if (queue.isEmpty()) {
        continue;
      }
      NodeEntity nodeEntity = queue.poll();
      String id = nodeEntity.id;
      int index = nodeEntity.index;
      ListenableFuture<Node> nodeFuture = fetchNode(id);
      Futures.addCallback(nodeFuture, new FutureCallback<Node>() {
        @Override
        public void onSuccess(@Nullable Node node) {
          if (!Strings.isNullOrEmpty(node.getSecret())) {
            System.out.println(node.getSecret() +", at depth" + node.getDepth());
            synchronized (treeMap) {
              if (!treeMap.containsKey(index)) {
                treeMap.put(index, new ArrayList<>());
              }
              treeMap.get(index).add(node.getSecret());
              table.put(node.getDepth(),index, node.getSecret() );
              if(VERBOSE) {
                System.out.println(new Gson().toJson(treeMap));
                System.out.println(table);
              }

            }
          }
          int newIndex = 1 + nodeEntity.index;
          for (String s : node.getNextIds()) {
            queue.add(new NodeEntity(s, newIndex));
            newIndex++;
          }
        }

        @Override
        public void onFailure(Throwable throwable) {
          throwable.printStackTrace(System.out);
        }
      }, executorService);
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
      long start = System.currentTimeMillis();
      res = httpClient.newCall(req).execute();
      System.out.println(System.currentTimeMillis() - start);

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
          System.out.printf("Response: %s\n", response);
        }
        return node;
      }
    }, MoreExecutors.directExecutor());
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
        long start = System.currentTimeMillis();
        res = httpClient.newCall(req).execute();
        System.out.println(System.currentTimeMillis() - start);

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

