package com.kcserver.vermietung.zaehlerimport;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Explicit schema and connection-owned transaction: independent of JWT/OSIV tenant routing. */
@Service
public class MeterImportService {
    private final DataSource dataSource;
    private final MeterImportConfig config;
    public MeterImportService(DataSource dataSource, MeterImportConfig config) {
        this.dataSource = dataSource; this.config = config;
    }
    public MeterImportController.Result ingest(MeterImportController.Reading r) throws SQLException {
        if (!config.configured() || !config.getTenant().equals(r.tenant()) || !config.getMeterId().equals(r.meterId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Import binding mismatch");
        Instant time = r.measuredAt().toInstant();
        if (time.getNano() != 0 || time.isAfter(Instant.now().plusSeconds(300)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected measurement time in whole seconds, not in future");
        String schema = "\"" + config.getTenant() + "\".";
        try (Connection c = dataSource.getConnection()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // Lock the bound meter for a stable identity while inserting. Never accept an ID alone.
                try (var s = c.prepareStatement("SELECT id FROM " + schema + "zaehler WHERE id=? AND zaehlernummer=? AND aktiv=true AND verbrauchsart='STROM' AND mengeneinheit='kWh' FOR SHARE")) {
                    s.setLong(1, config.getMeterId()); s.setString(2, config.getMeterNumber());
                    try (var rs = s.executeQuery()) {
                        if (!rs.next()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bound active electricity meter not found");
                    }
                }
                String sql = "INSERT INTO " + schema + "zaehlerstand (zaehler_id,ablesedatum,zaehlerstand,bemerkung,import_messzeit,created_at,created_by,last_modified_at,last_modified_by) "
                    + "VALUES (?,?,?,?,?,CURRENT_TIMESTAMP,'ha-meter-import',CURRENT_TIMESTAMP,'ha-meter-import') "
                    + "ON CONFLICT (zaehler_id,import_messzeit) DO NOTHING RETURNING id";
                try (var s = c.prepareStatement(sql)) {
                    s.setLong(1, r.meterId());
                    s.setObject(2, LocalDateTime.ofInstant(time, ZoneId.of(config.getZone())));
                    s.setBigDecimal(3, r.value()); s.setString(4, r.remark());
                    s.setObject(5, time.atOffset(ZoneOffset.UTC));
                    try (var rs = s.executeQuery()) {
                        if (rs.next()) {
                            long id = rs.getLong(1); c.commit();
                            return new MeterImportController.Result(id, true);
                        }
                    }
                }
                try (var s = c.prepareStatement("SELECT id,zaehlerstand,bemerkung FROM " + schema + "zaehlerstand WHERE zaehler_id=? AND import_messzeit=?")) {
                    s.setLong(1, r.meterId()); s.setObject(2, time.atOffset(ZoneOffset.UTC));
                    try (var rs = s.executeQuery()) {
                        if (!rs.next() || rs.getBigDecimal(2).compareTo(r.value()) != 0 || !java.util.Objects.equals(rs.getString(3), r.remark()))
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "Measurement already exists with different data");
                        long id = rs.getLong(1); c.commit();
                        return new MeterImportController.Result(id, false);
                    }
                }
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(autoCommit); }
        }
    }
}
