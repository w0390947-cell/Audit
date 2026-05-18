import base64
import io
import json
import logging
import os
import tempfile
from typing import Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel


app = FastAPI(title="Audit OCR Service")
logger = logging.getLogger("audit-ocr-service")


def load_env_file(path: str = "config.env"):
    if not os.path.exists(path):
        return
    with open(path, "r", encoding="utf-8") as file:
        for line in file:
            stripped = line.strip()
            if not stripped or stripped.startswith("#") or "=" not in stripped:
                continue
            key, value = stripped.split("=", 1)
            os.environ.setdefault(key.strip(), value.strip().strip("\"'"))


load_env_file()
OCR_PROVIDER = os.getenv("OCR_PROVIDER", "paddle").strip().lower()
ALIYUN_OCR_API = os.getenv("ALIYUN_OCR_API", "general").strip().lower()
ALIYUN_OCR_ENDPOINT = os.getenv("ALIYUN_OCR_ENDPOINT", "ocr-api.cn-hangzhou.aliyuncs.com").strip()
ALIYUN_CONNECT_TIMEOUT = int(os.getenv("ALIYUN_CONNECT_TIMEOUT", "600000"))
ALIYUN_READ_TIMEOUT = int(os.getenv("ALIYUN_READ_TIMEOUT", "600000"))
ALIYUN_IMAGE_MAX_SIDE = int(os.getenv("ALIYUN_IMAGE_MAX_SIDE", "2200"))
ALIYUN_IMAGE_MAX_BYTES = int(os.getenv("ALIYUN_IMAGE_MAX_BYTES", str(5 * 1024 * 1024)))
ALIYUN_JPEG_QUALITY = int(os.getenv("ALIYUN_JPEG_QUALITY", "85"))
_paddle_ocr = None
_aliyun_client = None


class OcrRequest(BaseModel):
    image_base64: str | None = None
    image: str | None = None
    page_no: int | None = None


def create_paddle_ocr():
    from paddleocr import PaddleOCR

    try:
        return PaddleOCR(
            use_doc_orientation_classify=False,
            use_doc_unwarping=False,
            use_textline_orientation=False,
            engine="paddle",
        )
    except TypeError:
        return PaddleOCR(use_angle_cls=True, lang="ch", show_log=False)


def decode_image(payload: OcrRequest) -> bytes:
    value = payload.image_base64 or payload.image
    if not value:
        raise HTTPException(status_code=400, detail="image_base64 is required")
    if "," in value:
        value = value.split(",", 1)[1]
    try:
        return base64.b64decode(value)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"invalid base64: {exc}")


def collect_text(value: Any, texts: list[str]):
    if value is None:
        return

    if isinstance(value, dict):
        for key in ("rec_texts", "texts"):
            items = value.get(key)
            if isinstance(items, list):
                texts.extend(str(item) for item in items if str(item).strip())

        for key in ("rec_text", "text", "content", "ocr_text"):
            item = value.get(key)
            if isinstance(item, str) and item.strip():
                texts.append(item)

        for key in ("res", "data", "result", "results", "overall_ocr_res"):
            collect_text(value.get(key), texts)
        return

    if isinstance(value, (list, tuple)):
        if len(value) >= 2 and isinstance(value[1], (list, tuple)) and value[1]:
            if isinstance(value[1][0], str) and value[1][0].strip():
                texts.append(value[1][0])
                return
        for item in value:
            collect_text(item, texts)
        return

    if hasattr(value, "to_dict"):
        collect_text(value.to_dict(), texts)
        return


def safe_remove(path: str | None):
    if not path or not os.path.exists(path):
        return
    try:
        os.remove(path)
    except OSError as exc:
        logger.warning("temporary file cleanup skipped: %s (%s)", path, exc)


def write_aliyun_image(image_bytes: bytes) -> str:
    from PIL import Image

    with Image.open(io.BytesIO(image_bytes)) as image:
        image = image.convert("RGB")
        max_side = max(800, ALIYUN_IMAGE_MAX_SIDE)
        if max(image.size) > max_side:
            image.thumbnail((max_side, max_side))

        quality = max(40, min(95, ALIYUN_JPEG_QUALITY))
        while True:
            output = io.BytesIO()
            image.save(output, format="JPEG", quality=quality, optimize=True)
            data = output.getvalue()
            if len(data) <= ALIYUN_IMAGE_MAX_BYTES or quality <= 45:
                break
            quality -= 10

    with tempfile.NamedTemporaryFile(delete=False, suffix=".jpg") as tmp:
        tmp.write(data)
        logger.info("aliyun ocr image prepared: %s bytes, quality=%s", len(data), quality)
        return tmp.name


def run_paddle_ocr(image_bytes: bytes) -> str:
    global _paddle_ocr
    if _paddle_ocr is None:
        _paddle_ocr = create_paddle_ocr()

    tmp_path = None
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=".png") as tmp:
            tmp.write(image_bytes)
            tmp_path = tmp.name

        if hasattr(_paddle_ocr, "predict"):
            result = _paddle_ocr.predict(tmp_path)
        else:
            result = _paddle_ocr.ocr(tmp_path, cls=True)

        texts: list[str] = []
        collect_text(result, texts)
        return "\n".join(texts).strip()
    finally:
        safe_remove(tmp_path)


def get_aliyun_client():
    global _aliyun_client
    if _aliyun_client is not None:
        return _aliyun_client

    access_key_id = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID") or os.getenv("ALIYUN_ACCESS_KEY_ID")
    access_key_secret = os.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET") or os.getenv("ALIYUN_ACCESS_KEY_SECRET")
    security_token = os.getenv("ALIBABA_CLOUD_SECURITY_TOKEN") or os.getenv("ALIYUN_SECURITY_TOKEN")
    if not access_key_id or not access_key_secret:
        raise RuntimeError("Aliyun OCR credentials are not configured")

    from alibabacloud_ocr_api20210707.client import Client as OcrClient
    from alibabacloud_tea_openapi import models as open_api_models

    config = open_api_models.Config(
        access_key_id=access_key_id,
        access_key_secret=access_key_secret,
        security_token=security_token,
    )
    config.endpoint = ALIYUN_OCR_ENDPOINT
    _aliyun_client = OcrClient(config)
    return _aliyun_client


def run_aliyun_ocr(image_bytes: bytes) -> str:
    from alibabacloud_darabonba_stream.client import Client as StreamClient
    from alibabacloud_ocr_api20210707 import models as ocr_models
    from alibabacloud_tea_util import models as util_models

    tmp_path = None
    try:
        tmp_path = write_aliyun_image(image_bytes)

        body = StreamClient.read_from_file_path(tmp_path)
        client = get_aliyun_client()
        runtime = util_models.RuntimeOptions()
        runtime.connect_timeout = ALIYUN_CONNECT_TIMEOUT
        runtime.read_timeout = ALIYUN_READ_TIMEOUT
        if ALIYUN_OCR_API == "advanced":
            request = ocr_models.RecognizeAdvancedRequest(body=body)
            response = client.recognize_advanced_with_options(request, runtime)
        else:
            request = ocr_models.RecognizeGeneralRequest(body=body)
            response = client.recognize_general_with_options(request, runtime)

        response_body = response.body.to_map() if hasattr(response.body, "to_map") else response.body
        texts: list[str] = []
        collect_aliyun_text(response_body, texts)
        return "\n".join(texts).strip()
    finally:
        safe_remove(tmp_path)


def collect_aliyun_text(value: Any, texts: list[str]):
    if value is None:
        return
    if hasattr(value, "to_map"):
        value = value.to_map()
    if isinstance(value, dict):
        code = value.get("Code")
        message = value.get("Message")
        if code:
            raise RuntimeError(f"Aliyun OCR failed: {code} {message or ''}".strip())
        data = value.get("Data") or value.get("data")
        if isinstance(data, str):
            try:
                data = json.loads(data)
            except json.JSONDecodeError:
                if data.strip():
                    texts.append(data.strip())
                return
        collect_aliyun_text(data, texts)
        for key in ("content", "text", "word"):
            item = value.get(key)
            if isinstance(item, str) and item.strip():
                texts.append(item.strip())
        for key in ("prism_wordsInfo", "prism_words_info", "wordsInfo", "words_info", "results"):
            collect_aliyun_text(value.get(key), texts)
        return
    if isinstance(value, (list, tuple)):
        for item in value:
            collect_aliyun_text(item, texts)
        return
    if isinstance(value, str) and value.strip():
        texts.append(value.strip())


def run_ocr(image_bytes: bytes) -> str:
    if OCR_PROVIDER in ("aliyun", "ali", "cloud"):
        return run_aliyun_ocr(image_bytes)
    if OCR_PROVIDER in ("paddle", "local"):
        return run_paddle_ocr(image_bytes)
    raise RuntimeError(f"Unsupported OCR_PROVIDER: {OCR_PROVIDER}")


@app.get("/health")
def health():
    return {
        "status": "ok",
        "provider": OCR_PROVIDER,
        "aliyun_api": ALIYUN_OCR_API if OCR_PROVIDER in ("aliyun", "ali", "cloud") else None,
    }


@app.post("/ocr")
def recognize(payload: OcrRequest):
    try:
        image_bytes = decode_image(payload)
        text = run_ocr(image_bytes)
        return {
            "page_no": payload.page_no,
            "provider": OCR_PROVIDER,
            "text": text,
        }
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=502, detail=str(exc))
