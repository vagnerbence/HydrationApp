package hu.app.hydrationapp.model;

public class User {
    public String name;
    public int height;
    public int weight;
    public int age;
    public String gender;
    public String activityLevel;
    public double totalWaterIntake; // Az új adattag a teljes vízmennyiségnek

    // Üres konstruktor szükséges a Firebase számára
    public User() {
    }

    // Konstruktor az objektum létrehozásához az adatokkal
    public User(String name, int height, int weight, int age, String gender, String activityLevel, double totalWaterIntake) {
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.totalWaterIntake = totalWaterIntake;
    }

    // Getterek és setterek (opcionális, de javasolt)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public double getTotalWaterIntake() {
        return totalWaterIntake;
    }

    public void setTotalWaterIntake(double totalWaterIntake) {
        this.totalWaterIntake = totalWaterIntake;
    }
}
