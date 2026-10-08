"""
Video-to-Text & Confidence Scoring Agent using NVIDIA VSS (Video Search & Summarization) / NIM Multimodal APIs.

Pipeline:
1. Video Ingestion & Frame Sampling: Extracts key temporal frames across the video and generates a temporal filmstrip montage.
2. Speech-to-Text (Audio Transcription): Decodes spoken audio track using Whisper STT.
3. NVIDIA Multimodal NIM Reasoning: Evaluates visual posture, camera eye contact, speech delivery, clarity, and German visa/academic readiness.
4. Output: Structured JSON containing confidence scores (0-100), transcript, extracted candidate profile, visual feedback, and actionable interview tips.
"""

import os
import re
import cv2
import json
import base64
import shutil
import logging
from typing import Dict, Any, Optional, Tuple
import numpy as np
import httpx
from dotenv import load_dotenv

# Ensure environment variables are loaded
load_dotenv(r"D:\EduGerman\.env")
load_dotenv(os.path.join(os.path.dirname(__file__), ".env"))

logger = logging.getLogger("video_vss_agent")
logger.setLevel(logging.INFO)

# Setup ffmpeg path for whisper from imageio_ffmpeg
try:
    import imageio_ffmpeg
    ffmpeg_exe = imageio_ffmpeg.get_ffmpeg_exe()
    bin_dir = os.path.dirname(ffmpeg_exe)
    alias_exe = os.path.join(bin_dir, "ffmpeg.exe")
    if not os.path.exists(alias_exe):
        try:
            shutil.copyfile(ffmpeg_exe, alias_exe)
        except Exception:
            pass
    if bin_dir not in os.environ.get("PATH", ""):
        os.environ["PATH"] = bin_dir + os.pathsep + os.environ.get("PATH", "")
except Exception as e:
    logger.warning("Could not auto-configure ffmpeg from imageio_ffmpeg: %s", e)

# Whisper cached model instance
_WHISPER_MODEL = None


def get_whisper_model():
    global _WHISPER_MODEL
    if _WHISPER_MODEL is None:
        import whisper
        # Use tiny for ultra-fast response time on CPU
        _WHISPER_MODEL = whisper.load_model("tiny")
    return _WHISPER_MODEL


def extract_filmstrip(video_path: str, num_frames: int = 3, max_width: int = 360) -> Tuple[str, float]:
    """
    Extracts num_frames uniformly across the video duration, stamps timestamps,
    and stitches them into a horizontal filmstrip image.
    Returns: (base64_jpeg_string, video_duration_seconds)
    """
    if not os.path.exists(video_path):
        raise FileNotFoundError(f"Video file not found at: {video_path}")

    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        raise ValueError(f"Could not open video file: {video_path}")

    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    fps = cap.get(cv2.CAP_PROP_FPS) or 24.0
    duration = round(total_frames / fps, 1) if total_frames > 0 else 0.0

    if total_frames <= 0:
        cap.release()
        raise ValueError(f"Video file has no frames or is corrupted: {video_path}")

    # Generate sampling fractions
    fractions = np.linspace(0.15, 0.85, num_frames)
    frames = []

    for frac in fractions:
        frame_idx = min(int(total_frames * frac), total_frames - 1)
        cap.set(cv2.CAP_PROP_POS_FRAMES, frame_idx)
        ret, frame = cap.read()
        if ret and frame is not None:
            h, w = frame.shape[:2]
            target_w = max_width
            target_h = int(h * (target_w / w))
            resized = cv2.resize(frame, (target_w, target_h))
            
            # Timestamp indicator badge
            t_sec = round(frame_idx / fps, 1)
            cv2.putText(
                resized,
                f"{t_sec}s",
                (12, 28),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.75,
                (0, 255, 255),
                2,
                cv2.LINE_AA
            )
            frames.append(resized)

    cap.release()

    if not frames:
        raise ValueError("Failed to extract any readable frames from video.")

    # Stitch frames horizontally into a composite progression filmstrip
    strip = np.hstack(frames)
    encode_params = [int(cv2.IMWRITE_JPEG_QUALITY), 78]
    _, buf = cv2.imencode(".jpg", strip, encode_params)
    b64_str = base64.b64encode(buf).decode("utf-8")

    return b64_str, duration


def transcribe_video_audio(video_path: str) -> Dict[str, Any]:
    """
    Extracts and transcribes spoken audio from video using Whisper.
    """
    try:
        model = get_whisper_model()
        res = model.transcribe(video_path, fp16=False)
        text = (res.get("text") or "").strip()
        lang = res.get("language") or "en"
        return {
            "transcript": text if text else "(No clear speech detected)",
            "language": lang,
            "has_speech": bool(text)
        }
    except Exception as e:
        logger.warning("Whisper transcription failed or audio absent: %s", e)
        return {
            "transcript": "(Audio track could not be transcribed or is silent)",
            "language": "unknown",
            "has_speech": False
        }


def _extract_json_from_response(raw_text: str) -> Dict[str, Any]:
    """
    Robustly parses JSON or structured markdown from LLM/VLM response.
    """
    text = raw_text.strip()
    
    # 1. Check if enclosed in markdown code fences ```json ... ```
    match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", text)
    if match:
        candidate = match.group(1).strip()
        try:
            return json.loads(candidate)
        except Exception:
            pass

    # 2. Look for first { and last }
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end != -1 and end > start:
        json_slice = text[start:end + 1]
        try:
            return json.loads(json_slice)
        except Exception:
            pass

    # 3. Parse Markdown report structured sections if LLM answered in formatted markdown
    data: Dict[str, Any] = {}
    
    # Extract scores
    m_conf = re.search(r"Confidence(?:\s*Score)?\s*[:*]*\s*(\d{1,3})", text, re.I)
    m_flue = re.search(r"Fluency[^\n\d]*?(\d{1,3})", text, re.I)
    m_visa = re.search(r"Visa[^\n\d]*?(\d{1,3})", text, re.I)
    data["confidence_score"] = int(m_conf.group(1)) if m_conf else 65
    data["fluency_score"] = int(m_flue.group(1)) if m_flue else 60
    data["visa_readiness_score"] = int(m_visa.group(1)) if m_visa else 55

    m_rating = re.search(r"Overall Rating\s*[:*]*\s*([^\n\r*]+)", text, re.I)
    data["overall_rating"] = m_rating.group(1).strip() if m_rating else "Needs Practice"

    def get_section_bullets(heading: str) -> list:
        m = re.search(rf"\*\*{heading}[^\n]*\*\*([\s\S]*?)(?=\n\s*\*\*|\Z)", text, re.I)
        if not m:
            return []
        items = re.findall(r"^\s*[*•-]\s*(.+)$", m.group(1), re.M)
        return [it.strip() for it in items if it.strip()]

    def get_section_kv(heading: str) -> dict:
        m = re.search(rf"\*\*{heading}[^\n]*\*\*([\s\S]*?)(?=\n\s*\*\*|\Z)", text, re.I)
        if not m:
            return {}
        d = {}
        for line in m.group(1).splitlines():
            kv = re.match(r"^\s*[*•-]?\s*([^:]+):\s*(.+)$", line)
            if kv:
                k = kv.group(1).replace("*", "").strip().lower().replace(" ", "_")
                d[k] = kv.group(2).replace("*", "").strip()
        return d

    data["candidate_profile"] = get_section_kv("Candidate Profile")
    data["visual_analysis"] = get_section_kv("Visual Analysis")
    data["speech_analysis"] = get_section_kv("Speech Delivery")
    data["strengths"] = get_section_bullets("Strengths") or ["Clear visual effort"]
    data["actionable_improvements"] = get_section_bullets("Actionable Improvements") or [
        "Establish steady eye contact with the camera",
        "Prepare a structured self-introduction with motivation for Germany",
        "Record in a quiet room with good front lighting"
    ]
    data["raw_analysis"] = raw_text
    return data


def analyze_video_introduction(video_path: str, user_context: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
    """
    Full Video-to-Text & Confidence Scoring Agent pipeline using NVIDIA VSS Multimodal NIM.
    
    Returns structured analysis with confidence score, fluency score,
    speech transcript, visual posture review, and interview readiness.
    """
    api_key = os.environ.get("NVIDIA_API_KEY", "").strip()
    if not api_key:
        raise ValueError("NVIDIA_API_KEY is not set in environment or .env file.")

    model_name = os.environ.get("NVIDIA_VSS_MODEL", "meta/llama-3.2-11b-vision-instruct")
    
    # 1. Extract filmstrip frames
    filmstrip_b64, duration = extract_filmstrip(video_path, num_frames=3)

    # 2. Transcribe speech audio
    audio_info = transcribe_video_audio(video_path)
    transcript = audio_info["transcript"]
    detected_lang = audio_info["language"]

    # 3. Formulate admissions & interview coaching prompt
    candidate_hint = ""
    if user_context:
        candidate_hint = f"Candidate Context: Field: {user_context.get('course', 'Unknown')}, CGPA: {user_context.get('cgpa', 'Unknown')}.\n"

    system_instruction = (
        "You are Voraus AI's Admissions & Student Visa Video Assessment Agent, specialized in German university admissions "
        "(TU9, UAS) and German Embassy Student Visa interviews. "
        "Analyze the candidate's self-introduction video comprehensively."
    )

    prompt = f"""{candidate_hint}Video Analysis Parameters:
- Video Duration: {duration} seconds
- Transcribed Spoken Audio: "{transcript}"
- Visual Progression: Attached 3-frame sequential filmstrip (showing progression across start, middle, and end of video).

Please perform a comprehensive evaluation:
1. **Confidence Score (0-100)**: Eye contact with lens, stability, composure, hesitation, nervousness, assertiveness.
2. **Fluency & Communication Score (0-100)**: Speaking pace, clarity of articulation, structure of introduction.
3. **Visa & University Readiness Score (0-100)**: Professionalism of setting, attire, presentation quality for German professors or visa officers.
4. **Candidate Profile Extraction**: Extract Name, Desired Degree/Major in Germany, and Core Motivation (or state 'Not mentioned' if missing).
5. **Visual Analysis**: Evaluate eye contact, posture/body language, lighting, and attire.
6. **Speech Delivery**: Evaluate clarity, delivery tone, and effectiveness of the spoken message.
7. **Actionable Recommendations**: 3-4 concrete tips for German university and visa interview success.

Respond ONLY with valid JSON following this exact structure:
{{
  "confidence_score": <integer 0-100>,
  "fluency_score": <integer 0-100>,
  "visa_readiness_score": <integer 0-100>,
  "overall_rating": "Needs Practice" | "Good" | "Strong" | "Interview Ready",
  "candidate_profile": {{
    "name": "string",
    "target_degree": "string",
    "field_of_study": "string",
    "extracted_motivation": "string"
  }},
  "visual_analysis": {{
    "eye_contact": "string",
    "body_language_and_posture": "string",
    "environment_and_lighting": "string",
    "attire_and_professionalism": "string"
  }},
  "speech_analysis": {{
    "clarity_and_pacing": "string",
    "language_proficiency": "string",
    "delivery_summary": "string"
  }},
  "strengths": ["string", "string"],
  "actionable_improvements": ["string", "string", "string"]
}}"""

    payload = {
        "model": model_name,
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "image_url",
                        "image_url": {"url": f"data:image/jpeg;base64,{filmstrip_b64}"}
                    },
                    {
                        "type": "text",
                        "text": f"{system_instruction}\n\n{prompt}"
                    }
                ]
            }
        ],
        "temperature": 0.1,
        "max_tokens": 1200
    }

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    with httpx.Client(timeout=65.0) as client:
        response = client.post(
            "https://integrate.api.nvidia.com/v1/chat/completions",
            headers=headers,
            json=payload
        )

    if response.status_code != 200:
        logger.error("NVIDIA API failed (%d): %s", response.status_code, response.text)
        raise RuntimeError(f"NVIDIA API Error ({response.status_code}): {response.text}")

    resp_json = response.json()
    raw_content = resp_json["choices"][0]["message"]["content"]

    # Parse JSON
    result = _extract_json_from_response(raw_content)

    # Attach video metadata & transcript to output
    result["transcript"] = transcript
    result["detected_language"] = detected_lang
    result["duration_seconds"] = duration
    result["model_used"] = model_name

    # Ensure required score keys exist with valid integer values
    result["confidence_score"] = int(result.get("confidence_score") or 65)
    result["fluency_score"] = int(result.get("fluency_score") or 60)
    result["visa_readiness_score"] = int(result.get("visa_readiness_score") or 55)

    return result


if __name__ == "__main__":
    sample_vid = r"D:\EduGerman\VID-20261008-WA0009.mp4"
    if os.path.exists(sample_vid):
        print("Running Video VSS Agent on sample video...")
        analysis = analyze_video_introduction(sample_vid)
        print("=" * 60)
        print("AGENT ANALYSIS RESULT:")
        print(json.dumps(analysis, indent=2))
        print("=" * 60)
    else:
        print("Sample video not found.")
