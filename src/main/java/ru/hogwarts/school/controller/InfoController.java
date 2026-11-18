package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@RestController
public class InfoController {
    @Value("${server.port}")
    private String port;


    @GetMapping("/port")
    public String getPort() {
        return port;
    }

    @GetMapping("/complex-calculation")
    public int calculateSum(){
        //int sum = Stream.iterate(1, a -> a +1) .limit(1_000_000) .reduce(0, Integer::sum); //Метод по заданию.
        //sum = 1784293664; //Самый быстрый ответ
        int sum = IntStream.rangeClosed(1, 1_000_000).parallel().sum();
        return sum;
    }
}
