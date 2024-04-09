package hu.app.hydrationapp.model;

public class DailyWaterIntake {
    public String date;
    public boolean goalAchieved;

    public DailyWaterIntake() {
        // Firebasehez szükséges üres konstruktor
    }

    public DailyWaterIntake(String date, boolean goalAchieved) {
        this.date = date;
        this.goalAchieved = goalAchieved;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isGoalAchieved() {
        return goalAchieved;
    }

    public void setGoalAchieved(boolean goalAchieved) {
        this.goalAchieved = goalAchieved;
    }
}
