package com.elesey3510.powerprojector;

import org.joml.Vector3f;


public abstract class Formulas {
    public static Vector3f kelvinToRGB(float kelvin) {
        kelvin = Math.clamp(kelvin, 1000.0f, 40000.0f);
        float temp = kelvin / 100.0f;
        float red, green, blue;

        // Расчет красного (Red)
        if (temp <= 66) {
            red = 255;
        } else {
            red = temp - 60;
            red = (float) (329.698727446 * Math.pow(red, -0.1332047592));
            red = Math.clamp(red, 0, 255);
        }

        // Расчет зеленого (Green)
        if (temp <= 66) {
            green = temp;
            green = (float) (99.4708025861 * Math.log(green) - 161.1195681661);
            green = Math.clamp(green, 0, 255);
        } else {
            green = temp - 60;
            green = (float) (288.1221695283 * Math.pow(green, -0.0755148492));
            green = Math.clamp(green, 0, 255);
        }

        // Расчет синего (Blue)
        if (temp >= 66) {
            blue = 255;
        } else if (temp <= 19) {
            blue = 0;
        } else {
            blue = temp - 10;
            blue = (float) (138.5177312231 * Math.log(blue) - 305.0447927307);
            blue = Math.clamp(blue, 0, 255);
        }

        // Нормализуем значения в диапазон 0.0f - 1.0f для передачи в Veil
        return new Vector3f(red / 255.0f, green / 255.0f, blue / 255.0f);
    }
}
