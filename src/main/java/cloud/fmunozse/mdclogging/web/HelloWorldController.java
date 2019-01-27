package cloud.fmunozse.mdclogging.web;


import cloud.fmunozse.mdclogging.config.annotations.MDCEnable;
import cloud.fmunozse.mdclogging.config.annotations.MDCParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Controller
public class HelloWorldController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @MDCEnable
    @GetMapping("/hello-world-noTag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloNoTag(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "test1", required = false) String test1
    ) {
        log.info("testing get hello-world-noTag");
        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @MDCEnable
    @GetMapping("/hello-world-1tag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloOneTag(
            @RequestParam(name = "name", required = false) @MDCParam(key = "testKey") String name,
            @RequestParam(name = "test1", required = false) String test1
    ) {

        log.info("testing get hello-world-1tag with input name:{} ", name);

        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @MDCEnable
    @GetMapping("/hello-world-2tag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloTwoTag(
            @RequestParam(name = "name", required = false) @MDCParam(key = "testKey") String name,
            @RequestParam(name = "title", required = false) @MDCParam(key = "AnotherKey") String title) {

        log.info("testing get hello-world-2tag with inputs name:{} and title:{}", name, title);

        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }


    @MDCEnable
    @PostMapping("/helloworld")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloPost(@RequestBody @MDCParam(key = "testKey", jsonPathValue = "$.id") Greeting greeting) {

        log.info("testing post helloworld with input greeting:{}", greeting);

        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, greeting.getContent())));
    }


}