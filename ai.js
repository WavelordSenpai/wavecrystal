// WaveCrystal AI Brain - calls Gemini (FREE)
exports.handler = async (event) => {
  // Only allow POST
  if (event.httpMethod !== 'POST') {
    return { statusCode: 405, body: 'Method not allowed' };
  }

  try {
    const { prompt } = JSON.parse(event.body);
    const apiKey = process.env.GEMINI_API_KEY;

    if (!apiKey) {
      return {
        statusCode: 200,
        body: JSON.stringify({ answer: "AI key not set yet. Go to Netlify > Site Settings > Environment Variables > Add GEMINI_API_KEY. Get free key at aistudio.google.com" })
      };
    }

    // Call Gemini - FREE tier
    const res = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }]
        })
      }
    );

    const data = await res.json();
    const answer = data.candidates?.[0]?.content?.parts?.[0]?.text || "AI had no answer, try again.";

    return {
      statusCode: 200,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ answer })
    };

  } catch (e) {
    return { statusCode: 500, body: JSON.stringify({ answer: "AI error: " + e.message }) };
  }
};