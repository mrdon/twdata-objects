package org.twdata.objects;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class TokenCreator
{
    private static final Object UNKNOWN = new Object();

    public static <M> M createToken(Class<M> tokenClass, final String data)
    {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(tokenClass);
            enhancer.setInterfaces(new Class[] {Sourced.class});
            enhancer.setCallbackFilter(LazyCallbackFilter.INSTANCE);
            enhancer.setCallbackTypes(new Class[] {
                    NoOp.class,
                    LazyMethodInterceptor.class,
                    FixedValue.class,
            });
            enhancer.setCallbacks(new Callback[]{
                    NoOp.INSTANCE, new LazyMethodInterceptor(data),
                    new FixedValue() {

                        public Object loadObject() throws Exception
                        {
                            return data;
                        }
                    }
            });
            return (M) enhancer.create();
    }

    private static <M> M castTo(Class<M> targetType, String value)
    {
        if (targetType == Integer.class || targetType == int.class) {
            return (M) Integer.valueOf(Integer.parseInt(value));
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return targetType.cast(Boolean.parseBoolean(value));
        } else if (targetType == String.class) {
            return targetType.cast(value);
        } else if (targetType.isEnum()) {
            return (M) Enum.valueOf(targetType.asSubclass(Enum.class), value);
        } else {
            throw new IllegalArgumentException("Invalid type: " + targetType + " for value " + value);
        }
    }

    private static class LazyCallbackFilter implements CallbackFilter
    {
        public static final LazyCallbackFilter INSTANCE = new LazyCallbackFilter();
        public int accept(Method method)
        {
            if (method.getAnnotation(Lazy.class) != null)
            {
                return 1;
            } else if (method.getName().equals("getSource")) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    private static class LazyMethodInterceptor implements MethodInterceptor
    {
        private final Map<Method,Object> cachedValues;

        public LazyMethodInterceptor(final String data)
        {
            cachedValues = new MapMaker().makeComputingMap(new Function<Method,Object>() {

                public Object apply(Method method)
                {
                    Lazy lazyAnno = method.getAnnotation(Lazy.class);
                    if ("".equals(lazyAnno.matchFirst())) {
                        return UNKNOWN;
                    } else {
                        Pattern pattern = Pattern.compile(lazyAnno.matchFirst());
                        Matcher m = pattern.matcher(data);
                        if (m.find())
                        {
                            return castTo(method.getReturnType(), m.group(1));
                        } else {
                            throw new IllegalArgumentException("Cannot find pattern: " + lazyAnno.matchFirst()
                                + " in data " + data);
                        }
                    }
                }
            });
        }

        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable
        {
            Object val = cachedValues.get(method);
            if (val == UNKNOWN) {
                val = methodProxy.invokeSuper(o, objects);
                cachedValues.put(method, val);
            }
            return val;
        }
    }
}
