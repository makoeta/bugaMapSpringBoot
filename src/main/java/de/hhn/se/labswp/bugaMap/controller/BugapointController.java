package de.hhn.se.labswp.bugaMap.controller;

import de.hhn.se.labswp.bugaMap.crudRepos.BugapointRepository;
import de.hhn.se.labswp.bugaMap.jpa.Bugapoint;
import de.hhn.se.labswp.bugaMap.requests.BugapointRequest;
import de.hhn.se.labswp.bugaMap.responses.DatabaseSaveResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Mappings for Bugapoint table.
 */
@RestController
@RequestMapping("/bugapoint")
@CrossOrigin(origins = "http://localhost:4200")
public class BugapointController {

  private final BugapointRepository bugapointRepository;

  private final JdbcTemplate jdbcTemplate;

  public BugapointController(BugapointRepository bugapointRepository, JdbcTemplate jdbcTemplate) {
    this.bugapointRepository = bugapointRepository;
    this.jdbcTemplate = jdbcTemplate;
  }


  /**
   * Simple request to get all points in the database.
   *
   * @return bugapoints all bugapoints
   */
  @GetMapping("/list")
  public List<Bugapoint> getBugapoints() {
    return (List<Bugapoint>) bugapointRepository.findAll();
  }

  /**
   * Returns all bugapoints with the matching discriminators.
   *
   * @param discriminators discriminators
   * @return filtered bugapoints
   */
  @GetMapping("/list/filter")
  public List<Bugapoint> getBugapoints(@RequestParam(value = "discriminators", required = false)
  List<String> discriminators) {

    if (discriminators == null || discriminators.isEmpty()) {
      return new ArrayList<>();
    }

    String sql = "SELECT * FROM bugapoint WHERE Discriminator IN (" +
        String.join(",", Collections.nCopies(discriminators.size(), "?")) + ")";
    return jdbcTemplate.query(sql, discriminators.toArray(),
        new BeanPropertyRowMapper<>(Bugapoint.class));
  }

  /**
   * Adds the given bugapoint to the database.
   */
  @PostMapping("/save")
  public ResponseEntity<DatabaseSaveResponse> save(@RequestBody BugapointRequest request) {

    Bugapoint bugapoint = Bugapoint.builder()
        .parkID(request.getParkID())
        .adminID(request.getAdminID())
        .title(request.getTitle())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .description(request.getDescription())
        .discriminator(request.getDiscriminator())
        .build();

    try {
      bugapointRepository.save(bugapoint);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          new DatabaseSaveResponse(false, e.getCause().getCause().getMessage()));
    }

    return ResponseEntity.ok(new DatabaseSaveResponse(true, "Bugapoint saved"));
  }


  @PostMapping("/add")
  public ResponseEntity<DatabaseSaveResponse> add(@RequestParam(value = "parkId") int parkId,
      @RequestParam(value="adminId") int adminId, @RequestParam(value = "title") String title,
      @RequestParam(value = "latitude") double latitude,
      @RequestParam(value = "longitude") double longitude,
      @RequestParam(value = "discriminator") String discriminator,
      @RequestParam(value = "description") String description) {


    try {
      Bugapoint bugapoint = Bugapoint.builder()
          .parkID(parkId)
          .adminID(adminId)
          .title(title)
          .latitude(latitude)
          .longitude(longitude)
          .discriminator(discriminator)
          .description(description)
          .build();

      bugapointRepository.save(bugapoint);

      return ResponseEntity.ok(new DatabaseSaveResponse(true, "saved"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new DatabaseSaveResponse(false, e.getCause().getMessage()));
    }

  }

  /**
   * Simple request to get all the different types of the buga points.
   *
   * @return distinct discriminators
   */
  @GetMapping("/discriminators")
  public List<String> getDiscriminators() {
    return jdbcTemplate.queryForList(
        "SELECT DISTINCT discriminator FROM bugapoint", String.class);
  }



}
