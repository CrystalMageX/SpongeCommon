/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.CauseTracked;
import org.spongepowered.api.event.filter.CauseFilters.All;
import org.spongepowered.api.event.filter.CauseFilters.First;
import org.spongepowered.api.event.filter.CauseFilters.Last;
import org.spongepowered.api.event.filter.EventTypeFilters.Exclude;
import org.spongepowered.api.event.filter.EventTypeFilters.Include;
import org.spongepowered.api.event.filter.EventTypeFilters.IsCancelled;
import org.spongepowered.common.event.gen.DefineableClassLoader;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class FilterFactory {

    private final AtomicInteger id = new AtomicInteger();
    private final DefineableClassLoader classLoader = new DefineableClassLoader(getClass().getClassLoader());
    private final LoadingCache<Method, Class<? extends EventFilter>> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1).weakValues().build(new CacheLoader<Method, Class<? extends EventFilter>>() {

                @Override
                public Class<? extends EventFilter> load(Method method) throws Exception {
                    return createClass(method);
                }
            });
    private final String targetPackage;

    public FilterFactory(String targetPackage) {
        checkNotNull(targetPackage, "targetPackage");
        checkArgument(!targetPackage.isEmpty(), "targetPackage cannot be empty");
        this.targetPackage = targetPackage + '.';
    }

    public EventFilter createFilter(Method method) throws Exception {
        if (method.getParameterCount() == 1 && method.getParameters()[0].getAnnotations().length == 0) {
            return null;
        }
        return this.cache.get(method).newInstance();
    }

    private Class<? extends EventFilter> createClass(Method method) {
        Class<?> handle = method.getDeclaringClass();
        Class<?> eventClass = method.getParameterTypes()[0];
        String name = this.targetPackage + eventClass.getSimpleName() + "Filter_" + handle.getSimpleName() + '_'
                + method.getName() + this.id.incrementAndGet();

        return this.classLoader.defineClass(name, generateClass(name, method, eventClass));
    }

    private static byte[] generateClass(String name, Method method, Class<?> eventClass) {
        name = name.replace('.', '/');
        Parameter[] params = method.getParameters();
        Parameter eventParam = params[0];

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        FieldVisitor fv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, "java/lang/Object",
                new String[] {"org/spongepowered/common/event/filter/EventFilter"});

        if (eventParam.isAnnotationPresent(Include.class) && eventParam.isAnnotationPresent(Exclude.class)) {
            throw new IllegalArgumentException("Cannot have both @Include and @Exclude annotations present at once");
        }

        if (eventParam.isAnnotationPresent(Include.class) || eventParam.isAnnotationPresent(Exclude.class)) {
            fv = cw.visitField(0, "classes", "Ljava/util/Set;", "Ljava/util/Set<Ljava/lang/Class<*>;>;", null);
            fv.visitEnd();

            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            // Initialize the class set
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/collect/Sets", "newHashSet", "()Ljava/util/HashSet;",
                    false);
            mv.visitFieldInsn(PUTFIELD, name, "classes", "Ljava/util/Set;");

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "classes", "Ljava/util/Set;");

            if (eventParam.isAnnotationPresent(Include.class)) {
                Include inc = eventParam.getAnnotation(Include.class);
                for (Class<?> cls : inc.value()) {
                    // dup the field
                    mv.visitInsn(DUP);
                    // ldc the type
                    mv.visitLdcInsn(Type.getType(cls));
                    // add it to the set
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                    // pop the boolean result leaving just the original field
                    mv.visitInsn(POP);
                }
                // pop the original field ref
                mv.visitInsn(POP);
            } else {
                Exclude inc = eventParam.getAnnotation(Exclude.class);
                for (Class<?> cls : inc.value()) {
                    // dup the field
                    mv.visitInsn(DUP);
                    // ldc the type
                    mv.visitLdcInsn(Type.getType(cls));
                    // add it to the set
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "add", "(Ljava/lang/Object;)Z", true);
                    // pop the boolean result leaving just the original field
                    mv.visitInsn(POP);
                }
                // pop the original field ref
                mv.visitInsn(POP);
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        } else {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "filter", "(Lorg/spongepowered/api/event/Event;)[Ljava/lang/Object;", null,
                    null);
            mv.visitCode();
            // index of the next available local variable
            int local = 2;
            if (eventParam.isAnnotationPresent(IsCancelled.class)) {
                if (!Cancellable.class.isAssignableFrom(eventClass)) {
                    throw new IllegalStateException(
                            "Attempted to filter a non-cancellable event type by its cancellation status");
                }
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, "org/spongepowered/api/event/Cancellable");
                mv.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/api/event/Cancellable", "isCancelled", "()Z",
                        true);
                Label success = new Label();
                mv.visitJumpInsn(IFNE, success);
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
                mv.visitLabel(success);
            }
            if (eventParam.isAnnotationPresent(Include.class) || eventParam.isAnnotationPresent(Exclude.class)) {

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, name, "classes", "Ljava/util/Set;");
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
                Label success = new Label();
                if (eventParam.isAnnotationPresent(Include.class)) {
                    mv.visitJumpInsn(IFEQ, success);
                } else {
                    mv.visitJumpInsn(IFNE, success);
                }
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
                mv.visitLabel(success);
            }
            // local var indices of the parameters values
            int[] plocals = new int[params.length - 1];
            for (int i = 1; i < params.length; i++) {
                Parameter p = params[i];
                Source source = null;
                Class<?> targetType = p.getType();
                // Detect source type
                if (p.isAnnotationPresent(First.class)) {
                    source = Source.CAUSE_FIRST;
                }
                if (p.isAnnotationPresent(Last.class)) {
                    if (source != null) {
                        throw new IllegalStateException("Parameter " + p.getName() + " has multiple source filters");
                    }
                    source = Source.CAUSE_LAST;
                }
                if (p.isAnnotationPresent(All.class)) {
                    if (source != null) {
                        throw new IllegalStateException("Parameter " + p.getName() + " has multiple source filters");
                    }
                    source = Source.CAUSE_ALL;
                }
                if (source == null) {
                    throw new IllegalStateException("Parameter " + p.getName() + " had no source filter");
                }
                if ((source == Source.CAUSE_LAST || source == Source.CAUSE_FIRST || source == Source.CAUSE_ALL)
                        && !CauseTracked.class.isAssignableFrom(eventClass)) {
                    throw new IllegalStateException("Attempted to filter a non-causetracked event type by a cause");
                }

                if (source == Source.CAUSE_LAST || source == Source.CAUSE_FIRST || source == Source.CAUSE_ALL) {
                    // Get the cause
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitTypeInsn(CHECKCAST, "org/spongepowered/api/event/cause/CauseTracked");
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/api/event/cause/CauseTracked", "getCause",
                            "()Lorg/spongepowered/api/event/cause/Cause;", true);
                    // Fetch the object
                    if (source == Source.CAUSE_FIRST) {
                        mv.visitLdcInsn(Type.getType(targetType));
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/api/event/cause/Cause", "first",
                                "(Ljava/lang/Class;)Ljava/util/Optional;", false);
                    }
                    if (source == Source.CAUSE_LAST) {
                        mv.visitLdcInsn(Type.getType(targetType));
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/api/event/cause/Cause", "last",
                                "(Ljava/lang/Class;)Ljava/util/Optional;", false);
                    }
                    if (source == Source.CAUSE_ALL) {
                        if (targetType.isArray()) {
                            mv.visitLdcInsn(Type.getType(targetType.getComponentType()));
                        } else {
                            throw new IllegalStateException(
                                    "Parameter " + p.getName() + " is marked with @All but is not an array type");
                        }
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/spongepowered/api/event/cause/Cause", "allOf",
                                "(Ljava/lang/Class;)Ljava/util/List;", false);
                    }
                    mv.visitVarInsn(ASTORE, plocals[i - 1] = local++);
                    // Validate the object
                    if (source != Source.CAUSE_ALL || params[i].getAnnotation(All.class).ignoreEmpty()) {
                        mv.visitVarInsn(ALOAD, plocals[i - 1]);

                        Label success = new Label();
                        if (source == Source.CAUSE_LAST || source == Source.CAUSE_FIRST) {
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
                            mv.visitJumpInsn(IFNE, success);
                        }
                        if (source == Source.CAUSE_ALL) {
                            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "isEmpty", "()Z", true);
                            mv.visitJumpInsn(IFEQ, success);
                        }
                        mv.visitInsn(ACONST_NULL);
                        mv.visitInsn(ARETURN);
                        mv.visitLabel(success);
                    }
                }

                // Transform the object if needed
                if (source == Source.CAUSE_LAST || source == Source.CAUSE_FIRST) {
                    // who needs strongly typed variables anyway
                    mv.visitVarInsn(ALOAD, plocals[i - 1]);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false);
                    mv.visitVarInsn(ASTORE, plocals[i - 1]);
                }
                if (source == Source.CAUSE_ALL) {
                    mv.visitVarInsn(ALOAD, plocals[i - 1]);
                    mv.visitInsn(ICONST_0);
                    mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(targetType.getComponentType()));
                    mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", true);
                    mv.visitVarInsn(ASTORE, plocals[i - 1]);
                }
            }

            // create the return array
            if (params.length == 1) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitIntInsn(BIPUSH, params.length);
            }
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            // load the event into the array
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(AASTORE);
            // load all the params into the array
            for (int i = 1; i < params.length; i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitVarInsn(ALOAD, plocals[i - 1]);
                mv.visitInsn(AASTORE);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static enum Source {
        CAUSE_FIRST, CAUSE_LAST, CAUSE_ALL,;
    }

    @SuppressWarnings("unused")
    private static class SecondaryFilter_EventTestPlugin_unfiltered0 implements EventFilter {

        @Override
        public Object[] filter(Event event) {
            List<Player> param1 = ((CauseTracked) event).getCause().allOf(Player.class);
            if (!param1.isEmpty()) {
                return null;
            }
            return new Object[] {event, param1.toArray(new Player[0])};
        }

        public void invoke(Event e, Player[] p) {

        }

    }

}
