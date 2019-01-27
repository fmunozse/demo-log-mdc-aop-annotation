package cloud.fmunozse.mdclogging.config.aop;

import cloud.fmunozse.mdclogging.config.annotations.MDCEnable;
import cloud.fmunozse.mdclogging.config.annotations.MDCParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component
public class MDCLoggingAspect {

    public static final String KEY_MDC_LOGGING = "mdcLogging";

    ObjectMapper objectMapper;

    public MDCLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(cloud.fmunozse.mdclogging.config.annotations.MDCEnable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        //Validate correct use of annotation
        if (request == null)
            throw new RuntimeException(String.format("@MDCEnable is only allow for Rest Controllers. %s - %s",
                    joinPoint.getTarget().getClass().getName(), joinPoint.getSignature().getName()));

        //Preparation inputJSON
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        MDCEnable annotation = method.getAnnotation(MDCEnable.class);
        Map<String, String> mdcParams = getInputMDCParams(method, joinPoint.getArgs());

        //Setting in the Mapped Diagnostic Context
        if (!mdcParams.isEmpty()) {
            MDC.put(KEY_MDC_LOGGING, mdcParams.toString());
        }

        //Call to the method
        Object proceed = joinPoint.proceed();

        //Restart the context
        MDC.clear();

        return proceed;

    }


    @SneakyThrows
    private Map<String, String> getInputMDCParams(Method method, Object[] args) {
        Map<String, String> keyParams = new LinkedHashMap();

        Annotation[][] annotationMatrix = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Annotation[] annotations = annotationMatrix[i];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == MDCParam.class) {
                    MDCParam MDCParam = (MDCParam) annotation;

                    Object value = MDCParam.jsonPathValue().isEmpty() ?
                            args[i] :
                            (Object) JsonPath
                                    .parse(objectMapper.writeValueAsString(args[i]))
                                    .read(MDCParam.jsonPathValue());

                    if (value != null) {
                        keyParams.put(MDCParam.key(), String.valueOf(value));
                    }
                }
            }
        }
        return keyParams;
    }

}

