package com.michael.poc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * ler os valores dos campos via reflection, não só os metadados.
 * https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/reflect/Field.html
 * -setAccessible(true): abre o campo private pra leitura em runtime.
 * -field.get(obj): lê o valor real daquele objeto, não a definição.
 * -combina metadado (@Column) e valor pra montar um INSERT de verdade.
 * -o Main básico lê o schema; aqui lemos os dados.
 */
public class FieldValuesMain {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Entity {
        String table();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Column {
        String name() default "";
    }

    @Entity(table = "users")
    static class User {
        @Column(name = "user_id")
        long id;

        @Column(name = "email")
        String email;

        String note; // sem @Column, fica fora do INSERT

        User(long id, String email, String note) {
            this.id = id;
            this.email = email;
            this.note = note;
        }
    }

    public static void main(String[] args) throws IllegalAccessException {
        User user = new User(1, "michael@teste.com", "ignorado");
        System.out.println(toInsert(user));
    }

    // nomes das colunas saem de @Column, os valores saem de field.get
    static String toInsert(Object entity) throws IllegalAccessException {
        Class<?> type = entity.getClass();
        String table = type.getAnnotation(Entity.class).table();

        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Column.class)) continue;
            field.setAccessible(true); // sem isso, get num campo private lança IllegalAccessException

            Column column = field.getAnnotation(Column.class);
            columns.add(column.name().isEmpty() ? field.getName() : column.name());
            values.add(format(field.get(entity)));
        }
        return "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES (" + String.join(", ", values) + ");";
    }

    static String format(Object value) {
        return value instanceof String text ? "'" + text + "'" : String.valueOf(value);
    }
}
