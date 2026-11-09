# Medication lookup and safety features

The website now uses three public sources without an API key:

- **NLM RxNorm**: autocomplete and normalized medication suggestions. The website uses the approximate-term endpoint and stores the selected RxCUI only in the page state.
- **DailyMed**: current FDA-submitted Structured Product Label records and media URLs. When a matching label includes an image, the add-medication screen shows it with a manufacturer-variation note. DailyMed does not guarantee an image for every product.
- **openFDA Drug Label**: label sections such as `overdosage`, `boxed_warning`, `warnings_and_cautions`, and `warnings`. The site displays this as label reference text, not as a personalized medical decision.

The older NLM RxImage API is not used because NLM discontinued it. Images are therefore optional and come from DailyMed media when available.

## Added website behavior

The add-medication page now provides RxNorm dropdown recommendations. Choosing a suggestion triggers a DailyMed image lookup and an openFDA label-safety lookup. The medication list includes a local search field.

The dashboard includes a Possible Overdose page. A signed-in user can save the medication, estimated amount, time, and symptoms under their own Firebase `users/{uid}/overdoseReports` collection. The page clearly warns that it cannot diagnose an overdose and links to emergency services, U.S. Poison Control, and webPOISONCONTROL.

The overdose report must not be treated as a triage engine. If there are life-threatening symptoms, users must contact emergency services immediately. For the U.S., Poison Control is 1-800-222-1222. Users outside the U.S. should use their local emergency and poison-center procedures.
