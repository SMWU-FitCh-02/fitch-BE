"""
TJ미디어 인기차트 곡들의 음역대(최저음~최고음)를 오디오 분석으로 추출해서
FitCh 백엔드(/chart/vocal-ranges/bulk-upsert)에 등록하는 배치 스크립트.

이 버전은 보컬 분리(demucs)를 먼저 거친 뒤 분리된 보컬 트랙만으로 피치를
분석해서 반주/코러스 간섭을 줄인다. 대신 곡당 처리 시간이 꽤 늘어난다
(모델 로딩 + 분리 자체가 CPU에서 곡당 30초~2분 정도 걸릴 수 있음).

사전 설치 필요:
    pip install --break-system-packages -q demucs
"""

import os
import glob
import shutil
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

SONG_URL_OVERRIDES = {
    "바다의 왕자": "https://youtu.be/-kqdnx9qw28",
    "떠나가요, 떠나지마요": "https://youtu.be/Po2974noWzQ",
    "나에게로 떠나는 여행": "https://youtu.be/w7adx_CuApw",
    "버스 안에서": "https://youtu.be/R3DbYFoqGr8",
    "残酷な天使のテーゼ(新世紀エヴァンゲリオン OP)": "https://youtu.be/o6wtDPVkKqI",
}

QUERY_SUFFIXES = ["", "live", "노래방"]

# demucs 결과물이 쌓이는 폴더 (2-stems 모드: vocals / no_vocals)
DEMUCS_OUT_DIR = "separated"
DEMUCS_MODEL = "htdemucs"


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


def cleanup_demucs_output(basename: str):
    out_path = os.path.join(DEMUCS_OUT_DIR, DEMUCS_MODEL, basename)
    if os.path.isdir(out_path):
        shutil.rmtree(out_path, ignore_errors=True)


def download_audio_from(target: str, basename: str):
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


def separate_vocals(audio_path: str, basename: str):
    """demucs로 보컬만 분리해서 그 wav 경로를 반환. 실패하면 None."""
    # macOS에서 numpy/torch가 서로 다른 OpenMP 런타임을 로드하면서
    # SIGABRT로 죽는 경우가 많아, KMP_DUPLICATE_LIB_OK로 우회하고
    # MPS 대신 CPU를 강제 지정해서 안정성을 높인다.
    env = os.environ.copy()
    env["KMP_DUPLICATE_LIB_OK"] = "TRUE"
    try:
        subprocess.run(
            [
                "demucs", "--two-stems", "vocals",
                "-n", DEMUCS_MODEL,
                "-d", "cpu",
                "-o", DEMUCS_OUT_DIR,
                audio_path,
            ],
            check=True, capture_output=True, timeout=600, env=env,
        )
    except subprocess.CalledProcessError as e:
        stderr_tail = (e.stderr or b"").decode(errors="ignore")[-500:]
        print(f"  보컬 분리 실패, 원본으로 대체: {e}\n  stderr: {stderr_tail}")
        return None
    except Exception as e:
        print(f"  보컬 분리 실패, 원본으로 대체: {e}")
        return None

    # demucs는 입력 파일명(확장자 제외)으로 하위 폴더를 만든다
    stem_name = os.path.splitext(os.path.basename(audio_path))[0]
    vocals_path = os.path.join(DEMUCS_OUT_DIR, DEMUCS_MODEL, stem_name, "vocals.wav")
    return vocals_path if os.path.exists(vocals_path) else None


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


def try_extract(raw_audio_path: str, basename: str):
    # 1) 보컬 분리 먼저 시도
    vocals_path = separate_vocals(raw_audio_path, basename)
    analyze_path = vocals_path if vocals_path else raw_audio_path
    if vocals_path:
        print("  -> 보컬 분리 성공, 분리된 트랙으로 분석")
    else:
        print("  -> 보컬 분리 실패, 원본 오디오로 분석")

    try:
        r = estimate_range_midi(analyze_path)
    except Exception as e:
        print(f"  분석 실패: {e}")
        r = None
    finally:
        cleanup_temp(basename)
        cleanup_demucs_output(basename)
    return r


def analyze_one(artist: str, title: str, basename: str):
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
    print(f"분석 대상 {len(songs)}곡 불러옴 (보컬 분리 사용)")

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

        # 도중에 죽어도 여태까지 결과는 남도록 매 곡마다 저장
        with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
            json.dump(results, f, ensure_ascii=False, indent=2)

        time.sleep(1)

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