package hu.app.hydrationapp.model;

public class HydrationCalculator {

    public static double calculateActivityWater(String activityLevel) {
        switch (activityLevel) {
            case "None":
                return 0.0;
            case "Rare":
                return 0.58;
            case "Medium":
                return 1.16;
            case "High":
                return 1.74;
            default:
                return 0.0; // Ha az aktivitási szint nem ismert
        }
    }

    // Hozzáadtuk a 'gender' paramétert a metódus szignatúrájához
    public static double calculateBaseWater(double weight, int age, String gender) {
        double ageFactor = age < 30 ? 40 : age < 55 ? 35 : 30;
        double weightInPounds = weight / 0.45359237; // A súly átváltása fontba
        double baseWaterOunces = ((weightInPounds / 2) * ageFactor) / 28.3;
        double baseWaterLiters = baseWaterOunces / 33.814; // Az eredmény átváltása literbe

        // Ellenőrizzük a nem-specifikus minimális vízbevitelt és szükség esetén állítsuk be
        if ("Male".equals(gender) && baseWaterLiters < 2.25) {
            return 2.25;
        } else if ("Female".equals(gender) && baseWaterLiters < 2.0) {
            return 2.0;
        }

        return baseWaterLiters;
    }
}
