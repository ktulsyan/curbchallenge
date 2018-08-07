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
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
      json.beginObject();
      while (json.hasNext()) {
        switch (json.nextName()) {
          case "id":
            id = json.nextString();
            break;
          case "depth":
            depth = json.nextInt();
            break;
          case "message":
            message = json.nextString();
            break;
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

      return new Node(depth, id, message, nextIds);
    }
  };

  int depth;
  String id;
  String message;
  @EqualsAndHashCode.Exclude
  @SerializedName("next")
  List<String> nextIds;

  public List<String> getNextIds() {
    return Collections.unmodifiableList(nextIds);
  }
}
