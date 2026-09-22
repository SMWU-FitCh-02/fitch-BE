"""
TJ미디어 인기차트 곡들의 음역대(최저음~최고음)를 오디오 분석으로 추출해서
FitCh 백엔드(/chart/vocal-ranges/bulk-upsert)에 등록하는 배치 스크립트.
"""

import os
import glob
import subprocess
import json
import time
import numpy as np
import librosa
import requests

# ====== 설정 (본인 값으로 수정) ======
CHART_API = "https://fitch-fe.vercel.app/api/tjchart?limit=100"
API_BASE = "https://brian-alabama-quarters-promises.trycloudflare.com"
LOGIN_USERNAME = "im3zero"
LOGIN_PASSWORD = "00000000"
OUTPUT_JSON = "vocal_ranges_result.json"
TEST_LIMIT = None

# 빈 set() = 필터링 없이 TJ차트 100곡 전체를 돌림.
RETRY_ONLY_TITLES = set()

# 검색으로 계속 실패하는 곡은 유튜브 링크를 직접 지정해서 검색을 건너뛰고
# 그 URL에서 바로 다운로드한다. 키는 TJ차트 title과 정확히 같아야 매칭됨.
SONG_URL_OVERRIDES = {
    "바다의 왕자": "https://youtu.be/-kqdnx9qw28",
    "떠나가요, 떠나지마요": "https://youtu.be/Po2974noWzQ",
    "나에게로 떠나는 여행": "https://youtu.be/w7adx_CuApw",
    "버스 안에서": "https://youtu.be/R3DbYFoqGr8",
    "残酷な天使のテーゼ(新世紀エヴァンゲリオン OP)": "https://youtu.be/o6wtDPVkKqI",
}

# 직접 URL이 없는 곡에 한해, 한 곡당 순서대로 시도해볼 검색어 접미사.
# (원곡 -> 라이브 -> 노래방 순. "노래방"을 먼저 넣으면 MR만 잡히는 경우가 많아 뒤로 뺌)
QUERY_SUFFIXES = ["", "live", "노래방"]


def fetch_chart_songs():
    res = requests.get(CHART_API)
    res.raise_for_status()
    items = res.json()["items"]
    songs = [{"title": it["title"], "artist": it["singer"]} for it in items]

    if RETRY_ONLY_TITLES:
        before = len(songs)
        songs = [s for s in songs if s["title"] in RETRY_ONLY_TITLES]
        found_titles = {s["title"] for s in songs}
        missing = RETRY_ONLY_TITLES - found_titles
        if missing:
            print(f"  ! 차트에서 제목이 매칭 안 된 곡 {len(missing)}개: {missing}")
        print(f"재시도 대상 {len(songs)}곡 (전체 {before}곡 중 필터링)")

    if TEST_LIMIT:
        songs = songs[:TEST_LIMIT]
    return songs


def get_token():
    res = requests.post(f"{API_BASE}/auth/login", json={
        "username": LOGIN_USERNAME, "password": LOGIN_PASSWORD
    })
    res.raise_for_status()
    return res.json()["accessToken"]


def cleanup_temp(basename: str):
    for f in glob.glob(f"{basename}*"):
        try:
            os.remove(f)
        except OSError:
            pass


def download_audio_from(target: str, basename: str):
    """target은 검색어(ytsearch1:...) 또는 직접 URL 둘 다 될 수 있음."""
    cleanup_temp(basename)
    try:
        subprocess.run([
            "yt-dlp", "-x", "--audio-format", "wav",
            "--no-playlist", "--force-overwrites",
            "-o", f"{basename}.%(ext)s",
            target
        ], check=True, capture_output=True, timeout=120)
        matches = glob.glob(f"{basename}.wav")
        return matches[0] if matches else None
    except Exception as e:
        print(f"  다운로드 실패: {e}")
        return None


def estimate_range_midi(audio_path: str):
    y, sr = librosa.load(audio_path, sr=22050)
    f0, voiced_flag, voiced_probs = librosa.pyin(
        y, fmin=librosa.note_to_hz("C3"), fmax=librosa.note_to_hz("C6")
    )

    mask = voiced_flag & (voiced_probs >= 0.5) & ~np.isnan(f0)
    voiced_f0 = f0[mask]
    print(f"  (유효 발성 프레임: {len(voiced_f0)}개)")
    if len(voiced_f0) < 10:
        return None

    low_hz = np.percentile(voiced_f0, 10)
    high_hz = np.percentile(voiced_f0, 90)

    min_note = int(round(librosa.hz_to_midi(low_hz)))
    max_note = int(round(librosa.hz_to_midi(high_hz)))
    return min_note, max_note


def try_extract(audio_path: str, basename: str):
    try:
        r = estimate_range_midi(audio_path)
    except Exception as e:
        print(f"  분석 실패: {e}")
        r = None
    finally:
        cleanup_temp(basename)
    return r


def analyze_one(artist: str, title: str, basename: str):
    # 1) 직접 지정한 URL이 있으면 검색 없이 그걸로만 시도
    override_url = SONG_URL_OVERRIDES.get(title)
    if override_url:
        print(f"  지정된 URL로 다운로드: {override_url}")
        audio_path = download_audio_from(override_url, basename)
        if audio_path:
            r = try_extract(audio_path, basename)
            if r:
                return r
            print("  -> 지정 URL로도 분석 결과 없음")
        return None

    # 2) 검색어를 바꿔가며 순서대로 시도
    for suffix in QUERY_SUFFIXES:
        query = f"{artist} {title} {suffix}".strip()
        print(f"  검색: \"{query}\"")
        audio_path = download_audio_from(f"ytsearch1:{query}", basename)
        if not audio_path:
            continue
        r = try_extract(audio_path, basename)
        if r:
            return r
        print("  -> 이 검색어로는 결과 없음, 다음 검색어로 재시도")
    return None


def main():
    songs = fetch_chart_songs()
    print(f"분석 대상 {len(songs)}곡 불러옴")

    results = []
    for i, s in enumerate(songs, 1):
        title, artist = s["title"], s["artist"]
        basename = f"temp_audio_{i}"
        print(f"[{i}/{len(songs)}] {artist} - {title}")

        r = analyze_one(artist, title, basename)

        if r:
            min_note, max_note = r
            print(f"  -> minNote={min_note}, maxNote={max_note}")
            results.append({
                "title": title, "artist": artist,
                "minNote": min_note, "maxNote": max_note
            })
        else:
            print("  -> 모든 방법으로도 분석 결과 없음 (건너뜀)")

        time.sleep(1)

    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"\n총 {len(results)}곡 분석 완료 -> {OUTPUT_JSON}")

    upload = input("백엔드에 바로 업로드할까요? (y/n): ")
    if upload.lower() == "y":
        token = get_token()
        res = requests.post(
            f"{API_BASE}/chart/vocal-ranges/bulk-upsert",
            headers={"Authorization": f"Bearer {token}"},
            json=results
        )
        print(res.status_code, res.text)


if __name__ == "__main__":
    main()