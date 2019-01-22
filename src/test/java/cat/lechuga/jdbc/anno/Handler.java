package cat.lechuga.jdbc.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lenteja.mapper.handler.ColumnHandler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Handler {

    Class<ColumnHandler> value();

    String[] args() default {};
}
