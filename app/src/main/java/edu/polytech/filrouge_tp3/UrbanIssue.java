package edu.polytech.filrouge_tp3;

import android.os.Parcel;

import androidx.annotation.NonNull;

public class UrbanIssue extends Issue {

    public UrbanIssue(String title, String description, String priority, float score) {
        super(title, description, priority, score);
        this.type = "Urban";
        setPriority(Priority.MEDIUM);
    }

    public UrbanIssue(double latitude, double longitude, String name, String description,
                      String image, float gravity, String type, String publicationTime, int nbInjured) {
        super(latitude, longitude, name, description, image, gravity, type, publicationTime, nbInjured);
        setPriority(Priority.MEDIUM);
    }

    public UrbanIssue(double latitude, double longitude, String name, String description,
                      String image, float gravity, String type, String publicationTime,
                      int nbInjured, Integer nbVehicles, boolean isBlocked) {
        super(latitude, longitude, name, description, image, gravity, type, publicationTime, nbInjured, nbVehicles, isBlocked);
        setPriority(isBlocked ? Priority.HIGH : Priority.MEDIUM);
    }

    protected UrbanIssue(Parcel in) {
        super(in);
    }

    @Override
    public String getSafetyProtocol() {
        return "Rester vigilant en zone urbaine.";
    }

    public static final Creator<UrbanIssue> CREATOR = new Creator<UrbanIssue>() {
        @Override
        public UrbanIssue createFromParcel(Parcel in) {
            return new UrbanIssue(in);
        }

        @Override
        public UrbanIssue[] newArray(int size) {
            return new UrbanIssue[size];
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

