exports.handler = async (event) => {
  const headers = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'Content-Type', 'Access-Control-Allow-Methods': 'GET,POST,OPTIONS', 'Content-Type': 'application/json' };
  if (event.httpMethod === 'OPTIONS') return { statusCode: 200, headers, body: '{}' };
  try {
    let prompt = '';
    if (event.queryStringParameters) prompt = event.queryStringParameters.prompt || event.queryStringParameters.message || event.queryStringParameters.q || '';
    if (event.body) { try { const b = JSON.parse(event.body); prompt = prompt || b.prompt || b.message || b.text || b.question || b.input || ''; } catch(e){ prompt = prompt || event.body; } }
    if (!prompt) return { statusCode: 400, headers, body: JSON.stringify({ answer: '', error: 'No prompt' }) };
    const key = process.env.GROQ_API_KEY;
    if (!key) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'No GROQ_API_KEY - add it in Netlify' }) };
    const res = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${key}` },
      body: JSON.stringify({ model: 'llama-3.3-70b-versatile', messages: [{ role: 'user', content: prompt }], temperature: 0.7, max_tokens: 1000 })
    });
    const data = await res.json();
    console.log('GROQ STATUS:', res.status, JSON.stringify(data).slice(0,800));
    const text = data?.choices?.[0]?.message?.content || '';
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'Empty: ' + JSON.stringify(data).slice(0,600) }) };
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text }) };
  } catch(e){ console.log('FUNCTION ERROR:', e.message); return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: e.message }) }; }
};
