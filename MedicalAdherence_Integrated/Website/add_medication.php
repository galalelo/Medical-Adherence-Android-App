<?php
require_once 'config.php'; require_login();
$pageTitle=page_title('Add Medication'); $showBack=true; $backHref='medications.php'; include 'partials/header.php';
?>
<div class="header-block center"><h1 class="title">Add Medication</h1><p class="subtitle">This medication will appear in the Android app too.</p></div>
<form class="form-card" data-medication-form>
  <div class="input-group medication-lookup-group"><label for="medicationName">Medication Name</label><input id="medicationName" name="medicationName" autocomplete="off" placeholder="Start typing a medication" required data-medication-lookup><div class="medication-suggestions" data-medication-suggestions hidden></div><small>Suggestions use the free U.S. National Library of Medicine RxNorm service.</small></div>
  <div class="medication-preview" data-medication-preview hidden></div>
  <div class="medication-safety-card" data-medication-safety hidden></div>
  <div class="input-group"><label for="dosage">Dosage</label><input id="dosage" name="dosage" placeholder="Dosage" required></div>
  <div class="input-group"><label for="medicationTime">Time</label><input id="medicationTime" type="time" name="medicationTime" required></div>
  <button class="btn" type="submit">Save Medication</button>
</form>
<?php include 'partials/footer.php'; ?>
