package com.flytbase.drone.util.geometry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Component;

/** Utility class for parsing GeoJSON into JTS Geometry objects. */
@Component
@Slf4j
public class GeoJsonParser {

  private final ObjectMapper objectMapper;
  private final GeometryFactory geometryFactory;

  public GeoJsonParser() {
    this.objectMapper = new ObjectMapper();
    this.geometryFactory = new GeometryFactory();
  }

  /**
   * Parse a GeoJSON string into a JTS Polygon.
   *
   * @param geoJson the GeoJSON string
   * @return the JTS Polygon
   * @throws IOException if the GeoJSON is invalid
   */
  public Polygon parsePolygon(String geoJson) throws IOException {
    JsonNode rootNode = objectMapper.readTree(geoJson);

    if (!rootNode.has("type") || !rootNode.get("type").asText().equals("Polygon")) {
      throw new IllegalArgumentException("GeoJSON must be of type Polygon");
    }

    JsonNode coordinatesNode = rootNode.get("coordinates");
    if (coordinatesNode == null || !coordinatesNode.isArray() || coordinatesNode.size() == 0) {
      throw new IllegalArgumentException("Invalid coordinates in GeoJSON");
    }

    // Get the exterior ring
    JsonNode exteriorRingNode = coordinatesNode.get(0);
    if (!exteriorRingNode.isArray() || exteriorRingNode.size() < 4) {
      throw new IllegalArgumentException("Exterior ring must have at least 4 points");
    }

    // Parse exterior ring coordinates
    Coordinate[] exteriorCoordinates = new Coordinate[exteriorRingNode.size()];
    for (int i = 0; i < exteriorRingNode.size(); i++) {
      JsonNode pointNode = exteriorRingNode.get(i);
      if (!pointNode.isArray() || pointNode.size() < 2) {
        throw new IllegalArgumentException("Invalid point at index " + i);
      }

      double lng = pointNode.get(0).asDouble();
      double lat = pointNode.get(1).asDouble();
      exteriorCoordinates[i] = new Coordinate(lng, lat);
    }

    // Create the exterior ring
    LinearRing exteriorRing = geometryFactory.createLinearRing(exteriorCoordinates);

    // Parse interior rings (holes) if any
    List<LinearRing> interiorRings = new ArrayList<>();
    for (int i = 1; i < coordinatesNode.size(); i++) {
      JsonNode interiorRingNode = coordinatesNode.get(i);
      if (!interiorRingNode.isArray() || interiorRingNode.size() < 4) {
        throw new IllegalArgumentException("Interior ring must have at least 4 points");
      }

      Coordinate[] interiorCoordinates = new Coordinate[interiorRingNode.size()];
      for (int j = 0; j < interiorRingNode.size(); j++) {
        JsonNode pointNode = interiorRingNode.get(j);
        if (!pointNode.isArray() || pointNode.size() < 2) {
          throw new IllegalArgumentException(
              "Invalid point at index " + j + " in interior ring " + i);
        }

        double lng = pointNode.get(0).asDouble();
        double lat = pointNode.get(1).asDouble();
        interiorCoordinates[j] = new Coordinate(lng, lat);
      }

      interiorRings.add(geometryFactory.createLinearRing(interiorCoordinates));
    }

    // Create the polygon with exterior and interior rings
    return geometryFactory.createPolygon(exteriorRing, interiorRings.toArray(new LinearRing[0]));
  }

  /**
   * Convert a JTS Coordinate to a GeoJSON point string.
   *
   * @param coordinate the JTS Coordinate
   * @param altitude the altitude
   * @return the GeoJSON point string
   */
  public String coordinateToGeoJson(Coordinate coordinate, int altitude) {
    return String.format(
        "{\"lat\": %f, \"lng\": %f, \"alt\": %d}", coordinate.y, coordinate.x, altitude);
  }
}
