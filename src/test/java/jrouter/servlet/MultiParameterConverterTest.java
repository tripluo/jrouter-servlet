/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jrouter.servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jrouter.JRouterException;
import jrouter.servlet.MultiParameterConverterFactory.MultiParameterConverter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 测试MultiParameterConverter转换参数。
 */
public class MultiParameterConverterTest {

    @Test
    public void testConvert() throws Exception {

        Object obj = new Object();
        CharSequence str1 = "String_1";
        CharSequence str2 = "String_2";
        CharSequence sb1 = new StringBuilder("StringBuilder_1");
        CharSequence sb2 = new StringBuilder("StringBuilder_2");

        MultiParameterConverter c = null;
        //test()
        c = buildMultiParameterConverter(obj);
        assertArrayEquals(new Object[0], testConvertMethod(c, "test"));
        assertArrayEquals(null, testConvertMethod(c, "test", (Object[]) null));  //original params

        //test1(Object obj)
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test1"));  //match
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", str1));  //original params

        c = buildMultiParameterConverter(str1);
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1"));  //match
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test1", str1)); //original params
        assertArrayEquals(new Object[]{str2}, testConvertMethod(c, "test1", str2)); //original params

        //test2(String str)
        c = buildMultiParameterConverter(obj, sb1);
        assertArrayEquals(new Object[]{null}, testConvertMethod(c, "test2"));  //no match fill null
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test2", obj));  //original params
        c = buildMultiParameterConverter(str1);
        assertArrayEquals(new Object[]{str1}, testConvertMethod(c, "test2"));  //match
        assertArrayEquals(new Object[]{obj}, testConvertMethod(c, "test2", obj));  //original params

        //test3(Object obj, String str)
        c = buildMultiParameterConverter(obj);
        assertArrayEquals(new Object[]{obj, null}, testConvertMethod(c, "test3"));  //match & fill null
        assertArrayEquals(new Object[]{str1, null}, testConvertMethod(c, "test3", str1));  //original params & no match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", str1, str2));  //original params

        c = buildMultiParameterConverter(obj, str2);
        assertArrayEquals(new Object[]{obj, str2}, testConvertMethod(c, "test3"));  //match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", str1));  //original params & match
        assertArrayEquals(new Object[]{str1, str2}, testConvertMethod(c, "test3", str1, str2));  //original params

        //test4(String str, Object obj)
        c = buildMultiParameterConverter(obj);
        assertArrayEquals(new Object[]{null, obj}, testConvertMethod(c, "test4"));  //match & fill null
        assertArrayEquals(new Object[]{str1, obj}, testConvertMethod(c, "test4", str1));  //original params & match
        c = buildMultiParameterConverter(str1);
        assertArrayEquals(new Object[]{str1, str1}, testConvertMethod(c, "test4"));  //match
        c = buildMultiParameterConverter(str1, str2);
        assertArrayEquals(new Object[]{str1, str1}, testConvertMethod(c, "test4"));  //match
        assertArrayEquals(new Object[]{obj, sb2}, testConvertMethod(c, "test4", obj, sb2));  //original params
        assertArrayEquals(new Object[]{str2, sb2}, testConvertMethod(c, "test4", str2, sb2));  //original params

        //test5(String str, StringBuilder sb)
        c = buildMultiParameterConverter(obj);
        assertArrayEquals(new Object[]{null, null}, testConvertMethod(c, "test5"));  //no match & fill null
        c = buildMultiParameterConverter(sb1);
        assertArrayEquals(new Object[]{null, sb1}, testConvertMethod(c, "test5"));  //match & fill null
        assertArrayEquals(new Object[]{obj, sb1}, testConvertMethod(c, "test5", obj));  //original params & match

        //test6(CharSequence s1, CharSequence s2, String obj)
        c = buildMultiParameterConverter(obj);
        assertArrayEquals(new Object[]{null, null, null}, testConvertMethod(c, "test6"));  //no match & fill null
        assertArrayEquals(new Object[]{obj, null, null}, testConvertMethod(c, "test6", obj));  //original params & no match & fill null
        c = buildMultiParameterConverter(str1, str2);
        assertArrayEquals(new Object[]{str1, str1, str1}, testConvertMethod(c, "test6"));  //match
        assertArrayEquals(new Object[]{obj, str1, str1}, testConvertMethod(c, "test6", obj));  //original params & match
        c = buildMultiParameterConverter(sb1, sb2);
        assertArrayEquals(new Object[]{sb1, sb1, null}, testConvertMethod(c, "test6"));  //match & fill null
        assertArrayEquals(new Object[]{obj, sb1, null}, testConvertMethod(c, "test6", obj));  //original params & match & fill null
    }

    /**
     * 根据动态参数构建转换器。
     */
    private MultiParameterConverter buildMultiParameterConverter(Object... additionalParameters) {
        return new MultiParameterConverterFactory().new MultiParameterConverter(additionalParameters);
    }

    /**
     * @see MultiParameterConverter#convert(java.lang.reflect.Method, java.lang.Object, java.lang.Object[])
     */
    private static Object[] testConvertMethod(MultiParameterConverter converter, String method,
            Object... args) throws JRouterException {
        return converter.convert(TestAction.TEST_METHODS.get(method), null, args);
    }

    /**
     * TestAction.
     */
    private static class TestAction {

        //test methods
        static final Map<String, Method> TEST_METHODS = new HashMap<String, Method>(8);

        static {
            Method[] methods = TestAction.class.getDeclaredMethods();
            for (Method m : methods) {
                TEST_METHODS.put(m.getName(), m);
            }
            assertTrue(TEST_METHODS.size() == 7);
        }

        public void test() {
        }

        public void test1(Object obj) {
        }

        public void test2(String str) {
        }

        public void test3(Object obj, String str) {
        }

        public void test4(String str, Object obj) {
        }

        public void test5(String str, StringBuilder sb) {
        }

        public void test6(CharSequence s1, CharSequence s2, String obj) {
        }

    }
}
