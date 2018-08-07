package com.ktulsyan.curbside.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Node {
  int depth;
  String id;
  String message;
  @EqualsAndHashCode.Exclude
  @SerializedName("next")
  List<String> nextIds;
}
