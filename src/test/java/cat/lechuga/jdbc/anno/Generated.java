package cat.lechuga.jdbc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cat.lechuga.jdbc.generator.Generator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Generated {
    Class<? extends Generator> value();

    String[] args() default {};
}