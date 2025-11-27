# 🔒 Запечатанные (sealed) классы и интерфейсы в Java

Запечатанные (sealed) классы и интерфейсы — это новая возможность языка Java, которая появилась в релизе **Java 15**
как *preview*-фича, а окончательно стабилизировалась в **Java 17**.  
Идея заключается в том, чтобы ограничить круг наследников для определённого класса или интерфейса. Это даёт разработчику
контроль над иерархией и позволяет компилятору проверять исчерпывающие конструкции, такие как `switch` или *pattern
matching*.

📖 Официальное предложение описано в [JEP 360: Sealed Classes](https://openjdk.org/jeps/360).  
В Java 17 эта возможность стала стандартной частью языка.

---

## ✨ Зачем нужны sealed-классы?

1. **Контроль наследования** — разработчик явно указывает, какие классы могут наследовать базовый sealed-тип.
2. **Повышение безопасности** — ограничение круга наследников помогает избежать ошибок и упрощает анализ кода.
3. **Исчерпывающие проверки** — компилятор знает все возможные варианты sealed-иерархии и может требовать полного
   покрытия в `switch` или pattern matching.
4. **Удобство моделирования** — sealed-иерархии хорошо подходят для моделирования закрытых наборов сущностей, например,
   типов событий, состояний, команд.

---

## 📝 Синтаксис

Чтобы объявить sealed-тип, используется модификатор `sealed`.  
После этого необходимо указать список разрешённых наследников через ключевое слово `permits`.

```java
sealed interface Shape permits Circle, Rectangle, Triangle {
}

final class Circle implements Shape {
}

final class Rectangle implements Shape {
}

final class Triangle implements Shape {
}
```

В этом примере интерфейс Shape может иметь только три реализации: Circle, Rectangle и Triangle. Другие классы не смогут
его реализовать.

---

## Варианты наследников

Наследники sealed-типа должны быть объявлены одним из трёх способов:

1. `final` — окончательный класс, который нельзя расширять дальше.
2. `sealed` — сам запечатанный класс, который продолжает ограничивать наследование.
3. `non-sealed` — класс, который снимает ограничения и позволяет наследоваться свободно.

Таким образом, у разработчика есть полный контроль: можно закрыть иерархию, можно продолжить ограничивать, а можно
разрешить свободное расширение.

---

## Пример из практики

В учебном примере «Умный дом» можно построить sealed-иерархию устройств:

```java

/*
 * Пример демонстрации sealed-иерархии в Java.
 * Sealed-классы и интерфейсы появились как preview в Java 15 (JEP 360),
 * а стали стандартом в Java 17.
 *
 * Идея: ограничить круг наследников, чтобы компилятор знал все варианты.
 */

package sealed;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Главный класс-демо.
 * Здесь мы создаём список устройств и демонстрируем работу sealed-иерархии.
 */
public class SealedClassesDemo {

    public static void main(String[] args) {
        List<Switchable> devices = List.of(
                new AirConditioner(),
                new Lamp(),
                new AlarmSystem(),
                new FireAlarmSystem(),
                new SmartLamp(),
                new RgbLamp()
        );

        // Перебираем устройства и выполняем действия
        for (Switchable device : devices) {
            device.turnOn();

            // Pattern matching for instanceof (Java 16+)
            // Позволяет сразу "распаковать" объект в переменную нужного типа
            if (device instanceof AirConditioner ac) {
                ac.setTemperature(24);
            } else if (device instanceof Lamp lamp) {
                lamp.dim(70);
            } else if (device instanceof FireAlarmSystem fire) {
                fire.triggerAlarm();
            } else if (device instanceof RgbLamp rgb) {
                rgb.changeColor("Red");
                rgb.setBrightness(80);
            } else if (device instanceof SmartLamp smart) {
                smart.changeColor("Blue");
                smart.runScenario("Evening Relax");
            } else if (device instanceof AlarmSystem alarm) {
                alarm.triggerAlarm();
            }

            device.turnOff();
            System.out.println();
        }
    }
}

/**
 * Sealed интерфейс.
 * Ограничивает круг реализаций: только AbstractDevice.
 */
sealed interface Switchable permits AbstractDevice {
    void turnOn();

    void turnOff();
}

/**
 * Sealed абстрактный базовый класс.
 * Разрешает наследование только указанным классам (AirConditioner, Lamp, AlarmSystem, SmartLamp).
 * Здесь реализован общий функционал: генерация ID и красивое имя.
 */
sealed abstract class AbstractDevice implements Switchable
        permits AirConditioner, Lamp, AlarmSystem, SmartLamp {

    // Счётчики для каждого класса устройств
    private static final ConcurrentHashMap<Class<?>, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final int id;

    // Конструктор: каждому устройству присваивается уникальный ID
    protected AbstractDevice() {
        this.id = counters.computeIfAbsent(getClass(), k -> new AtomicInteger()).incrementAndGet();
    }

    // Метод для красивого отображения имени класса (разбивает CamelCase на слова)
    protected String displayName() {
        String className = getClass().getSimpleName();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char ch = className.charAt(i);
            if (Character.isUpperCase(ch) && i > 0) sb.append(' ');
            sb.append(ch);
        }
        return sb.toString();
    }

    protected int getId() {
        return id;
    }
}

/**
 * Кондиционер.
 * final — значит, нельзя наследовать дальше.
 */
final class AirConditioner extends AbstractDevice {
    @Override
    public void turnOn() {
        System.out.println(displayName() + " #" + getId() + " is turned ON.");
    }

    @Override
    public void turnOff() {
        System.out.println(displayName() + " #" + getId() + " is turned OFF.");
    }

    public void setTemperature(int degrees) {
        System.out.println(displayName() + " #" + getId() + " set to " + degrees + "°C.");
    }
}

/**
 * Лампа.
 * final — тоже закрытый класс.
 */
final class Lamp extends AbstractDevice {
    @Override
    public void turnOn() {
        System.out.println(displayName() + " #" + getId() + " is turned ON.");
    }

    @Override
    public void turnOff() {
        System.out.println(displayName() + " #" + getId() + " is turned OFF.");
    }

    public void dim(int percent) {
        System.out.println(displayName() + " #" + getId() + " dimmed to " + percent + "%.");
    }
}

/**
 * Система сигнализации.
 * sealed — значит, можно наследовать, но только тем классам, что указаны в permits.
 * Здесь разрешён только FireAlarmSystem.
 */
sealed class AlarmSystem extends AbstractDevice permits FireAlarmSystem {
    @Override
    public void turnOn() {
        System.out.println(displayName() + " #" + getId() + " is turned ON.");
    }

    @Override
    public void turnOff() {
        System.out.println(displayName() + " #" + getId() + " is turned OFF.");
    }

    public void triggerAlarm() {
        System.out.println(displayName() + " #" + getId() + " ALARM TRIGGERED!");
    }
}

/**
 * Пожарная сигнализация.
 * final — закрытый класс, наследует AlarmSystem.
 */
final class FireAlarmSystem extends AlarmSystem {
    @Override
    public void turnOn() {
        System.out.println(displayName() + " #" + getId() + " is monitoring heat and smoke...");
    }
}

/**
 * Смарт-лампа.
 * non-sealed — снимает ограничения, можно наследовать свободно.
 */
non-sealed class SmartLamp extends AbstractDevice {
    @Override
    public void turnOn() {
        System.out.println(displayName() + " #" + getId() + " is turned ON with smart features.");
    }

    @Override
    public void turnOff() {
        System.out.println(displayName() + " #" + getId() + " is turned OFF.");
    }

    public void changeColor(String color) {
        System.out.println(displayName() + " #" + getId() + " changed color to " + color + ".");
    }

    public void runScenario(String scenario) {
        System.out.println(displayName() + " #" + getId() + " running scenario: " + scenario);
    }
}

/**
 * RGB-лампа.
 * final — закрытый класс, наследует SmartLamp.
 */
final class RgbLamp extends SmartLamp {
    public void setBrightness(int percent) {
        System.out.println(displayName() + " #" + getId() + " brightness set to " + percent + "%.");
    }
}

// final class FloodControlAlarmSystem extends AlarmSystem { }
// ↑ Если раскомментировать — будет ошибка компиляции,
// потому что AlarmSystem разрешает наследование только FireAlarmSystem.
```

Здесь видно, что базовый класс AbstractDevice разрешает только четыре наследника. AlarmSystem сам является `sealed` и
разрешает только FireAlarmSystem. SmartLamp объявлен как `non-sealed`, поэтому его можно расширять свободно, например,
создавая RgbLamp.

---

## sealed и record

Комбинация `sealed` и `record` особенно удобна для моделирования закрытых наборов данных. Record-классы
автоматически `final`, а `sealed` гарантирует, что список вариантов фиксирован. Это позволяет строить исчерпывающие
проверки и упрощает работу с данными.

Пример:

```java
sealed interface Device permits AirConditioner, Lamp {
}

record AirConditioner(int id) implements Device {
}

record Lamp(int id) implements Device {
}
```

Здесь интерфейс Device может иметь только два варианта: AirConditioner и Lamp. Компилятор знает все варианты и может
проверять `switch` на полноту.

---

## Когда использовать sealed-классы?

* Когда нужно ограничить иерархию и предотвратить появление неожиданных наследников.
* Когда важно, чтобы компилятор знал все варианты и мог проверять исчерпывающие конструкции.
* Когда моделируется закрытый набор сущностей: состояния, события, команды, устройства.
* Когда хочется повысить читаемость и предсказуемость архитектуры.

---

## Вывод

Запечатанные классы и интерфейсы — это мощный инструмент языка Java, который позволяет контролировать наследование и
строить более надёжные архитектуры.
В сочетании с `record` и pattern matching sealed-иерархии дают разработчику удобный способ моделировать закрытые наборы
сущностей и обеспечивать безопасность и предсказуемость кода.

---

#### Author: Denis Odesskiy ([MyFreeIT](https://myfreeit.github.io/)).