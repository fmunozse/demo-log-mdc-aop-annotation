# demo-log-mdc-aop-annotation

## Explanation 
Application to create a Annotation at level of controller to set MDC Mapped Diagnostic Context  to can 
defined the MDC in a declarative way. 

## Use
Created 2 annotations

| Annotations| Description  |
| :--------- | :----------- |
| MDCEnable  | Just to set indicate that the method will contains MDCParam to set information in the context |
| MDCParam   | `key`: to indicate the key to set in the MDC Context. <br>`jsonPathValue`: Use a JsonPath expression to extract the value|

Using the annotation, basically, create a custom variable `mdcLogging` in the context MDC including the keys that has been defined in the method. 
Then, this variable could be use in the log pattern to add the information in the logs. 

See the examples for a better understand.

### Use in a GET endpoint and setting just 1 parameter to include in the MDC context
```java
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
```
When is call to this endpoint with param name with value "1234", then, in the logs you can find the mdc printed `{testKey=1234}`:
```
2019-01-27 22:02:02.834  INFO  25641 --- [           main] .m.w.HelloWorldControllerIntegrationTest : Started HelloWorldControllerIntegrationTest in 5.327 seconds (JVM running for 7.241)
2019-01-27 22:02:03.289  INFO {testKey=1234} 25641 --- [           main] c.f.mdclogging.web.HelloWorldController  : testing get hello-world-1tag with input name:pepe 
```


----

### Use in a GET endpoint and setting just 2 parameter to include in the MDC context
```java
    @MDCEnable
    @GetMapping("/hello-world-2tag")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloTwoTag(
            @RequestParam(name = "name", required = false) @MDCParam(key = "testKey") String name,
            @RequestParam(name = "title", required = false) @MDCParam(key = "AnotherKey") String title) {

        log.info("testing get hello-world-2tag with inputs name:{} and title:{}", name, title);

        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }
```
When is call to this endpoint with param name with value "1234" and title with "sr"
then, in the logs you can find the mdc printed `{testKey=1234, AnotherKey=sr}`:
```
2019-01-27 22:05:27.569  INFO  25647 --- [           main] .m.w.HelloWorldControllerIntegrationTest : Started HelloWorldControllerIntegrationTest in 5.649 seconds (JVM running for 7.492)
2019-01-27 22:05:28.080  INFO {testKey=1234, AnotherKey=sr} 25647 --- [           main] c.f.mdclogging.web.HelloWorldController  : testing get hello-world-2tag with inputs name:pepe and title:sr 
```

----

### Use in a POST endpoint and using JsonPath to extract the value to include in the MDC context
```java
    @MDCEnable
    @PostMapping("/helloworld")
    @ResponseBody
    public ResponseEntity<Greeting> sayHelloPost(@RequestBody @MDCParam(key = "testKey", jsonPathValue = "$.id") Greeting greeting) {

        log.info("testing post helloworld with input greeting:{}", greeting);

        return ResponseEntity.ok(new Greeting(counter.incrementAndGet(), String.format(template, greeting.getContent())));
    }
    
    .... 
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Greeting {
        private Long id;
        private String content;
    }
    
```
When is call to this endpoint with `Greeting greetingRequest = new Greeting(1L, "xxxx");`
then, in the logs you can find the mdc printed `{testKey=1}`:
```
2019-01-27 22:10:54.320  INFO  25659 --- [           main] .m.w.HelloWorldControllerIntegrationTest : Started HelloWorldControllerIntegrationTest in 5.635 seconds (JVM running for 7.296)
2019-01-27 22:10:54.916  INFO {testKey=1} 25659 --- [           main] c.f.mdclogging.web.HelloWorldController  : testing post helloworld with input greeting:Greeting(id=1, content=pepe)
```


## Possible options to set the log pattern

### Setting at the begging of the line
This configuration is set in the part of LEVEL of the pattern.
```yaml
logging:
  pattern:
    level: "%5p %X{mdcLogging}"
```


### Setting like part of the pattern log
This configuration needs to setup the pattern ... Notices the mdcLogging vbo set at the end.
```yaml
logging:
  pattern:
    console: "%d %-5level [%thread] %logger{0} : %msg %X{mdcLogging}%n"
```

### use a 
In the springboot release, you can find the default used by springboot in
spring-boot-2.0.5.RELEASE.jar!/org/springframework/boot/logging/logback/defaults.xml. 
Something that could be done to get the most close to default is to use "debugger" tool to check in the unitest the pattern. 
```yaml
logging:
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(18971){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m %X{mdcLogging}%n%wEx"
```


