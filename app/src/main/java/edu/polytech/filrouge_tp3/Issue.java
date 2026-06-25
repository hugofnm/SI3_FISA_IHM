package edu.polytech.filrouge_tp3;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Issue implements Parcelable, IssueObservable {
    private static final String TAG = "Issue";
    private final String incidentId;
    private final String incidentTitle;
    private final String incidentDescription;
    private final long incidentTimestamp;
    private Priority incidentPriority;
    private Status incidentStatus;
    private transient List<IssueObserver> observers;

    public String title;
    public String priority;
    public String priorityLabel;
    public float score;
    public String cordonnates;
    public String date;
    public String location; // NON obligatoire
    public String type; // enum
    public int nbDead;
    public String involvedCar; // enum // NON obligatoire
    public boolean isBlocked; // NON obligatoire
    public String description; // compat UI
    public String descriptionLegacy; // NON obligatoire
    public String photo;
    public double latitude;
    public double longitude;

    // Nouveaux champs
    public float gravity;        // gravité 0-5
    public String publicationTime; // heure de publication
    public int nbInjured;        // nombre de blessés
    public Integer nbVehicles;   // nombre de voitures impliquées (constructeur 2), null si non renseigné

    protected Issue(String title, String description, Priority priority) {
        this(UUID.randomUUID().toString(), title, description, System.currentTimeMillis(), priority, Status.REPORTED);
    }

    protected Issue(String id, String title, String description, long timestamp, Priority priority, Status status) {
        this.incidentId = id;
        this.incidentTitle = title;
        this.incidentDescription = description;
        this.incidentTimestamp = timestamp;
        this.incidentPriority = priority == null ? Priority.MEDIUM : priority;
        this.incidentStatus = status == null ? Status.REPORTED : status;
        this.observers = new ArrayList<>();
        syncLegacyFields();
    }

    public Issue(String title, String description, String priority, float score) {
        this(title, description, parsePriority(priority));
        this.score = score;
        this.priorityLabel = priority;
    }

    // Constructeur 1 : champs essentiels
    protected Issue(double latitude, double longitude, String name, String description,
                    String image, float gravity, String type, String publicationTime, int nbInjured) {
        this(name, description, Priority.MEDIUM);
        this.latitude = latitude;
        this.longitude = longitude;
        this.photo = image;
        this.gravity = gravity;
        this.score = gravity;
        this.type = type;
        this.publicationTime = publicationTime;
        this.nbInjured = nbInjured;
        this.nbDead = nbInjured;
    }

    // Constructeur 2 : champs essentiels + voitures impliquées + route bloquée
    protected Issue(double latitude, double longitude, String name, String description,
                    String image, float gravity, String type, String publicationTime,
                    int nbInjured, Integer nbVehicles, boolean isBlocked) {
        this(latitude, longitude, name, description, image, gravity, type, publicationTime, nbInjured);
        this.nbVehicles = nbVehicles;
        this.isBlocked = isBlocked;
        if (isBlocked) setPriority(Priority.HIGH);
    }

    public Issue(String cordonnates, String date, String location, String type, int nbDead, String involvedCar,
            boolean isBlocked, String description, String photo) {
        this(UUID.randomUUID().toString(), type, description, System.currentTimeMillis(),
                isBlocked ? Priority.HIGH : Priority.MEDIUM, Status.REPORTED);
        this.cordonnates = cordonnates;
        this.date = date;
        this.location = location;
        this.type = type;
        this.nbDead = nbDead;
        this.involvedCar = involvedCar;
        this.isBlocked = isBlocked;
        this.photo = photo;
        this.score = this.incidentStatus.getRating();
        this.priorityLabel = this.incidentPriority.name();
        this.descriptionLegacy = description;
    }

    protected Issue(Parcel in) {
        incidentId = in.readString();
        incidentTitle = in.readString();
        incidentDescription = in.readString();
        incidentTimestamp = in.readLong();
        incidentPriority = parsePriority(in.readString());
        incidentStatus = parseStatus(in.readString());
        score = in.readFloat();
        cordonnates = in.readString();
        date = in.readString();
        location = in.readString();
        type = in.readString();
        nbDead = in.readInt();
        involvedCar = in.readString();
        isBlocked = in.readByte() != 0;
        descriptionLegacy = in.readString();
        photo = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        priorityLabel = incidentPriority.name();
        observers = new ArrayList<>();
        syncLegacyFields();
    }

    public String getId() {
        return incidentId;
    }

    public String getTitle() {
        return incidentTitle;
    }

    public String getDescription() {
        return incidentDescription;
    }

    public long getTimestamp() {
        return incidentTimestamp;
    }

    public Priority getPriority() {
        return incidentPriority;
    }

    public Status getStatus() {
        return incidentStatus;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
        notifyPictureChanged();
    }

    public void setPriority(Priority priority) {
        if (priority == null || priority == this.incidentPriority) {
            return;
        }
        this.incidentPriority = priority;
        this.priorityLabel = priority.name();
        syncLegacyFields();
        notifyPriorityChanged();
    }

    public void setStatus(Status status) {
        if (status == null || status == this.incidentStatus) {
            return;
        }
        Log.d(TAG, "Status change requested for " + incidentId + " : " + incidentStatus + " -> " + status);
        this.incidentStatus = status;
        this.score = status.getRating();
        syncLegacyFields();
        notifyStatusChanged();
    }

    @Override
    public void addObserver(IssueObserver observer) {
        ensureObservers();
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(IssueObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        notifyStatusChanged();
        notifyPriorityChanged();
    }

    protected void notifyStatusChanged() {
        if (observers == null) {
            return;
        }
        for (IssueObserver observer : observers) {
            observer.onStatusChanged(this);
        }
    }

    protected void notifyPriorityChanged() {
        if (observers == null) {
            return;
        }
        for (IssueObserver observer : observers) {
            observer.onPriorityChanged(this);
        }
    }

    protected void notifyPictureChanged() {
        if (observers == null) {
            return;
        }
        for (IssueObserver observer : observers) {
            observer.onPictureChanged(this);
        }
    }

    private void ensureObservers() {
        if (observers == null) {
            observers = new ArrayList<>();
        }
    }

    protected void syncLegacyFields() {
        this.title = incidentTitle;
        this.description = incidentDescription;
        this.descriptionLegacy = incidentDescription;
        this.priority = incidentPriority == null ? null : incidentPriority.name();
        this.priorityLabel = this.priority;
        this.score = incidentStatus == null ? score : incidentStatus.getRating();
    }

    private static Priority parsePriority(String priority) {
        if (priority == null) {
            return Priority.MEDIUM;
        }

        try {
            return Priority.valueOf(priority.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return Priority.MEDIUM;
        }
    }

    private static Status parseStatus(String status) {
        if (status == null) {
            return Status.REPORTED;
        }

        try {
            return Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return Status.REPORTED;
        }
    }

    public abstract String getSafetyProtocol();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(incidentId);
        parcel.writeString(incidentTitle);
        parcel.writeString(incidentDescription);
        parcel.writeLong(incidentTimestamp);
        parcel.writeString(incidentPriority == null ? null : incidentPriority.name());
        parcel.writeString(incidentStatus == null ? null : incidentStatus.name());
        parcel.writeFloat(score);
        parcel.writeString(cordonnates);
        parcel.writeString(date);
        parcel.writeString(location);
        parcel.writeString(type);
        parcel.writeInt(nbDead);
        parcel.writeString(involvedCar);
        parcel.writeByte((byte) (isBlocked ? 1 : 0));
        parcel.writeString(descriptionLegacy);
        parcel.writeString(photo);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
