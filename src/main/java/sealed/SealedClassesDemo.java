/*
 * Copyright (c) 2025, Denis Odesskiy. All rights reserved.
 *
 * This software is the confidential and proprietary information of Denis Odesskiy
 * ("Confidential Information"). You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you
 * entered into with Denis Odesskiy.
 */

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
