package io.github.lgp547.anydoor.core;

import io.github.lgp547.anydoor.common.dto.AnyDoorRunDto;
import io.github.lgp547.anydoor.common.util.AnyDoorAopUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorBeanUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorClassUtil;
import io.github.lgp547.anydoor.common.util.AnyDoorSpringUtil;
import io.github.lgp547.anydoor.util.JsonUtil;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AnyDoorService {

    private static final String ANY_DOOR_RUN_MARK = "any-door run ";

    public AnyDoorService() {
    }

    public Object run(AnyDoorRunDto anyDoorDto) {
        try {
            anyDoorDto.verify();
            Class<?> clazz = Class.forName(anyDoorDto.getClassName());
            Method method = AnyDoorClassUtil.getMethod(clazz, anyDoorDto.getMethodName(), anyDoorDto.getParameterTypes());

            boolean containsBean = AnyDoorSpringUtil.containsBean(clazz);
            Object bean;
            if (!containsBean) {
                bean = AnyDoorBeanUtil.instantiate(clazz);
            } else {
                bean = AnyDoorSpringUtil.getBean(clazz);
                if (!Modifier.isPublic(method.getModifiers())) {
                    bean = AnyDoorAopUtil.getTargetObject(bean);
                }
            }
            return doRun(anyDoorDto, method, bean, () -> {}, () -> {});
        } catch (Exception e) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDto + "]");
            throw new RuntimeException(e);
        }
    }

    /**
     * {@code  io.github.lgp547.anydoor.attach.AnyDoorAttach#AnyDoorRun(String)}
     */
    public Object run(String anyDoorDtoStr, Method method, Object bean, Runnable startRun, Runnable endRun) {
        if (null == anyDoorDtoStr || anyDoorDtoStr.isEmpty()) {
            System.err.println("anyDoorService run param exception. anyDoorDtoStr is empty");
            return null;
        }
        if (null == method || null == bean) {
            System.err.println("anyDoorService run param exception. method or bean is null");
            return null;
        }

        try {
            Thread.currentThread().setContextClassLoader(AnyDoorService.class.getClassLoader());
            return doRun(JsonUtil.toJavaBean(anyDoorDtoStr, AnyDoorRunDto.class), method, bean, startRun, endRun);
        } catch (Throwable throwable) {
            System.err.println("anyDoorService run exception. param [" + anyDoorDtoStr + "]");
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
            return null;
        }
    }

    public Object doRun(AnyDoorRunDto anyDoorDto, Method method, Object bean, Runnable startRun, Runnable endRun) {
        String methodName = method.getName();
        String content = JsonUtil.toStrNotExc(anyDoorDto.getContent());
        return handleAndRun(anyDoorDto, method, bean, startRun, endRun, content, methodName);
    }

    private static Object handleAndRun(AnyDoorRunDto anyDoorDto, Method method, Object bean, Runnable startRun, Runnable endRun, String content, String methodName) {
        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        Integer num = anyDoorDto.getNum();
        List<Map<String, Object>> contentMaps;
        // 若是json数组，数量按照数组为准
        if (JsonUtil.isJsonArray(content)) {
            contentMaps = JsonUtil.toMaps(content);
            num = contentMaps.size();
        } else {
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            list.add(JsonUtil.toMap(content));
            contentMaps = list;
        }

        if (num < 1) {
            System.err.println("anyDoorService run param exception. num < 1");
            return null;
        }

        if (num == 1) {
            if (Objects.equals(anyDoorDto.getSync(), true)) {
                Object result = handlerMethod.invokeSync(startRun, contentMaps.get(0));
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toContentStrNotExc(result));
                endRun.run();
                return result;
            } else {
                handlerMethod.invokeAsync(startRun, contentMaps.get(0)).whenComplete(futureResultLogConsumer(methodName)).whenComplete((result, throwable) -> endRun.run());
                return null;
            }
        } else {
            if (anyDoorDto.getConcurrent()) {
                List<CompletableFuture<Object>> completableFutures =
                    handlerMethod.concurrentInvokeAsync(startRun, contentMaps, num, resultLogConsumer(methodName), excLogConsumer(methodName));
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).whenComplete((result, throwable) -> endRun.run());
            } else {
                if (Objects.equals(anyDoorDto.getSync(), true)) {
                    handlerMethod.parallelInvokeSync(startRun, contentMaps, num, resultLogConsumer(methodName));
                    endRun.run();
                } else {
                    CompletableFuture<Void> voidCompletableFuture = handlerMethod.parallelInvokeAsync(startRun, contentMaps, num, resultLogConsumer(methodName));
                    voidCompletableFuture.whenComplete((result, throwable) -> endRun.run());
                }
            }
            return null;
        }
    }

    private static BiConsumer<Integer, Object> resultLogConsumer(String methodName) {
        return (num, result) -> System.out.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " return: " + JsonUtil.toContentStrNotExc(result));
    }

    private static BiConsumer<Integer, Throwable> excLogConsumer(String methodName) {
        return (num, throwable) -> {
            System.err.println(ANY_DOOR_RUN_MARK + methodName + " " + num + " exception: " + throwable.getMessage());
            Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
        };
    }

    private static BiConsumer<Object, Throwable> futureResultLogConsumer(String methodName) {
        return (result, throwable) -> {
            if (throwable != null) {
                System.err.println(ANY_DOOR_RUN_MARK + methodName + " exception: " + throwable.getMessage());
                Optional.ofNullable(throwable.getCause()).map(Throwable::getCause).map(Throwable::getCause).orElse(throwable).printStackTrace();
            } else {
                System.out.println(ANY_DOOR_RUN_MARK + methodName + " return: " + JsonUtil.toContentStrNotExc(result));
            }
        };
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("str", "123");
        AnyDoorService bean = new AnyDoorService();
        Method method = bean.getClass().getMethod("test2", String.class, Integer.class);
        /*Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            System.out.println(parameter.getName());
        }*/
        AnyDoorHandlerMethod handlerMethod = new AnyDoorHandlerMethod(bean, method);
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter parameter = methodParameters[i];
            System.out.println(parameter);
            parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
            String parameterName = Optional.ofNullable(parameter.getParameterName()).orElse("arg" + i);
            System.out.printf("AnyDoorHandlerMethod.getArgs fori i=%d resolvedParameterName=%s %n", i, parameterName);
            // System.out.println(parameter.getParameterName());
            System.out.println(parameter.getParameterType());
        }
        System.out.println(handlerMethod);
    }

    public void test(String str) {
        System.out.println(str);
    }

    public void test2(String name, Integer age) {
        System.out.println(name + age);
    }
}
