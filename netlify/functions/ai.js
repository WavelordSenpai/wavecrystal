exports.handler = async (event) => {
  const headers = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'Content-Type', 'Access-Control-Allow-Methods': 'GET,POST,OPTIONS', 'Content-Type': 'application/json' };
  if (event.httpMethod === 'OPTIONS') return { statusCode: 200, headers, body: '{}' };
  try {
    let prompt = '';
    if (event.queryStringParameters) prompt = event.queryStringParameters.prompt || event.queryStringParameters.message || event.queryStringParameters.q || '';
    if (event.body) { try { const b = JSON.parse(event.body); prompt = prompt || b.prompt || b.message || b.text || b.question || b.input || ''; } catch(e){ prompt = prompt || event.body; } }
    if (!prompt) return { statusCode: 400, headers, body: JSON.stringify({ answer: '', error: 'No prompt' }) };
    const key = process.env.GEMINI_API_KEY;
    if (!key) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'No API key - check Netlify variable' }) };

    // CORRECT Interactions API - v1beta/interactions + header auth
    const url = `https://generativelanguage.googleapis.com/v1beta/interactions`;
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'x-goog-api-key': key },
      body: JSON.stringify({ model: 'gemini-3.8-flash', input: prompt, store: false })
    });
    const raw = await res.text();
    console.log('GEMINI STATUS:', res.status, raw.slice(0,1000));
    let data = {};
    try { data = JSON.parse(raw); } catch(e) { data = {}; }
    let text = data.output_text || '';
    if (!text && data.outputs) text = data.outputs.map(o => (o.content||[]).map(c=>c.text||'').join('')).join('');
    if (!text && data.output) text = typeof data.output === 'string' ? data.output : JSON.stringify(data.output);
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'Empty from Gemini: ' + raw.slice(0,600) }) };
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text }) };
  } catch(e){ console.log('FUNCTION ERROR:', e.message); return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: e.message }) }; }
};
