"""Pristojan HTTP klijent: robots.txt, najviše 1 zahtjev/s po domeni, lokalni cache.

Svi izvori dijele jedan klijent (`get_client()`), pa se ograničenje brzine
poštuje i kad se izvori izvode paralelno u više dretvi.
"""
import hashlib
import threading
import time
import urllib.robotparser
from collections import defaultdict
from urllib.parse import urlsplit

import httpx

import config


class Blocked(Exception):
    """URL je zabranjen u robots.txt."""


class HttpClient:
    def __init__(self, refresh: bool = False):
        self.refresh = refresh
        self._client = httpx.Client(
            headers={"User-Agent": config.USER_AGENT, "Accept-Language": "hr,en;q=0.5"},
            follow_redirects=True,
            timeout=40,
        )
        self._robots: dict[str, urllib.robotparser.RobotFileParser | None] = {}
        self._domain_locks: dict[str, threading.Lock] = defaultdict(threading.Lock)
        self._last: dict[str, float] = {}
        self._meta_lock = threading.Lock()
        self.stats: dict[str, dict] = defaultdict(lambda: {"zahtjeva": 0, "cache": 0, "greske": 0, "robots_blok": 0})
        # isto po dretvi: run_all izvodi svaki izvor u svojoj dretvi, pa je to brojač po izvoru
        self.thread_stats: dict[str, dict] = defaultdict(lambda: {"zahtjeva": 0, "cache": 0})

    # --- robots.txt -------------------------------------------------------
    def _robots_for(self, url: str):
        parts = urlsplit(url)
        base = f"{parts.scheme}://{parts.netloc}"
        with self._meta_lock:
            if base in self._robots:
                return self._robots[base]
        rp = urllib.robotparser.RobotFileParser()
        try:
            r = self._raw_get(base + "/robots.txt")
            if r.status_code == 200:
                rp.parse(r.text.splitlines())
            else:
                rp = None          # nema robots.txt -> sve dopušteno
        except httpx.HTTPError:
            rp = None
        with self._meta_lock:
            self._robots[base] = rp
        return rp

    def allowed(self, url: str) -> bool:
        rp = self._robots_for(url)
        return rp is None or rp.can_fetch(config.USER_AGENT, url)

    # --- rate limit -------------------------------------------------------
    def _raw_get(self, url: str, **kw) -> httpx.Response:
        domain = urlsplit(url).netloc
        with self._domain_locks[domain]:
            wait = self._last.get(domain, 0) + config.MIN_INTERVAL_S - time.monotonic()
            if wait > 0:
                time.sleep(wait)
            try:
                return self._client.get(url, **kw)
            finally:
                self._last[domain] = time.monotonic()
                self.stats[domain]["zahtjeva"] += 1
                self.thread_stats[threading.current_thread().name]["zahtjeva"] += 1

    # --- cache ------------------------------------------------------------
    @staticmethod
    def cache_path(url: str, ext: str):
        domain = urlsplit(url).netloc.replace(":", "_")
        h = hashlib.sha1(url.encode()).hexdigest()[:20]
        return config.CACHE / domain / f"{h}{ext}"

    def get_bytes(self, url: str, ext: str = ".html", ttl_h: float | None = None) -> bytes:
        """Vraća tijelo odgovora; iz cachea ako postoji i nije starije od ttl_h (None = zauvijek)."""
        domain = urlsplit(url).netloc
        path = self.cache_path(url, ext)
        if path.exists() and not (self.refresh and ttl_h is not None):
            age_h = (time.time() - path.stat().st_mtime) / 3600
            if ttl_h is None or age_h < ttl_h:
                self.stats[domain]["cache"] += 1
                self.thread_stats[threading.current_thread().name]["cache"] += 1
                return path.read_bytes()
        if not self.allowed(url):
            self.stats[domain]["robots_blok"] += 1
            raise Blocked(url)
        last_exc = None
        for attempt in range(3):
            try:
                r = self._raw_get(url)
                if r.status_code in (429, 502, 503, 504):
                    last_exc = httpx.HTTPStatusError(f"{r.status_code}", request=r.request, response=r)
                    time.sleep(3 * (attempt + 1))
                    continue
                r.raise_for_status()
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(r.content)
                return r.content
            except (httpx.TransportError,) as e:
                last_exc = e
                time.sleep(2 * (attempt + 1))
            except httpx.HTTPStatusError:
                self.stats[domain]["greske"] += 1
                raise
        self.stats[domain]["greske"] += 1
        raise last_exc

    def get_text(self, url: str, ttl_h: float | None = None) -> str:
        data = self.get_bytes(url, ".html", config.HTML_CACHE_TTL_H if ttl_h is None else ttl_h)
        return decode(data)

    def get_json(self, url: str):
        import json
        return json.loads(decode(self.get_bytes(url, ".json", config.HTML_CACHE_TTL_H)))

    def get_pdf(self, url: str) -> bytes:
        return self.get_bytes(url, ".pdf", None)


def decode(data: bytes) -> str:
    for enc in ("utf-8", "cp1250"):
        try:
            return data.decode(enc)
        except UnicodeDecodeError:
            continue
    return data.decode("utf-8", errors="replace")


_client: HttpClient | None = None


def get_client() -> HttpClient:
    global _client
    if _client is None:
        _client = HttpClient()
    return _client


def set_refresh(refresh: bool):
    get_client().refresh = refresh
