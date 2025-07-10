package com.flytbase.drone.util.geometry;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

/** Utility class for generating waypoints for different flight patterns. */
@Component
public class WaypointGenerator {

  private final DistanceCalculator distanceCalculator;
  private final GeoJsonParser geoJsonParser;

  public WaypointGenerator(DistanceCalculator distanceCalculator, GeoJsonParser geoJsonParser) {
    this.distanceCalculator = distanceCalculator;
    this.geoJsonParser = geoJsonParser;
  }

  /**
   * Generate waypoints for a linear pattern.
   *
   * @param boundary the boundary polygon
   * @param altitude the flight altitude
   * @param spacing the spacing between lines in meters
   * @return a JSON string of waypoints
   */
  public String generateLinearPattern(Polygon boundary, int altitude, double spacing) {
    // Get the envelope (bounding box) of the polygon
    Geometry envelope = boundary.getEnvelope();
    Coordinate[] envelopeCoords = envelope.getCoordinates();

    // Calculate the width and height of the envelope
    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

    for (Coordinate coord : envelopeCoords) {
      minX = Math.min(minX, coord.x);
      minY = Math.min(minY, coord.y);
      maxX = Math.max(maxX, coord.x);
      maxY = Math.max(maxY, coord.y);
    }

    // Determine the direction of the pattern (east-west or north-south)
    // For simplicity, we'll use east-west (horizontal lines) if the area is wider than tall
    boolean horizontalLines = (maxX - minX) > (maxY - minY);

    // Calculate the number of lines needed
    double distance = horizontalLines ? (maxY - minY) : (maxX - minX);
    int numLines = (int) Math.ceil(distance / spacing) + 1;

    // Generate the lines
    List<Coordinate> waypoints = new ArrayList<>();
    boolean reverseDirection = false;

    for (int i = 0; i < numLines; i++) {
      // Calculate the position of this line
      double linePos =
          horizontalLines
              ? minY + (i * spacing * (maxY - minY) / distance)
              : minX + (i * spacing * (maxX - minX) / distance);

      // Create the line
      Coordinate start, end;
      if (horizontalLines) {
        start = new Coordinate(minX, linePos);
        end = new Coordinate(maxX, linePos);
      } else {
        start = new Coordinate(linePos, minY);
        end = new Coordinate(linePos, maxY);
      }

      // Reverse direction on alternating lines for efficiency
      if (reverseDirection) {
        Coordinate temp = start;
        start = end;
        end = temp;
      }
      reverseDirection = !reverseDirection;

      // Create a line and clip it to the boundary
      LineString line = boundary.getFactory().createLineString(new Coordinate[] {start, end});
      Geometry clippedLine = line.intersection(boundary);

      // Add the clipped line's coordinates to the waypoints
      if (!clippedLine.isEmpty()) {
        Coordinate[] lineCoords = clippedLine.getCoordinates();
        for (Coordinate coord : lineCoords) {
          waypoints.add(coord);
        }
      }
    }

    // Convert waypoints to GeoJSON
    return waypointsToGeoJson(waypoints, altitude);
  }

  /**
   * Generate waypoints for a crosshatch pattern.
   *
   * @param boundary the boundary polygon
   * @param altitude the flight altitude
   * @param spacing the spacing between lines in meters
   * @return a JSON string of waypoints
   */
  public String generateCrosshatchPattern(Polygon boundary, int altitude, double spacing) {
    // First generate horizontal lines
    List<Coordinate> waypoints = new ArrayList<>();

    // Get the envelope (bounding box) of the polygon
    Geometry envelope = boundary.getEnvelope();
    Coordinate[] envelopeCoords = envelope.getCoordinates();

    // Calculate the width and height of the envelope
    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

    for (Coordinate coord : envelopeCoords) {
      minX = Math.min(minX, coord.x);
      minY = Math.min(minY, coord.y);
      maxX = Math.max(maxX, coord.x);
      maxY = Math.max(maxY, coord.y);
    }

    // Generate horizontal lines
    int numHorizontalLines = (int) Math.ceil((maxY - minY) / spacing) + 1;
    boolean reverseDirection = false;

    for (int i = 0; i < numHorizontalLines; i++) {
      // Calculate the position of this line
      double linePos = minY + (i * spacing * (maxY - minY) / (maxY - minY));

      // Create the line
      Coordinate start = new Coordinate(minX, linePos);
      Coordinate end = new Coordinate(maxX, linePos);

      // Reverse direction on alternating lines for efficiency
      if (reverseDirection) {
        Coordinate temp = start;
        start = end;
        end = temp;
      }
      reverseDirection = !reverseDirection;

      // Create a line and clip it to the boundary
      LineString line = boundary.getFactory().createLineString(new Coordinate[] {start, end});
      Geometry clippedLine = line.intersection(boundary);

      // Add the clipped line's coordinates to the waypoints
      if (!clippedLine.isEmpty()) {
        Coordinate[] lineCoords = clippedLine.getCoordinates();
        for (Coordinate coord : lineCoords) {
          waypoints.add(coord);
        }
      }
    }

    // Generate vertical lines
    int numVerticalLines = (int) Math.ceil((maxX - minX) / spacing) + 1;
    reverseDirection = false;

    for (int i = 0; i < numVerticalLines; i++) {
      // Calculate the position of this line
      double linePos = minX + (i * spacing * (maxX - minX) / (maxX - minX));

      // Create the line
      Coordinate start = new Coordinate(linePos, minY);
      Coordinate end = new Coordinate(linePos, maxY);

      // Reverse direction on alternating lines for efficiency
      if (reverseDirection) {
        Coordinate temp = start;
        start = end;
        end = temp;
      }
      reverseDirection = !reverseDirection;

      // Create a line and clip it to the boundary
      LineString line = boundary.getFactory().createLineString(new Coordinate[] {start, end});
      Geometry clippedLine = line.intersection(boundary);

      // Add the clipped line's coordinates to the waypoints
      if (!clippedLine.isEmpty()) {
        Coordinate[] lineCoords = clippedLine.getCoordinates();
        for (Coordinate coord : lineCoords) {
          waypoints.add(coord);
        }
      }
    }

    // Convert waypoints to GeoJSON
    return waypointsToGeoJson(waypoints, altitude);
  }

  /**
   * Generate waypoints for a perimeter pattern.
   *
   * @param boundary the boundary polygon
   * @param altitude the flight altitude
   * @param numRings the number of concentric rings
   * @return a JSON string of waypoints
   */
  public String generatePerimeterPattern(Polygon boundary, int altitude, int numRings) {
    List<Coordinate> waypoints = new ArrayList<>();

    // Start with the exterior ring
    Coordinate[] exteriorRing = boundary.getExteriorRing().getCoordinates();
    for (Coordinate coord : exteriorRing) {
      waypoints.add(coord);
    }

    // If multiple rings are requested, create interior rings
    if (numRings > 1) {
      double bufferDistance = -0.0001; // Approximate buffer distance in degrees
      Geometry currentRing = boundary;

      for (int i = 1; i < numRings; i++) {
        // Create a smaller ring by buffering inward
        Geometry innerRing = currentRing.buffer(bufferDistance * i);

        // Skip if the buffer operation resulted in an empty geometry
        if (innerRing.isEmpty()) {
          break;
        }

        // Add the coordinates of this ring
        Coordinate[] ringCoords = innerRing.getCoordinates();
        for (Coordinate coord : ringCoords) {
          waypoints.add(coord);
        }

        currentRing = innerRing;
      }
    }

    // Convert waypoints to GeoJSON
    return waypointsToGeoJson(waypoints, altitude);
  }

  /**
   * Convert a list of waypoints to a GeoJSON string.
   *
   * @param waypoints the list of waypoints
   * @param altitude the flight altitude
   * @return a JSON string of waypoints
   */
  private String waypointsToGeoJson(List<Coordinate> waypoints, int altitude) {
    if (waypoints.isEmpty()) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < waypoints.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(geoJsonParser.coordinateToGeoJson(waypoints.get(i), altitude));
    }
    sb.append("]");

    return sb.toString();
  }
}
