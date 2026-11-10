const CACHE_NAME = 'wave-crystal-v6-upgrade';
const urlsToCache = ['/', '/index.html', '/manifest.json', '/icon.png'];
self.addEventListener('install', (event) => { self.skipWaiting(); event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(urlsToCache))); });
self.addEventListener('activate', (event) => { event.waitUntil(caches.keys().then((cacheNames) => Promise.all(cacheNames.map((c) => { if (c !== CACHE_NAME) return caches.delete(c); }))).then(() => self.clients.claim())); });
self.addEventListener('fetch', (event) => {
  if (event.request.url.includes('index.html') || event.request.mode === 'navigate') {
    event.respondWith(fetch(event.request).then((r) => { const cl = r.clone(); caches.open(CACHE_NAME).then((ca) => ca.put(event.request, cl)); return r; }).catch(() => caches.match(event.request)));
    return;
  }
  event.respondWith(caches.match(event.request).then((r) => r || fetch(event.request)));
});
