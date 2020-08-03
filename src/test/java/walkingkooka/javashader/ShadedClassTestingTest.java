/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.javashader;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.map.Maps;
import walkingkooka.predicate.Predicates;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.CharSequences;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ShadedClassTestingTest implements ClassTesting<ShadedClassTesting> {

    // Class......................................................................................................

    @Test
    public void testClassVisibilityPublic() {
        classTesting(TestPublicClass.class, TestPublicClass2.class)
                .testClassVisibility();
    }

    @Test
    public void testClassVisibilityPublic2() {
        classTesting(TestPublicClass.class, TestPublicClass.class)
                .testClassVisibility();
    }

    @Test
    public void testClassVisibilityPackagePrivate() {
        classTesting(TestPackagePrivateClass.class, TestPackagePrivateClass.class)
                .testClassVisibility();
    }

    @Test
    public void testClassDifferentVisibilityFails() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPackagePrivateClass.class, TestPublicClass.class)
                    .testClassVisibility();
        });
    }

    static class TestPackagePrivateClass {
    }

    public class TestPublicClass {
    }

    public class TestPublicClass2 {
    }

    public static class TestPublicStaticClass {
    }

    @Test
    public void testClassDifferentStaticFails() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicStaticClass.class, TestPublicClass.class)
                    .testClassStatic();
        });
    }

    @Test
    public void testClassDifferentStaticFails2() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicClass.class, TestPublicStaticClass.class)
                    .testClassStatic();
        });
    }

    @Test
    public void testClassDifferentFinalFails() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicStaticClass.class, TestPublicFinalClass.class)
                    .testClassFinal();
        });
    }

    @Test
    public void testClassDifferentFinalFails2() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicFinalClass.class, TestPublicStaticClass.class)
                    .testClassFinal();
        });
    }

    public final class TestPublicFinalClass {
    }

    @Test
    public void testClassDifferentAbstractFails() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicStaticClass.class, TestPublicAbstractClass.class)
                    .testClassAbstract();
        });
    }

    @Test
    public void testClassDifferentAbstractFails2() {
        assertThrows(AssertionError.class, () -> {
            classTesting(TestPublicAbstractClass.class, TestPublicStaticClass.class)
                    .testClassAbstract();
        });
    }

    public abstract class TestPublicAbstractClass {
    }

    private static <T> ShadedClassTesting<T> classTesting(final Class<T> from,
                                                          final Class<?> to) {
        return new FakeShadedClassTesting<T>() {

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(from, to));
            }

            @Override
            public Class<T> type() {
                return from;
            }
        };
    }

    // Constructor......................................................................................................

    @Test
    public void TestConstructorFiltered() {
        new FakeShadedClassTesting<TestPublicConstructor>() {
            @Override
            public Predicate<Constructor> requiredConstructors() {
                return (c) -> c.getParameterTypes().length == -1;
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestPublicConstructor.class, TestConstructorString.class));
            }

            @Override
            public Class<TestPublicConstructor> type() {
                return TestPublicConstructor.class;
            }
        }.testConstructors();
    }

    @Test
    public void testConstructorPublicPublic() {
        constructorTesting(TestPublicConstructor.class, TestPublicConstructor.class)
                .testConstructors();
    }

    @Test
    protected void testConstructorPublicProtectedFails() throws Exception {
        this.constructorVisibilityFails(TestPublicConstructor.class, TestProtectedConstructor.class);
    }

    @Test
    protected void testConstructorProtectedProtected() {
        constructorTesting(TestProtectedConstructor.class, TestProtectedConstructor.class)
                .testConstructors();
    }

    @Test
    protected void testConstructorPackagePrivatePackagePrivate() {
        constructorTesting(TestPackagePrivateConstructor.class, TestPackagePrivateConstructor.class)
                .testConstructors();
    }

    @Test
    public void testConstructorPublicProtectedVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestPublicConstructor.class, TestProtectedConstructor.class);
    }

    @Test
    public void testConstructorProtectedPublicVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestProtectedConstructor.class, TestPublicConstructor.class);
    }

    @Test
    public void testConstructorPublicPackagePrivateVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestPublicConstructor.class, TestPackagePrivateConstructor.class);
    }

    @Test
    public void testConstructorPackagePrivatePublicVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestPackagePrivateConstructor.class, TestPublicConstructor.class);
    }

    @Test
    public void testConstructorProtectedPackagePrivateVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestProtectedConstructor.class, TestPackagePrivateConstructor.class);
    }

    @Test
    public void testConstructorPackagePrivateProtectedVisibilityFails() throws Exception {
        this.constructorVisibilityFails(TestPackagePrivateConstructor.class, TestProtectedConstructor.class);
    }

    private void constructorVisibilityFails(final Class<?> from,
                                            final Class<?> to) throws Exception {
        final Constructor expected = to.getDeclaredConstructor();
        constructorTestingFails(from,
                to,
                "expected:<[]> but was:<[Constructor visibility " + JavaVisibility.of(expected) + " different: " + expected.toGenericString() + "]>");
    }

    static class TestPublicConstructor {
        public TestPublicConstructor() {
            super();
        }
    }

    static class TestProtectedConstructor {
        protected TestProtectedConstructor() {
            super();
        }
    }

    static class TestPackagePrivateConstructor {
        TestPackagePrivateConstructor() {
            super();
        }
    }

    @Test
    public void testConstructorMissingFails() {
        constructorTestingFails(TestConstructorString.class,
                TestConstructorInt.class,
                "expected:<[]> but was:<[Constructor missing from target: walkingkooka.javashader.ShadedClassTestingTest$TestConstructorString(java.lang.String)]>");
    }

    @Test
    public void TestConstructorString() {
        constructorTesting(TestConstructorString.class, TestConstructorString2.class)
                .testConstructors();
    }

    static class TestConstructorString2 {
        TestConstructorString2(final String string) {
            super();
        }
    }

    @Test
    public void TestConstructorParameterMapped() {
        new FakeShadedClassTesting<TestConstructorFrom>() {
            @Override
            public Predicate<Constructor> requiredConstructors() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestConstructorFrom.class, TestConstructorTo.class, TestFrom.class, TestTo.class));
            }

            @Override
            public Class<TestConstructorFrom> type() {
                return TestConstructorFrom.class;
            }
        }.testConstructors();
    }

    static class TestConstructorFrom {
        TestConstructorFrom(final TestFrom ignore) {
            super();
        }
    }

    static class TestConstructorTo {
        TestConstructorTo(final TestTo ignore) {
            super();
        }
    }

    static class TestConstructorString {
        TestConstructorString(final String string) {
            super();
        }
    }

    static class TestConstructorInt {
        TestConstructorInt(final int ignored) {
            super();
        }
    }

    @Test
    public void testConstructorThrows() {
        constructorTesting(TestConstructorThrowsIllegalArgumentException.class, TestConstructorThrowsIllegalArgumentException.class)
                .testConstructors();
    }

    @Test
    public void testConstructorThrows2() {
        constructorTesting(TestConstructorThrowsIllegalStateException.class, TestConstructorThrowsIllegalStateException.class)
                .testConstructors();
    }

    @Test
    public void testConstructorDifferentThrowsFails() {
        constructorTestingFails(TestConstructorThrowsIllegalArgumentException.class,
                TestConstructorThrowsIllegalStateException.class,
                "expected:<[]> but was:<[Constructor includes unexpected throws: walkingkooka.javashader.ShadedClassTestingTest$TestConstructorThrowsIllegalStateException() throws java.lang.IllegalStateException]>");
    }

    @Test
    public void testConstructorDifferentThrowsFails2() {
        constructorTestingFails(TestConstructorThrowsIllegalArgumentExceptionIllegalStateException.class,
                TestConstructorThrowsIllegalArgumentException.class,
                "expected:<[]> but was:<[Constructor includes unexpected throws: walkingkooka.javashader.ShadedClassTestingTest$TestConstructorThrowsIllegalArgumentException() throws java.lang.IllegalArgumentException]>");
    }

    static class TestConstructorThrowsIllegalArgumentException {
        TestConstructorThrowsIllegalArgumentException() throws IllegalArgumentException {
        }
    }

    static class TestConstructorThrowsIllegalStateException {
        TestConstructorThrowsIllegalStateException() throws IllegalStateException {
        }
    }

    static class TestConstructorThrowsIllegalArgumentExceptionIllegalStateException {
        TestConstructorThrowsIllegalArgumentExceptionIllegalStateException() throws IllegalArgumentException, IllegalStateException {
        }
    }

    private <T> void constructorTestingFails(final Class<T> from,
                                             final Class<?> to,
                                             final String expectedMessage) {
        final Throwable thrown = assertThrows(AssertionError.class,
                () -> {
                    constructorTesting(from, to)
                            .testConstructors();
                });
        assertEquals(expectedMessage, thrown.getMessage());
    }

    private static <T> ShadedClassTesting<T> constructorTesting(final Class<T> from,
                                                                final Class<?> to) {
        return new FakeShadedClassTesting<T>() {
            @Override
            public Predicate<Constructor> requiredConstructors() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(from, to));
            }

            @Override
            public Class<T> type() {
                return from;
            }
        };
    }

    // Method......................................................................................................

    @Test
    public void TestMethodFiltered() {
        new FakeShadedClassTesting<TestPublicMethodFiltered>() {
            @Override
            public Predicate<Method> requiredMethods() {
                return (c) -> false == c.getName().equals("ignore");
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestPublicMethodFiltered.class, TestPublicMethod.class));
            }

            @Override
            public Class<TestPublicMethodFiltered> type() {
                return TestPublicMethodFiltered.class;
            }
        }.testMethods();
    }

    static class TestPublicMethodFiltered extends TestPublicMethod {
        void ignore() {
        }
    }

    @Test
    public void testMethodPublicPublic() {
        methodTesting(TestPublicMethod.class, TestPublicMethod.class)
                .testMethods();
    }

    @Test
    protected void testMethodPublicProtectedFails() throws Exception {
        this.methodVisibilityFails(TestPublicMethod.class, TestProtectedMethod.class);
    }

    @Test
    protected void testMethodProtectedProtected() {
        methodTesting(TestProtectedMethod.class, TestProtectedMethod.class)
                .testMethods();
    }

    @Test
    protected void testMethodPackagePrivatePackagePrivate() {
        methodTesting(TestPackagePrivateMethod.class, TestPackagePrivateMethod.class)
                .testMethods();
    }

    @Test
    public void testMethodPublicProtectedVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestPublicMethod.class, TestProtectedMethod.class);
    }

    @Test
    public void testMethodProtectedPublicVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestProtectedMethod.class, TestPublicMethod.class);
    }

    @Test
    public void testMethodPublicPackagePrivateVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestPublicMethod.class, TestPackagePrivateMethod.class);
    }

    @Test
    public void testMethodPackagePrivatePublicVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestPackagePrivateMethod.class, TestPublicMethod.class);
    }

    @Test
    public void testMethodProtectedPackagePrivateVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestProtectedMethod.class, TestPackagePrivateMethod.class);
    }

    @Test
    public void testMethodPackagePrivateProtectedVisibilityFails() throws Exception {
        this.methodVisibilityFails(TestPackagePrivateMethod.class, TestProtectedMethod.class);
    }

    private void methodVisibilityFails(final Class<?> from,
                                       final Class<?> to) throws Exception {
        final Method expected = to.getDeclaredMethod("method123");

        methodTestingFails(from,
                to,
                "expected:<[]> but was:<[Method visibility " + JavaVisibility.of(expected) + " different: " + expected.toGenericString() + "]>");
    }

    static class TestPublicMethod {
        public void method123() {
        }
    }

    static class TestProtectedMethod {
        protected void method123() {
        }
    }

    static class TestPackagePrivateMethod {
        void method123() {
        }
    }

    @Test
    public void testMethodMissingFails() {
        methodTestingFails(TestMethodParameterString.class,
                TestMethodParameterInt.class,
                "expected:<[]> but was:<[Method missing from target: void walkingkooka.javashader.ShadedClassTestingTest$TestMethodParameterString.method123(java.lang.String)]>");
    }

    @Test
    public void TestMethodString() {
        methodTesting(TestMethodParameterString.class, TestMethodString2.class)
                .testMethods();
    }

    static class TestMethodString2 {
        void method123(final String string) {
        }
    }

    @Test
    public void TestMethodParameterMapped() {
        new FakeShadedClassTesting<TestMethodParameterFrom>() {
            @Override
            public Predicate<Method> requiredMethods() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestMethodParameterFrom.class, TestMethodParameterTo.class, TestFrom.class, TestTo.class));
            }

            @Override
            public Class<TestMethodParameterFrom> type() {
                return TestMethodParameterFrom.class;
            }
        }.testMethods();
    }

    static class TestMethodParameterFrom {
        void method123(final TestFrom ignore) {
        }
    }

    static class TestMethodParameterTo {
        void method123(final TestTo ignore) {
        }
    }

    static class TestMethodParameterString {
        void method123(final String string) {
        }
    }

    static class TestMethodParameterInt {
        void method123(final int ignored) {
        }
    }

    @Test
    public void testMethodThrows() {
        methodTesting(TestMethodThrowsIllegalArgumentException.class, TestMethodThrowsIllegalArgumentException.class)
                .testMethods();
    }

    @Test
    public void testMethodThrows2() {
        methodTesting(TestMethodThrowsIllegalStateException.class, TestMethodThrowsIllegalStateException.class)
                .testMethods();
    }

    @Test
    public void testMethodDifferentThrowsFails() {
        methodTestingFails(TestMethodThrowsIllegalArgumentException.class,
                TestMethodThrowsIllegalStateException.class,
                "expected:<[]> but was:<[Method includes unexpected throws(java.lang.IllegalArgumentException): void walkingkooka.javashader.ShadedClassTestingTest$TestMethodThrowsIllegalArgumentException.method123() throws java.lang.IllegalArgumentException]>");
    }

    @Test
    public void testMethodDifferentThrowsFails2() {
        methodTestingFails(TestMethodThrowsIllegalArgumentExceptionIllegalStateException.class,
                TestMethodThrowsIllegalArgumentException.class,
                "expected:<[]> but was:<[Method includes unexpected throws(java.lang.IllegalStateException): void walkingkooka.javashader.ShadedClassTestingTest$TestMethodThrowsIllegalArgumentExceptionIllegalStateException.method123() throws java.lang.IllegalArgumentException,java.lang.IllegalStateException]>");
    }

    static class TestMethodThrowsIllegalArgumentException {
        void method123() throws IllegalArgumentException {
        }
    }

    static class TestMethodThrowsIllegalStateException {
        void method123() throws IllegalStateException {
        }
    }

    static class TestMethodThrowsIllegalArgumentExceptionIllegalStateException {
        void method123() throws IllegalArgumentException, IllegalStateException {
        }
    }

    @Test
    public void testMethodDifferentReturnTypeFails() {
        methodTestingFails(TestMethodReturnString.class,
                TestMethodReturnInt.class,
                "expected:<[]> but was:<[Method return type java.lang.String different: java.lang.String walkingkooka.javashader.ShadedClassTestingTest$TestMethodReturnString.method123()]>");
    }

    static class TestMethodReturnString {
        String method123() {
            return null;
        }
    }

    static class TestMethodReturnInt {
        int method123() {
            return 0;
        }
    }

    @Test
    public void testMethodNotStaticStaticFails() {
        methodTestingFails(TestMethodNotStatic.class,
                TestMethodStatic.class,
                "expected:<[]> but was:<[Static expected instance: void walkingkooka.javashader.ShadedClassTestingTest$TestMethodNotStatic.method123()]>");
    }

    @Test
    public void testMethodStaticNotStaticFails() {
        methodTestingFails(TestMethodStatic.class,
                TestMethodNotStatic.class,
                "expected:<[]> but was:<[Instance expected static: static void walkingkooka.javashader.ShadedClassTestingTest$TestMethodStatic.method123()]>");
    }

    static class TestMethodStatic {
        static void method123() {
        }
    }

    static class TestMethodNotStatic {
        void method123() {
        }
    }

    @Test
    public void testMethodNotAbstractAbstractFails() {
        methodTestingFails(TestMethodNotAbstract.class,
                TestMethodAbstract.class,
                "expected:<[]> but was:<[Abstract expected non abstract: void walkingkooka.javashader.ShadedClassTestingTest$TestMethodNotAbstract.method123()]>");
    }

    @Test
    public void testMethodAbstractNotAbstractFails() {
        methodTestingFails(TestMethodAbstract.class,
                TestMethodNotAbstract.class,
                "expected:<[]> but was:<[Non abstract expected abstract: abstract void walkingkooka.javashader.ShadedClassTestingTest$TestMethodAbstract.method123()]>");
    }

    static abstract class TestMethodAbstract {
        abstract void method123();
    }

    static abstract class TestMethodNotAbstract {
        void method123() {
        }
    }

    @Test
    public void testMethodNotFinalFinalFails() {
        methodTestingFails(TestMethodNotFinal.class,
                TestMethodFinal.class,
                "expected:<[]> but was:<[Final expected non final: void walkingkooka.javashader.ShadedClassTestingTest$TestMethodNotFinal.method123()]>");
    }

    @Test
    public void testMethodFinalNotFinalFails() {
        methodTestingFails(TestMethodFinal.class,
                TestMethodNotFinal.class,
                "expected:<[]> but was:<[Non final expected final: final void walkingkooka.javashader.ShadedClassTestingTest$TestMethodFinal.method123()]>");
    }

    static class TestMethodFinal {
        final void method123() {
        }
    }

    static abstract class TestMethodNotFinal {
        void method123() {
        }
    }

    private <T> void methodTestingFails(final Class<T> from,
                                        final Class<?> to,
                                        final String expectedMessage) {
        final Throwable thrown = assertThrows(AssertionError.class,
                () -> {
                    methodTesting(from, to)
                            .testMethods();
                });
        assertEquals(expectedMessage, thrown.getMessage());
    }

    private static <T> ShadedClassTesting<T> methodTesting(final Class<T> from,
                                                           final Class<?> to) {
        return new FakeShadedClassTesting<T>() {
            @Override
            public Predicate<Method> requiredMethods() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(from, to));
            }

            @Override
            public Class<T> type() {
                return from;
            }
        };
    }

    // Field...........................................................................................................

    @Test
    public void TestFieldFiltered() throws Exception {
        new FakeShadedClassTesting<TestPublicFieldFiltered>() {
            @Override
            public Predicate<Field> requiredFields() {
                return (c) -> false == c.getName().equals("ignore");
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestPublicFieldFiltered.class, TestPublicField.class));
            }

            @Override
            public Class<TestPublicFieldFiltered> type() {
                return TestPublicFieldFiltered.class;
            }
        }.testFields();
    }

    static class TestPublicFieldFiltered extends TestPublicField {
        int ignore = 0;
    }

    @Test
    public void testFieldPublicPublic() throws Exception {
        fieldTesting(TestPublicField.class, TestPublicField.class)
                .testFields();
    }

    @Test
    protected void testFieldPublicProtectedFails() throws Exception {
        this.fieldVisibilityFails(TestPublicField.class, TestProtectedField.class);
    }

    @Test
    protected void testFieldProtectedProtected() throws Exception {
        fieldTesting(TestProtectedField.class, TestProtectedField.class)
                .testFields();
    }

    @Test
    protected void testFieldPackagePrivatePackagePrivate() throws Exception {
        fieldTesting(TestPackagePrivateField.class, TestPackagePrivateField.class)
                .testFields();
    }

    @Test
    public void testFieldPublicProtectedVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestPublicField.class, TestProtectedField.class);
    }

    @Test
    public void testFieldProtectedPublicVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestProtectedField.class, TestPublicField.class);
    }

    @Test
    public void testFieldPublicPackagePrivateVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestPublicField.class, TestPackagePrivateField.class);
    }

    @Test
    public void testFieldPackagePrivatePublicVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestPackagePrivateField.class, TestPublicField.class);
    }

    @Test
    public void testFieldProtectedPackagePrivateVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestProtectedField.class, TestPackagePrivateField.class);
    }

    @Test
    public void testFieldPackagePrivateProtectedVisibilityFails() throws Exception {
        this.fieldVisibilityFails(TestPackagePrivateField.class, TestProtectedField.class);
    }

    private void fieldVisibilityFails(final Class<?> from,
                                      final Class<?> to) throws Exception {
        final Field expected = to.getDeclaredField("field123");

        fieldTestingFails(from,
                to,
                "expected:<[]> but was:<[Field visibility " + JavaVisibility.of(expected) + " different: " + expected.toGenericString() + "]>");
    }

    static class TestPublicField {
        public final int field123 = 0;
    }

    static class TestProtectedField {
        protected final int field123 = 0;
    }

    static class TestPackagePrivateField {
        private final int field123 = 0;
    }

    @Test
    public void testFieldMissingFails() {
        fieldTestingFails(TestFieldString.class,
                TestFieldMissing.class,
                "expected:<[]> but was:<[Field missing from target: java.lang.String walkingkooka.javashader.ShadedClassTestingTest$TestFieldString.field123]>");
    }

    static class TestFieldMissing {
    }

    @Test
    public void TestFieldString() throws Exception {
        fieldTesting(TestFieldString.class, TestFieldString2.class)
                .testFields();
    }

    @Test
    public void testFieldDifferentTypeFails() {
        fieldTestingFails(TestFieldString.class,
                TestFieldInt.class,
                "expected:<[]> but was:<[Field type java.lang.String different: java.lang.String walkingkooka.javashader.ShadedClassTestingTest$TestFieldString.field123]>");
    }

    static class TestFieldString {
        String field123 = null;
    }

    static class TestFieldInt {
        int field123 = 0;
    }

    static class TestFieldString2 {
        String field123 = null;
    }

    @Test
    public void TestFieldParameterMapped() throws Exception {
        new FakeShadedClassTesting<TestFieldParameterFrom>() {
            @Override
            public Predicate<Field> requiredFields() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(TestFieldParameterFrom.class, TestFieldParameterTo.class, TestFrom.class, TestTo.class));
            }

            @Override
            public Class<TestFieldParameterFrom> type() {
                return TestFieldParameterFrom.class;
            }
        }.testFields();
    }

    static class TestFieldParameterFrom {
        TestFrom field123;
    }

    static class TestFieldParameterTo {
        TestTo field123;
    }

    @Test
    public void testFieldNotStaticStaticFails() {
        fieldTestingFails(TestFieldNotStatic.class,
                TestFieldStatic.class,
                "expected:<[]> but was:<[Static expected instance: int walkingkooka.javashader.ShadedClassTestingTest$TestFieldNotStatic.field123]>");
    }

    @Test
    public void testFieldStaticNotStaticFails() {
        fieldTestingFails(TestFieldStatic.class,
                TestFieldNotStatic.class,
                "expected:<[]> but was:<[Instance expected static: static int walkingkooka.javashader.ShadedClassTestingTest$TestFieldStatic.field123]>");
    }

    static class TestFieldStatic {
        static int field123;
    }

    static class TestFieldNotStatic {
        int field123;
    }

    @Test
    public void testFieldNotFinalFinalFails() {
        fieldTestingFails(TestFieldNotFinal.class,
                TestFieldFinal.class,
                "expected:<[]> but was:<[Final expected non final: int walkingkooka.javashader.ShadedClassTestingTest$TestFieldNotFinal.field123]>");
    }

    @Test
    public void testFieldFinalNotFinalFails() {
        fieldTestingFails(TestFieldFinal.class,
                TestFieldNotFinal.class,
                "expected:<[]> but was:<[Non final expected final: final int walkingkooka.javashader.ShadedClassTestingTest$TestFieldFinal.field123]>");
    }

    static class TestFieldFinal {
        final int field123 = 0;
    }

    static abstract class TestFieldNotFinal {
        int field123;
    }

    @Test
    public void testFieldBooleanConstant() {
        fieldTesting(TestFieldBooleanConstant.class,
                TestFieldBooleanConstant.class);
    }

    @Test
    public void testFieldBooleanConstant2() {
        fieldTesting(TestFieldBooleanConstant2.class,
                TestFieldBooleanConstant2.class);
    }

    @Test
    public void testFieldBooleanConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldBooleanConstant.class,
                TestFieldBooleanConstant2.class);
    }

    @Test
    public void testFieldBooleanConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldBooleanConstant2.class,
                TestFieldBooleanConstant.class);
    }

    static class TestFieldBooleanConstant {
        final static boolean field123 = false;
    }

    static class TestFieldBooleanConstant2 {
        final static boolean field123 = true;
    }

    @Test
    public void testFieldByteConstant() {
        fieldTesting(TestFieldByteConstant.class,
                TestFieldByteConstant.class);
    }

    @Test
    public void testFieldByteConstant2() {
        fieldTesting(TestFieldByteConstant2.class,
                TestFieldByteConstant2.class);
    }

    @Test
    public void testFieldByteConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldByteConstant.class,
                TestFieldByteConstant2.class);
    }

    @Test
    public void testFieldByteConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldByteConstant2.class,
                TestFieldByteConstant.class);
    }

    static class TestFieldByteConstant {
        final static byte field123 = 1;
    }

    static class TestFieldByteConstant2 {
        final static byte field123 = 23;
    }

    @Test
    public void testFieldCharConstant() {
        fieldTesting(TestFieldCharConstant.class,
                TestFieldCharConstant.class);
    }

    @Test
    public void testFieldCharConstant2() {
        fieldTesting(TestFieldCharConstant2.class,
                TestFieldCharConstant2.class);
    }

    @Test
    public void testFieldCharConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldCharConstant.class,
                TestFieldCharConstant2.class);
    }

    @Test
    public void testFieldCharConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldCharConstant2.class,
                TestFieldCharConstant.class);
    }

    static class TestFieldCharConstant {
        final static char field123 = 'A';
    }

    static class TestFieldCharConstant2 {
        final static char field123 = 'Z';
    }

    @Test
    public void testFieldIntConstant() {
        fieldTesting(TestFieldIntConstant.class,
                TestFieldIntConstant.class);
    }

    @Test
    public void testFieldIntConstant2() {
        fieldTesting(TestFieldIntConstant2.class,
                TestFieldIntConstant2.class);
    }

    @Test
    public void testFieldIntConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldIntConstant.class,
                TestFieldIntConstant2.class);
    }

    @Test
    public void testFieldIntConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldIntConstant2.class,
                TestFieldIntConstant.class);
    }

    @Test
    public void testFieldDoubleConstant() {
        fieldTesting(TestFieldDoubleConstant.class,
                TestFieldDoubleConstant.class);
    }

    @Test
    public void testFieldDoubleConstant2() {
        fieldTesting(TestFieldDoubleConstant2.class,
                TestFieldDoubleConstant2.class);
    }

    @Test
    public void testFieldDoubleConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldDoubleConstant.class,
                TestFieldDoubleConstant2.class);
    }

    @Test
    public void testFieldDoubleConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldDoubleConstant2.class,
                TestFieldDoubleConstant.class);
    }

    static class TestFieldDoubleConstant {
        final static double field123 = 1.5;
    }

    static class TestFieldDoubleConstant2 {
        final static double field123 = 23.5;
    }

    @Test
    public void testFieldFloatConstant() {
        fieldTesting(TestFieldFloatConstant.class,
                TestFieldFloatConstant.class);
    }

    @Test
    public void testFieldFloatConstant2() {
        fieldTesting(TestFieldFloatConstant2.class,
                TestFieldFloatConstant2.class);
    }

    @Test
    public void testFieldFloatConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldFloatConstant.class,
                TestFieldFloatConstant2.class);
    }

    @Test
    public void testFieldFloatConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldFloatConstant2.class,
                TestFieldFloatConstant.class);
    }

    static class TestFieldFloatConstant {
        final static float field123 = 1.5f;
    }

    static class TestFieldFloatConstant2 {
        final static float field123 = 23.5f;
    }


    static class TestFieldIntConstant {
        final static int field123 = 1;
    }

    static class TestFieldIntConstant2 {
        final static int field123 = 23;
    }

    @Test
    public void testFieldLongConstant() {
        fieldTesting(TestFieldLongConstant.class,
                TestFieldLongConstant.class);
    }

    @Test
    public void testFieldLongConstant2() {
        fieldTesting(TestFieldLongConstant2.class,
                TestFieldLongConstant2.class);
    }

    @Test
    public void testFieldLongConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldLongConstant.class,
                TestFieldLongConstant2.class);
    }

    @Test
    public void testFieldLongConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldLongConstant2.class,
                TestFieldLongConstant.class);
    }

    static class TestFieldLongConstant {
        final static long field123 = 1;
    }

    static class TestFieldLongConstant2 {
        final static long field123 = 23;
    }

    @Test
    public void testFieldShortConstant() {
        fieldTesting(TestFieldShortConstant.class,
                TestFieldShortConstant.class);
    }

    @Test
    public void testFieldShortConstant2() {
        fieldTesting(TestFieldShortConstant2.class,
                TestFieldShortConstant2.class);
    }

    @Test
    public void testFieldShortConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldShortConstant.class,
                TestFieldShortConstant2.class);
    }

    @Test
    public void testFieldShortConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldShortConstant2.class,
                TestFieldShortConstant.class);
    }

    static class TestFieldShortConstant {
        final static short field123 = 1;
    }

    static class TestFieldShortConstant2 {
        final static short field123 = 23;
    }

    @Test
    public void testFieldStringConstant() {
        fieldTesting(TestFieldStringConstant.class,
                TestFieldStringConstant.class);
    }

    @Test
    public void testFieldStringConstant2() {
        fieldTesting(TestFieldStringConstant2.class,
                TestFieldStringConstant2.class);
    }

    @Test
    public void testFieldStringConstantDifferentFails() throws Exception {
        fieldTestingValueFails(TestFieldStringConstant.class,
                TestFieldStringConstant2.class);
    }

    @Test
    public void testFieldStringConstantDifferentFails2() throws Exception {
        fieldTestingValueFails(TestFieldStringConstant2.class,
                TestFieldStringConstant.class);
    }

    static class TestFieldStringConstant {
        final static String field123 = "abc";
    }

    static class TestFieldStringConstant2 {
        final static String field123 = "different";
    }

    private <T> void fieldTestingValueFails(final Class<T> from,
                                            final Class<?> to) throws Exception {
        final Field fromField = from.getDeclaredField("field123");
        fromField.setAccessible(true);
        final Object fromValue = fromField.get(null);

        final Field toField = to.getDeclaredField("field123");
        toField.setAccessible(true);
        final Object toValue = toField.get(null);

        fieldTestingFails(from,
                to,
                "expected:<[]> but was:<[Field value " + CharSequences.quoteIfChars(fromValue) + " different " + CharSequences.quoteIfChars(toValue) + ": " + fromField.toGenericString() + "]>");
    }

    @Test
    public void testFieldObjectConstantDifferent() {
        fieldTesting(TestFieldObjectConstant2.class,
                TestFieldObjectConstant.class);
    }

    static class TestFieldObjectConstant {
        final static Object field123 = "abc";
    }

    static class TestFieldObjectConstant2 {
        final static Object field123 = "different";
    }

    private <T> void fieldTestingFails(final Class<T> from,
                                       final Class<?> to,
                                       final String expectedMessage) {
        final Throwable thrown = assertThrows(AssertionError.class,
                () -> {
                    fieldTesting(from, to)
                            .testFields();
                });
        assertEquals(expectedMessage, thrown.getMessage());
    }

    private static <T> ShadedClassTesting<T> fieldTesting(final Class<T> from,
                                                          final Class<?> to) {
        return new FakeShadedClassTesting<T>() {
            @Override
            public Predicate<Field> requiredFields() {
                return Predicates.always();
            }

            @Override
            public UnaryOperator<Class<?>> typeMapper() {
                return typeMapper0(Maps.of(from, to));
            }

            @Override
            public Class<T> type() {
                return from;
            }
        };
    }

    // helpers..........................................................................................................

    static abstract class FakeShadedClassTesting<T> implements ShadedClassTesting<T> {

        @Override
        public Predicate<Constructor> requiredConstructors() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<Method> requiredMethods() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<Field> requiredFields() {
            throw new UnsupportedOperationException();
        }
    }

    private static UnaryOperator<Class<?>> typeMapper0(final Map<Class<?>, Class<?>> mapping) {
        return (t) -> {
            final Class<?> to = mapping.get(t);
            if (null != to) {
                return to;
            }

            if (t.isPrimitive() || t.getName().startsWith("java.")) {
                return t;
            }

            throw new IllegalStateException("Type " + t.getName() + " not mapped");
        };
    }

    static class TestFrom {
    }

    static class TestTo {
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<ShadedClassTesting> type() {
        return ShadedClassTesting.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}