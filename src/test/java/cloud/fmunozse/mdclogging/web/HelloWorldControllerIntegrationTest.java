package cloud.fmunozse.mdclogging.web;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cloud.fmunozse.mdclogging.DemoLogMdcAopAnnotationApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static cloud.fmunozse.mdclogging.config.aop.MDCLoggingAspect.KEY_MDC_LOGGING;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoLogMdcAopAnnotationApplication.class)
@AutoConfigureMockMvc
public class HelloWorldControllerIntegrationTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setUp() {
        Logger controllerLogger = (Logger) LoggerFactory.getLogger(HelloWorldController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        controllerLogger.addAppender(listAppender);

    }

    @After
    public void tearDown() throws Exception {
        listAppender.stop();
        listAppender.clearAllFilters();
    }

    @Test
    public void whenGetEndpointHasNotMDCParam_thenMdcIsEmpty() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-noTag")
                .param("name", "Stranger")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, Stranger!")));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.emptyMap());
    }


    @Test
    public void whenGetEndpointHas1MDCParam_thenMdcContainsTheMDCParamDefined() throws Exception {
        //given
        String expectedMdcMap = Collections.singletonMap("testKey", "pepe").toString();

        //when
        mvc.perform(get("/hello-world-1tag")
                .param("name", "pepe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.singletonMap(KEY_MDC_LOGGING, expectedMdcMap));
    }


    @Test
    public void whenGetEndpointHas1MDCParam_withEmptyValue_thenMdcContainsEmpty() throws Exception {
        //given

        //when
        mvc.perform(get("/hello-world-1tag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.emptyMap());
    }

    @Test
    public void whenGetEndpointHas2MDCParam_thenMdcContainsTheMDCParamDefined() throws Exception {
        //given
        Map<String, String> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("testKey", "pepe");
        expectedEntries.put("AnotherKey", "sr");
        String expectedMdcMap = expectedEntries.toString();

        //when
        mvc.perform(get("/hello-world-2tag")
                .param("name", "pepe")
                .param("title", "sr")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.singletonMap(KEY_MDC_LOGGING, expectedMdcMap));
    }

    @Test
    public void whenPostEndpointHasMDCParamUsingJsonPath_thenMdcContainsTheMDCParamDefined() throws Exception {
        //given
        Greeting greetingRequest = new Greeting(1L, "pepe");
        String expectedMdcMap = Collections.singletonMap("testKey", "1").toString();

        //when
        mvc.perform(post("/helloworld")
                .content(objectMapper.writeValueAsBytes(greetingRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.singletonMap(KEY_MDC_LOGGING, expectedMdcMap));

    }

    @Test
    public void whenPostEndpointHasMDCParamUsinJsonPath_withNotValue_thenMdcContainsEmpty() throws Exception {
        //given
        Greeting greetingRequest = new Greeting(null, "pepe");
        String expectedMdcMap = Collections.singletonMap("testKey", "1").toString();

        //when
        mvc.perform(post("/helloworld")
                .content(objectMapper.writeValueAsBytes(greetingRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", is("Hello, pepe!")));

        //then
        Assertions.assertThat(listAppender.list)
                .extracting(ILoggingEvent::getMDCPropertyMap)
                .contains(Collections.emptyMap());

    }

}