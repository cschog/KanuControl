<?php if (!defined('ABSPATH')) { exit; } ?>
<section class="kc-hp" aria-label="Aufenthalt anfragen">
  <header><p class="kc-hp-eyebrow">Buchungsanfrage</p><h2>Deinen Aufenthalt anfragen</h2><p>Wähle deinen Wunschtermin und prüfe die Verfügbarkeit. Wir prüfen deine Anfrage anschließend persönlich.</p></header>
  <div class="kc-hp-message" role="status" aria-live="polite">Formular wird geladen …</div>
  <form hidden>
    <fieldset><legend>1. Zeitraum und Mietumfang</legend>
      <p class="kc-hp-object"></p>
      <div class="kc-hp-grid"><label>Anreise<input name="anreise" type="date" required /></label><label>Abreise<input name="abreise" type="date" required /></label></div>
      <p class="kc-hp-times"></p><p class="kc-hp-note">Mindestaufenthalt: eine Übernachtung. Die Abreise ist frühestens am Tag nach der Anreise möglich.</p><div class="kc-hp-resources"></div>
      <p class="kc-hp-note">Die angezeigten Einzelpreise stammen aus KC. Den endgültigen Preis einschließlich möglicher Rabatte und zusätzlicher Verbrauchskosten prüft die Vermietung.</p>
      <button type="button" class="kc-hp-check">Verfügbarkeit prüfen</button><p class="kc-hp-availability" role="status" aria-live="polite"></p>
    </fieldset>
    <fieldset><legend>2. Kontakt und Teilnehmer</legend>
      <div class="kc-hp-grid">
        <label>Vorname<input name="vorname" autocomplete="given-name" maxlength="100" required /></label>
        <label>Nachname<input name="name" autocomplete="family-name" maxlength="100" required /></label>
        <label>E-Mail<input name="email" type="email" autocomplete="email" maxlength="254" required /></label>
        <label>E-Mail wiederholen<input name="emailWiederholung" type="email" autocomplete="off" maxlength="254" required /></label>
        <label>Telefon<input name="telefon" type="tel" autocomplete="tel" maxlength="80" required /></label>
        <label>Straße und Hausnummer<input name="strasse" autocomplete="street-address" maxlength="200" required /></label>
        <label>Postleitzahl<input name="plz" autocomplete="postal-code" maxlength="20" required /></label>
        <label>Ort<input name="ort" autocomplete="address-level2" maxlength="120" required /></label>
        <label>Ländercode<input name="countryCode" autocomplete="country" value="DE" pattern="[A-Za-z]{2}" maxlength="2" required /><span>Zum Beispiel DE, NL oder BE.</span></label>
        <label>Veranstalter / Verein / Schule (optional)<input name="veranstalter" maxlength="500" /></label>
        <label>Anzahl Jugendliche<input name="anzahlJugendliche" type="number" min="0" max="100000" step="1" /></label>
        <label>Anzahl Erwachsene<input name="anzahlErwachsene" type="number" min="0" max="100000" step="1" /></label>
        <label>Alter ungefähr von<input name="jugendAlterVon" type="number" min="0" max="120" step="1" disabled /></label>
        <label>Alter ungefähr bis<input name="jugendAlterBis" type="number" min="0" max="120" step="1" disabled /></label>
      </div><p class="kc-hp-total"></p>
      <label>Bemerkungen (optional)<textarea name="bemerkung" rows="4" maxlength="3000"></textarea></label>
    </fieldset>
    <fieldset><legend>3. Hinweise und Anfrage</legend>
      <label class="kc-hp-checkbox"><input name="hausregelnAkzeptiert" type="checkbox" required /><span>Ich habe die <a class="kc-hp-rules" target="_blank" rel="noopener">Hausregeln / Nutzungsbedingungen</a> gelesen und akzeptiere sie.</span></label>
      <label class="kc-hp-checkbox"><input name="datenschutzGelesen" type="checkbox" required /><span>Ich habe den <a class="kc-hp-privacy" target="_blank" rel="noopener">Datenschutzhinweis</a> zur Kenntnis genommen.</span></label>
      <div class="kc-hp-honey" aria-hidden="true"><label>Website<input name="website" tabindex="-1" autocomplete="off" /></label></div>
      <p>Mit dem Absenden stellst du eine unverbindliche Anfrage. Eine freie Anzeige oder Eingangsbestätigung ist noch keine Reservierung oder Buchungsbestätigung.</p>
      <button class="kc-hp-submit" type="submit" disabled>Anfrage absenden</button>
    </fieldset>
  </form>
  <noscript>Bitte JavaScript aktivieren oder die Kontaktmöglichkeiten auf dieser Seite verwenden.</noscript>
</section>
