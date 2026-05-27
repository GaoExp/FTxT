package exp.ftxt.shared.color;

import android.graphics.Color;

public class ColorNameResolver {

    public static String getName(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int min = Math.min(r, Math.min(g, b));
        int max = Math.max(r, Math.max(g, b));
        int avg = (r + g + b) / 3;

        if (max == 0) return "Black";
        if (r == 255 && g == 255 && b == 255) return "White";
        if (r == 255 && g == 0 && b == 0) return "Red";
        if (r == 0 && g == 255 && b == 0) return "Green";
        if (r == 0 && g == 0 && b == 255) return "Blue";

        if (r > 220 && g < 40 && b < 40) return "Red";
        if (r > 200 && g < 80 && b < 80) return "Crimson";
        if (r > 200 && g > 180 && b < 50) return "Yellow";
        if (r > 200 && g > 100 && b < 50) return "Orange";
        if (r < 50 && g > 200 && b < 50) return "Green";
        if (r < 50 && g > 180 && b > 180) return "Cyan";
        if (r > 180 && g < 50 && b > 180) return "Magenta";
        if (r > 180 && g > 180 && b > 200) return "Lavender";
        if (r > 200 && g < 100 && b > 200) return "Bright Pink";
        if (r > 180 && g < 150 && b > 180) return "Pink";
        if (r < 80 && g < 80 && b < 80) return "Dark Gray";
        if (r > 180 && g > 180 && b > 180) return "Light Gray";
        if (r < 150 && g < 150 && b < 150) return "Gray";
        if (r > 200 && g < 80 && b > 100) return "Rose";
        if (r > 200 && g > 200 && b < 100) return "Lime";
        if (r > 100 && g > 100 && b < 50) return "Olive";
        if (r > 150 && g < 80 && b < 150) return "Purple";
        if (r < 50 && g < 50 && b > 200) return "Blue";
        if (r < 50 && g < 100 && b > 150) return "Indigo";
        if (r > 180 && g > 200 && b > 200) return "Ice Blue";
        if (r > 100 && g < 50 && b < 50) return "Maroon";
        if (r > 200 && g > 150 && b > 100) return "Peach";
        if (r > 200 && g > 150 && b < 80) return "Gold";
        if (r > 150 && g > 100 && b < 50) return "Brown";
        if (r < 50 && g > 150 && b < 50) return "Forest";
        if (r > 100 && g < 50 && b > 100) return "Plum";
        if (r > 220 && g > 100 && b > 220) return "Atomic Pink";

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float h = hsv[0];

        if (avg > 200) return "Pastel";
        if (avg < 50) return "Dark";

        if (h < 30) return "Red-Orange";
        if (h < 60) return "Yellow";
        if (h < 90) return "Lime";
        if (h < 150) return "Green";
        if (h < 200) return "Teal";
        if (h < 250) return "Blue";
        if (h < 300) return "Purple";
        if (h < 340) return "Pink";
        return "Red";
    }
}
