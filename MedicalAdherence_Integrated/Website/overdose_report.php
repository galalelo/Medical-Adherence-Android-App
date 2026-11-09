<?php
require_once 'config.php'; require_login();
$pageTitle=page_title('Overdose Safety Report'); $showBack=true; $backHref='dashboard.php'; include 'partials/header.php';
?>
<div class="safety-page">
  <section class="safety-hero">
    <p class="dashboard-eyebrow">SAFETY SUPPORT</p>
    <h1 class="title">Possible Overdose Report</h1>
    <p class="subtitle">Record what happened and get connected to official emergency guidance.</p>
  </section>
  <div class="urgent-card">
    <strong>If the person collapses, has a seizure, has trouble breathing, or cannot be awakened, call emergency services immediately.</strong>
    <p>This form is not a diagnosis and must not delay emergency care.</p>
  </div>
  <form class="form-card safety-report-form" data-overdose-report>
    <div class="input-group medication-lookup-group">
      <label for="reportMedication">Medication or substance</label>
      <input id="reportMedication" name="reportMedication" autocomplete="off" placeholder="Start typing a medication" required data-medication-lookup>
      <div class="medication-suggestions" data-medication-suggestions hidden></div>
    </div>
    <div class="input-group"><label for="reportAmount">Estimated amount taken</label><input id="reportAmount" name="reportAmount" placeholder="For example: 4 tablets or 20 mL" required></div>
    <div class="input-group"><label for="reportWhen">When did it happen?</label><input id="reportWhen" name="reportWhen" type="datetime-local" required></div>
    <div class="input-group"><label for="reportSymptoms">Symptoms or other details</label><textarea id="reportSymptoms" name="reportSymptoms" rows="4" placeholder="Describe symptoms, age, and anything else responders should know"></textarea></div>
    <button class="btn danger" type="submit">SAVE REPORT AND SHOW HELP</button>
  </form>
  <div class="safety-result" data-overdose-result hidden></div>
  <p class="safety-disclaimer">For a suspected poisoning in the United States, Poison Control is free and confidential at <a href="tel:+18002221222">1-800-222-1222</a>. Use <a href="https://triage.webpoisoncontrol.org/" target="_blank" rel="noopener">webPOISONCONTROL</a> when appropriate. Outside the U.S., use your local poison center or emergency number.</p>
</div>
<?php include 'partials/footer.php'; ?>
