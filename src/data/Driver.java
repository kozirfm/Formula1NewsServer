package data;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class Driver {

    public Driver(int position, String name, String surname, String team, int points) {
        this.position = position;
        this.name = name;
        this.surname = surname;
        this.team = team;
        this.points = points;
    }

    @Expose
    private int position;
    @Expose
    private String name;
    @Expose
    private String surname;
    @Expose
    private String team;
    @Expose
    private int points;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
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
        Driver driver = (Driver) o;
        return position == driver.position &&
                points == driver.points &&
                Objects.equals(name, driver.name) &&
                Objects.equals(surname, driver.surname) &&
                Objects.equals(team, driver.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, name, surname, team, points);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", team='" + team + '\'' +
                ", points=" + points +
                '}';
    }
}
