exports.handler = async (event) => {
  const headers = {
    'Access-Control-Allow-Origin': '*',
    'Content-Type': 'application/json'
  };
  try {
    const body = JSON.parse(event.body || '{}');
    const prompt = body.prompt || body.message || body.text || body.question || '';
    if (!prompt) return { statusCode: 400, headers, body: JSON.stringify({ answer: '', text: '', reply: '', error: 'No prompt' }) };

    const key = process.env.GEMINI_API_KEY;
    if (!key) {
      console.log('NO KEY FOUND');
      return { statusCode: 500, headers, body: JSON.stringify({ answer: '', text: '', reply: '', error: 'No API key' }) };
    }

    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${key}`;
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ contents: [{ parts: [{ text: prompt }] }] })
    });
    const data = await res.json();
    console.log('GEMINI STATUS:', res.status, JSON.stringify(data).slice(0,500));
    
    const text = data?.candidates?.[0]?.content?.parts?.[0]?.text || '';
    
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', text: '', reply: '', error: 'Gemini returned empty: ' + JSON.stringify(data).slice(0,500) }) };

    // Return ALL field names so ANY frontend code will find it
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text, result: text }) };
  } catch (e) {
    console.log('FUNCTION ERROR:', e.message);
    return { statusCode: 500, headers, body: JSON.stringify({ answer: '', text: '', reply: '', error: e.message }) };
  }
};
