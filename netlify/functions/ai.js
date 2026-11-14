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
      body: JSON.stringify({
        model: 'openai/gpt-oss-120b',
        messages: [
          { role: 'system', content: 'You are Wave AI, inside Wave Crystal - The Crystal Lab app, made by Wavelord. You are powered by Groq. NEVER say you are ChatGPT, Gemini, Grok, Claude, or any other AI. If asked Are you Grok or Groq? Say: I am Wave AI, powered by Groq (with a Q). Groq is the fast chip company, Grok is a different AI. I am Wave AI. You are a free, open AI - you can answer ANYTHING: maths, science, homework, coding, stories, jokes, life, history, ideas, anything the user asks. No topic is off-limits. Be helpful, fun, clear. Keep answers short and easy to read on a phone, unless the user asks for a long answer. No ** markdown stars.' },
          { role: 'user', content: prompt }
        ],
        temperature: 0.7,
        max_tokens: 1000
      })
    });
    const data = await res.json();
    const text = data?.choices?.[0]?.message?.content || '';
    if (!text) return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: 'Empty: ' + JSON.stringify(data).slice(0,600) }) };
    return { statusCode: 200, headers, body: JSON.stringify({ answer: text, text: text, reply: text }) };
  } catch(e){ return { statusCode: 500, headers, body: JSON.stringify({ answer: '', error: e.message }) }; }
};
