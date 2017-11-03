package tio4500.simulations.Entities;

public class Car {

    private final int carId;
    private double batteryLevel;

    public Car(int carId, double batteryLevel) {
        this.carId = carId;
        this.batteryLevel = batteryLevel;
    }

    public int getCarId() {
        return carId;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

}
