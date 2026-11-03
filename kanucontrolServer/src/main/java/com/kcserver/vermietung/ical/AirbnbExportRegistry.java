package com.kcserver.vermietung.ical;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** Nur Hashes der Feed-Schlüssel. Alle SQL-Abfragen verwenden explizit das globale Schema. */
@Repository
public class AirbnbExportRegistry {
    private final JdbcTemplate jdbc;
    public AirbnbExportRegistry(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Config(String tenant, long objektId, boolean aktiv, Set<Long> ids, long version, boolean linkVorhanden) {}
    private static final String SELECT = "SELECT tenant, mietobjekt_id, aktiv, mietbereich_ids, version, token_hash FROM public.airbnb_kalender_export ";
    private Config config(java.sql.ResultSet rs) throws java.sql.SQLException {
        Set<Long> ids = new TreeSet<>();
        for (String id : rs.getString("mietbereich_ids").split(",")) if (!id.isBlank()) ids.add(Long.valueOf(id));
        return new Config(rs.getString("tenant"), rs.getLong("mietobjekt_id"), rs.getBoolean("aktiv"),
                Set.copyOf(ids), rs.getLong("version"), rs.getString("token_hash") != null);
    }
    public Optional<Config> get(String tenant, long objektId) {
        return jdbc.query(SELECT + "WHERE tenant = ? AND mietobjekt_id = ?", (rs, n) -> config(rs), tenant, objektId).stream().findFirst();
    }
    public Optional<Config> lookup(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return Optional.empty();
        return jdbc.query(SELECT + "WHERE token_hash = ?", (rs, n) -> config(rs), digest(token)).stream().findFirst();
    }
    public void create(String tenant, long objektId, boolean aktiv, Set<Long> ids, String token) {
        jdbc.update("INSERT INTO public.airbnb_kalender_export (tenant,mietobjekt_id,aktiv,mietbereich_ids,version,token_hash) VALUES (?,?,?,?,1,?)",
                tenant, objektId, aktiv, ids(ids), token == null ? null : digest(token));
    }
    public boolean update(Config current, boolean aktiv, Set<Long> ids, String token) {
        String sql = "UPDATE public.airbnb_kalender_export SET aktiv=?, mietbereich_ids=?, version=version+1"
                + (token == null ? "" : ", token_hash=?") + " WHERE tenant=? AND mietobjekt_id=? AND version=?";
        return (token == null ? jdbc.update(sql,aktiv,ids(ids),current.tenant(),current.objektId(),current.version())
                : jdbc.update(sql,aktiv,ids(ids),digest(token),current.tenant(),current.objektId(),current.version())) == 1;
    }
    private static String ids(Set<Long> values) {
        return String.join(",", values.stream().sorted().map(String::valueOf).toList());
    }
    public static String newToken() {
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
