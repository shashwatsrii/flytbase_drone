package com.flytbase.drone.util.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Component;

/** Utility class for calculating distances between coordinates. */
@Component
public class DistanceCalculator {

  private static final double EARTH_RADIUS_METERS = 6371000; // Earth's radius in meters

  /**
   * Calculate the distance between two coordinates using the Haversine formula.
   *
   * @param coord1 the first coordinate
   * @param coord2 the second coordinate
   * @return the distance in meters
   */
  public double calculateDistance(Coordinate coord1, Coordinate coord2) {
    // Convert coordinates from degrees to radians
    double lat1 = Math.toRadians(coord1.y);
    double lon1 = Math.toRadians(coord1.x);
    double lat2 = Math.toRadians(coord2.y);
    double lon2 = Math.toRadians(coord2.x);

    // Haversine formula
    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_METERS * c;
  }

  /**
   * Calculate the total distance of a path.
   *
   * @param coordinates the array of coordinates forming the path
   * @return the total distance in meters
   */
  public double calculatePathDistance(Coordinate[] coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return 0;
    }

    double totalDistance = 0;
    for (int i = 0; i < coordinates.length - 1; i++) {
      totalDistance += calculateDistance(coordinates[i], coordinates[i + 1]);
    }

    return totalDistance;
  }

  /**
   * Calculate a point at a specific distance and bearing from a starting point.
   *
   * @param start the starting coordinate
   * @param distance the distance in meters
   * @param bearing the bearing in degrees (0 = north, 90 = east, etc.)
   * @return the new coordinate
   */
  public Coordinate calculatePointAtDistance(Coordinate start, double distance, double bearing) {
    double startLat = Math.toRadians(start.y);
    double startLon = Math.toRadians(start.x);
    double bearingRad = Math.toRadians(bearing);
    double distRatio = distance / EARTH_RADIUS_METERS;

    double endLat =
        Math.asin(
            Math.sin(startLat) * Math.cos(distRatio)
                + Math.cos(startLat) * Math.sin(distRatio) * Math.cos(bearingRad));
    double endLon =
        startLon
            + Math.atan2(
                Math.sin(bearingRad) * Math.sin(distRatio) * Math.cos(startLat),
                Math.cos(distRatio) - Math.sin(startLat) * Math.sin(endLat));

    // Convert back to degrees
    endLat = Math.toDegrees(endLat);
    endLon = Math.toDegrees(endLon);

    return new Coordinate(endLon, endLat);
  }
}
