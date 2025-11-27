/*
 * Copyright (c) 2025, Denis Odesskiy. All rights reserved.
 *
 * This software is the confidential and proprietary information of Denis Odesskiy
 * ("Confidential Information"). You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you
 * entered into with Denis Odesskiy.
 */

package sealed;

public class ReflectionDemo {
    public static void main(String[] args) {
        Class<?> clazz = AbstractDevice.class;

        // Проверяем, является ли класс sealed
        System.out.println("Is sealed: " + clazz.isSealed());

        // Получаем список разрешённых наследников
        Class<?>[] permitted = clazz.getPermittedSubclasses();
        System.out.println("Permitted subclasses of " + clazz.getSimpleName() + ":");
        for (Class<?> desc : permitted) {
            System.out.println(" - " + desc.getSimpleName());
        }
    }
}