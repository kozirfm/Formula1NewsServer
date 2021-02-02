package data;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class GrandPrix {
    @Expose
    String date;
    @Expose
    String flag;
    @Expose
    String grandPrix;
    @Expose
    String track;
    @Expose
    String length;
    @Expose
    String laps;
    @Expose
    String distance;
    @Expose
    String driver;
    @Expose
    String team;

    public GrandPrix(String date, String flag, String grandPrix, String track, String length, String laps, String distance, String driver, String team) {
        this.date = date;
        this.flag = flag;
        this.grandPrix = grandPrix;
        this.track = track;
        this.length = length;
        this.laps = laps;
        this.distance = distance;
        this.driver = driver;
        this.team = team;
    }

    public String getDate() {
        return date;
    }

    public String getFlag() {
        return flag;
    }

    public String getGrandPrix() {
        return grandPrix;
    }

    public String getTrack() {
        return track;
    }

    public String getLength() {
        return length;
    }

    public String getLaps() {
        return laps;
    }

    public String getDistance() {
        return distance;
    }

    public String getDriver() {
        return driver;
    }

    public String getTeam() {
        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrandPrix grandPrix1 = (GrandPrix) o;
        return Objects.equals(date, grandPrix1.date) &&
                Objects.equals(flag, grandPrix1.flag) &&
                Objects.equals(grandPrix, grandPrix1.grandPrix) &&
                Objects.equals(track, grandPrix1.track) &&
                Objects.equals(length, grandPrix1.length) &&
                Objects.equals(laps, grandPrix1.laps) &&
                Objects.equals(distance, grandPrix1.distance) &&
                Objects.equals(driver, grandPrix1.driver) &&
                Objects.equals(team, grandPrix1.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, flag, grandPrix, track, length, laps, distance, driver, team);
    }

    @Override
    public String toString() {
        return "GrandPrix{" +
                "date='" + date + '\'' +
                ", flag='" + flag + '\'' +
                ", grandPrix='" + grandPrix + '\'' +
                ", track='" + track + '\'' +
                ", length='" + length + '\'' +
                ", laps='" + laps + '\'' +
                ", distance='" + distance + '\'' +
                ", driver='" + driver + '\'' +
                ", team='" + team + '\'' +
                '}';
    }
}
