package edu.polytech.filrouge_tp3;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class HighwayIssue extends Issue {

    public HighwayIssue(String title, String description, String priority, float score) {
        super(title, description, priority, score);
        this.type = "Highway";
        setPriority(Priority.CRITICAL);
    }

    public HighwayIssue(String cordonnates, String date, String location, String type, int nbDead, String involvedCar,
                        boolean isBlocked, String description, String photo) {
        super(cordonnates, date, location, type, nbDead, involvedCar, isBlocked, description, photo);
        this.type = "Highway";
        setPriority(Priority.CRITICAL);
    }

    public HighwayIssue(double latitude, double longitude, String name, String description,
                        String image, float gravity, String type, String publicationTime, int nbInjured) {
        super(latitude, longitude, name, description, image, gravity, type, publicationTime, nbInjured);
        setPriority(Priority.CRITICAL);
    }

    public HighwayIssue(double latitude, double longitude, String name, String description,
                        String image, float gravity, String type, String publicationTime,
                        int nbInjured, Integer nbVehicles, boolean isBlocked) {
        super(latitude, longitude, name, description, image, gravity, type, publicationTime, nbInjured, nbVehicles, isBlocked);
        setPriority(Priority.CRITICAL);
    }

    protected HighwayIssue(Parcel in) {
        super(in);
    }

    @Override
    public String getSafetyProtocol() {
        return "Rester derrière la glissière de sécurité !";
    }

    public static final Creator<HighwayIssue> CREATOR = new Creator<HighwayIssue>() {
        @Override
        public HighwayIssue createFromParcel(Parcel in) {
            return new HighwayIssue(in);
        }

        @Override
        public HighwayIssue[] newArray(int size) {
            return new HighwayIssue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
    }
}

