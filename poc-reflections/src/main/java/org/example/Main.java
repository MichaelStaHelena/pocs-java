package org.example;

import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * Demonstra como usar a Reflection API do Java.
 * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/package-summary.html
 * -Class.getClass(): obtém a classe em runtime.
 * -getDeclaredFields / getFields: campos privados da classe vs públicos + herdados.
 * -getDeclaredMethods: métodos declarados na classe, incluindo privados.
 * -getDeclaredConstructors: todos os construtores, incluindo privados.
 * -Field.setAccessible(true): quebra encapsulamento para ler/escrever campos privados.
 * -Method.invoke(): executa um método dinamicamente sem referência estática.
 * -Constructor.newInstance(): cria objetos sem usar new diretamente.
 * -getSuperclass(): navega pela hierarquia de herança em runtime.
 */
public class Main {

    private static class Car {
        public String name;
        private String brand;
        private int manufactureYear;
        private boolean isAutomatic;
        private int age;

        public Car(String name, String brand, int manufactureYear, boolean isAutomatic) {
            this.name = name;
            this.brand = brand;
            this.manufactureYear = manufactureYear;
            this.isAutomatic = isAutomatic;
            this.age = calculateAge(manufactureYear);
        }

        private int calculateAge(int manufactureYear) {
            return LocalDate.now().getYear() - manufactureYear;
        }

        public void honk() {
            System.out.println("mip mip do " + name);
        }

        public void showCarDetails() {
            System.out.println(name + " - " + brand);
        }
    }

    private static class FordCar extends Car {
        private boolean isNational;

        public FordCar(String name, int manufactureYear, boolean automatic, boolean isNational) {
            super(name, "Ford", manufactureYear, automatic);
            this.isNational = isNational;
        }

        public void honk() {
            System.out.println("bi bi do " + name);
        }
    }

    public static void main(String[] args) throws Exception {
        var civicao = new Car("Civicão", "Honda", 2018, true);
        var mustangao = new FordCar("Mustangão", 2020, true, false);

        readClassDetails(civicao);
        System.out.println("---");
        readClassDetails(mustangao);

        System.out.println("alterando via reflection");

        accessPrivateField(mustangao);
        callMethod(mustangao);

        createObjectDynamically();

        //useCaseJackson();
    }

    private static void readClassDetails(Object obj) {
        Class<?> cls = obj.getClass();

        System.out.println("Classe: " + cls.getSimpleName());
        System.out.println("Herda de: " + cls.getSuperclass().getSimpleName());

        System.out.println("Construtores:");
        for (Constructor<?> c : cls.getDeclaredConstructors()) { // inclui private
            System.out.println("  " + c);
        }

        System.out.println("Fields públicos:");
        for (Field f : cls.getFields()) { // inclui herdados
            System.out.println("  " + f.getName());
        }

        System.out.println("Fields declarados (public e private):");
        for (Field f : cls.getDeclaredFields()) { // não inclui herdados
            System.out.println("  " + f.getName());
        }

        System.out.println("Métodos:");
        for (Method m : cls.getDeclaredMethods()) { // inclui private, não inclui herdados
            System.out.println("  " + m.getName());
        }
    }

    /** Mostra que reflection consegue quebrar encapsulamento quando explicitamente permitido. */
    private static void accessPrivateField(FordCar car) throws Exception {
        Field field = car.getClass().getDeclaredField("isNational");
        field.setAccessible(true); // bypassa o private — sonar costuma reclamar disso

        System.out.println("Antes: " + field.get(car));
        field.set(car, true);
        System.out.println("Depois: " + field.get(car));
    }

    private static void callMethod(FordCar car) throws Exception {
        Method method = car.getClass().getDeclaredMethod("honk");

        System.out.println("Chamando método via reflection:");
        method.invoke(car);
    }

    /** Em compile time devolve Object; em runtime devolve a instância do tipo correto. */
    private static void createObjectDynamically() throws Exception {
        Class<?> cls = Car.class;

        Constructor<?> constructor = cls
                .getConstructor(String.class, String.class, int.class, boolean.class);

        Object car = constructor.newInstance("Golfzão", "VW", 2010, false);
        System.out.println(car.getClass().getName());
        System.out.println(car.getClass().getCanonicalName());

        System.out.println("Objeto criado via reflection:");
        ((Car) car).showCarDetails();
    }

    /** Jackson usa reflection internamente para deserializar JSON em objeto e vice-versa. */
    private static void useCaseJackson() {
        ObjectMapper mapper = new ObjectMapper();

        String json = """
        {
            "name": "Civicão",
            "brand": "Honda",
            "manufactureYear": 2018,
            "isAutomatic": true
        }
    """;

        Car car = mapper.readValue(json, Car.class);

        System.out.println("Jackson criou o objeto:");
        car.showCarDetails();

        String jsonOutput = mapper.writeValueAsString(car);
        System.out.println("Objeto para JSON: " + jsonOutput);
    }
}
