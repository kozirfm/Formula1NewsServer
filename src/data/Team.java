package data;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {

    public Team(String name) {
        this.drivers = new ArrayList<>();
        this.name = name;
        this.points = 0;
    }

    @Expose
    private List<Driver> drivers;
    @Expose
    private String name;
    @Expose
    private int points;

    public List<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(List<Driver> drivers) {
        this.drivers = drivers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(drivers, team.drivers) &&
                Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(drivers, name);
    }

    @Override
    public String toString() {
        return "Team{" +
                "drivers=" + drivers +
                ", name='" + name + '\'' +
                ", points=" + points +
                '}';
    }
}
