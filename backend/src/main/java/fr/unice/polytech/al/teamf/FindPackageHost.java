package fr.unice.polytech.al.teamf;

import fr.unice.polytech.al.teamf.entities.GPSCoordinate;
import fr.unice.polytech.al.teamf.entities.Mission;
import fr.unice.polytech.al.teamf.entities.Parcel;
import fr.unice.polytech.al.teamf.entities.User;

public interface FindPackageHost {

    User findHost(Parcel parcel, GPSCoordinate coordinate);

    boolean answerToPendingPackageHosting(Parcel parcel, User user, boolean answer);

}
