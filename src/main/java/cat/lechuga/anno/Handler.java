package cat.lechuga.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cat.lechuga.handler.ColumnHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Handler {

    Class<? extends ColumnHandler> value();

    String[] args() default {};
}
