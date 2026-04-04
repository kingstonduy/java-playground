package me.personal.aop;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

public class Service {
    public void doSth() {

    }
}

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class Param {
    String name;
    Integer age;
    String email;
}

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class Result {
    String address;
    String hobby;
}
