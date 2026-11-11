exports.handler = async function (event) {
  const params = event.queryStringParameters || {};
  const path = params.path || '';

  // Only proxy the two DailyMed resources used by the medication lookup UI.
  if (!/^spls(?:\/[0-9a-f-]+\/media)?$/i.test(path)) {
    return {
      statusCode: 400,
      headers: { 'content-type': 'application/json; charset=utf-8' },
      body: JSON.stringify({ error: 'Unsupported DailyMed resource.' })
    };
  }

  const upstream = new URL(`https://dailymed.nlm.nih.gov/dailymed/services/v2/${path}`);
  for (const [key, value] of Object.entries(params)) {
    if (key !== 'path' && value != null) upstream.searchParams.set(key, value);
  }

  try {
    const response = await fetch(upstream);
    const body = await response.text();
    return {
      statusCode: response.status,
      headers: {
        'content-type': response.headers.get('content-type') || 'application/json; charset=utf-8',
        'cache-control': 'public, max-age=300'
      },
      body
    };
  } catch (error) {
    return {
      statusCode: 502,
      headers: { 'content-type': 'application/json; charset=utf-8' },
      body: JSON.stringify({ error: 'DailyMed is temporarily unavailable.' })
    };
  }
};
