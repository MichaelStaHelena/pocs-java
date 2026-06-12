package com.michael.poc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Custom annotation lida em runtime via reflection: o básico, um mini mapper estilo ORM.
 * https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/annotation/package-summary.html
 * -definir: @interface com @Retention(RUNTIME), senão a reflection não enxerga, e @Target pra onde aplica.
 * -elementos: table() obrigatório; name() e nullable() com default.
 * -ler: isAnnotationPresent + getAnnotation no Class e em cada Field, depois chamar os elementos como métodos.
 * -a anotação é só dado; quem age é este main varrendo a classe via reflection.
 */
public class Main {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Entity {
        String table();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Column {
        String name() default ""; // vazio cai no nome do campo
        boolean nullable() default true;
    }

    @Entity(table = "users")
    static class User {
        @Column(name = "user_id", nullable = false)
        long id;

        @Column(name = "email")
        String email;

        String note; // Campo sem a anotação é ignorado
    }

    public static void main(String[] args) {
        Class<?> type = User.class;

        Entity entity = type.getAnnotation(Entity.class);
        System.out.println("entity " + type.getSimpleName() + " -> table '" + entity.table() + "'");

        System.out.println("columns:");
        for (Field field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Column.class)) {
                System.out.println("  " + field.getName() + " (not mapped)");
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            String name = column.name().isEmpty() ? field.getName() : column.name();
            System.out.println("  " + field.getName() + " -> '" + name + "' nullable=" + column.nullable());
        }
    }
}
