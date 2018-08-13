package com.ktulsyan.curbside.models;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Node {

  public static final TypeAdapter<Node> TYPE_ADAPTER = new TypeAdapter<Node>() {
    @Override
    public void write(JsonWriter out, Node value) {

    }

    @Override
    public Node read(JsonReader json) throws IOException {
      String id = null;
      int depth = 0;
      String message = null;
      List<String> nextIds = null;
      String secret = null;
      json.beginObject();
      while (json.hasNext()) {
        switch (json.nextName().toLowerCase()) {
          case "id":
            id = json.nextString();
            break;
          case "depth":
            depth = json.nextInt();
            break;
          case "message":
            message = json.nextString();
            break;
          case "secret":
            secret = json.nextString();
            if(secret.isEmpty()) {
              secret = null;
            }
          case "next":
            nextIds = new ArrayList<>();
            switch (json.peek()) {
              case STRING:
                nextIds.add(json.nextString());
                break;
              case BEGIN_ARRAY:
                json.beginArray();
                while (JsonToken.STRING == json.peek()) {
                  nextIds.add(json.nextString());
                }
                json.endArray();
            }
        }
      }
      json.endObject();

      return new Node(depth, id, message, secret, nextIds);
    }
  };

  @Getter int depth;
  String id;
  String message;
  @Getter String secret;
  @EqualsAndHashCode.Exclude
  @SerializedName("next")
  List<String> nextIds;

  @Nonnull
  public List<String> getNextIds() {
    return nextIds == null ? Collections.emptyList() : Collections.unmodifiableList(nextIds);
  }
}
