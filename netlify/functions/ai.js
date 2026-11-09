exports.handler = async (event) => {
  const headers = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'Content-Type', 'Access-Control-Allow-Methods': 'GET,POST,OPTIONS', 'Content-Type': 'application/json' };
  if (event.httpMethod === 'OPTIONS') return { statusCode: 200, headers, body: '{}' };
  try {
    let prompt = '';
    if (event.queryStringParameters) prompt = event.queryStringParameters.prompt || event.queryStringParameters.message || event.queryStringParameters.q || '';
    if (event.body) { try { const b = JSON.parse(event.body); prompt = prompt || b.prompt || b.message || b.text || b.question || b.input || ''; } catch(e){ prompt = prompt || event.body; } }
    if (!prompt) return { statusCode: 400, headers, body: JSON.stringify({ answer: '', error: 'No prompt' }) };
    const key = process.env.GEMINI_API_KEY;
    if (!key) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'No API key' }) };

    // NEW Interactions API for Gemini 3.8
    const url = `https://generativelanguage.googleapis.com/v1beta/interactions:generate?key=${key}`;
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model: 'gemini-3.8-flash',
        input: [{ role: 'user', content: [{ type: 'text', text: prompt }] }]
      })
    });
    const data = await res.json();
    console.log('GEMINI STATUS:', res.status, JSON.stringify(data).slice(0,1000));
    // Interactions API returns output array
    let text = '';
    if (data.outputs) text = data.outputs.map(o => (o.content||[]).map(c=>c.text||'').join('')).join('');
    if (!text && data.candidates) text = data.candidates?.[0]?.content?.parts?.[0]?.text || '';
    if (!text) text = data.output_text || data.text || '';
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'Empty: ' + JSON.stringify(data).slice(0,600) }) };
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text }) };
  } catch(e){ console.log('FUNCTION ERROR:', e.message); return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: e.message }) }; }
};
