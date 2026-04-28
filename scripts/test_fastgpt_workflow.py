#!/usr/bin/env python3
"""
FastGPT workflow connectivity test.

Reads FastGPT settings from:
backend/ruoyi-admin/src/main/resources/application.yml

Usage:
  python3 scripts/test_fastgpt_workflow.py "测试主题"
  python3 scripts/test_fastgpt_workflow.py "测试主题" --file-url "http://host/report.pdf"
  python3 scripts/test_fastgpt_workflow.py "测试主题" --file-url "http://host/report.pdf" --print-request
  python3 scripts/test_fastgpt_workflow.py "测试主题" --raw-only
  python3 scripts/test_fastgpt_workflow.py
"""

import argparse
import json
import os
import re
import sys
import urllib.error
import urllib.request
from urllib.parse import urlparse, unquote
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONFIG = PROJECT_ROOT / "backend/ruoyi-admin/src/main/resources/application.yml"


def truncate(text, max_length=2000):
    if text is None:
        return ""
    if len(text) <= max_length:
        return text
    return text[:max_length] + f"\n... <truncated, total {len(text)} chars>"


def parse_scalar(value):
    value = value.strip()
    if not value:
        return ""
    if value[0:1] in ("'", '"') and value[-1:] == value[0]:
        value = value[1:-1]
    if value.lower() == "true":
        return True
    if value.lower() == "false":
        return False
    if re.fullmatch(r"-?\d+", value):
        return int(value)
    return value


def load_fastgpt_config(config_path):
    if not config_path.exists():
        raise FileNotFoundError(f"配置文件不存在: {config_path}")

    lines = config_path.read_text(encoding="utf-8").splitlines()
    config = {}
    in_fastgpt = False
    fastgpt_indent = None

    for raw_line in lines:
        line_without_comment = raw_line.split("#", 1)[0].rstrip()
        if not line_without_comment.strip():
            continue

        indent = len(line_without_comment) - len(line_without_comment.lstrip(" "))
        stripped = line_without_comment.strip()

        if stripped == "fastgpt:":
            in_fastgpt = True
            fastgpt_indent = indent
            continue

        if in_fastgpt:
            if indent <= fastgpt_indent:
                break
            if ":" not in stripped:
                continue
            key, value = stripped.split(":", 1)
            config[key.strip()] = resolve_placeholder(parse_scalar(value))

    return config


def resolve_placeholder(value):
    if not isinstance(value, str):
        return value

    match = re.fullmatch(r"\$\{([^}:]+)(?::([^}]*))?\}", value)
    if not match:
        return value

    env_name = match.group(1)
    default = match.group(2) or ""
    return os.environ.get(env_name, default)


def file_name_from_url(file_url):
    path = unquote(urlparse(file_url).path)
    name = Path(path).name
    return name or "report.pdf"


def validate_file_url(file_url):
    if not file_url:
        return ""

    parsed = urlparse(file_url)
    if parsed.scheme not in ("http", "https"):
        raise ValueError(
            "文档必须使用 FastGPT 服务可访问的 HTTP/HTTPS URL。"
            "当前 FastGPT 在另一台电脑上，本机磁盘路径不能直接作为文档传入。"
        )
    return file_url


def build_message_content(topic, file_url, file_name):
    if not file_url:
        return topic

    return [
        {
            "type": "text",
            "text": topic,
        },
        {
            "type": "file_url",
            "name": file_name,
            "url": file_url,
        },
    ]


def build_request_body(topic, config, file_url=""):
    file_name = file_name_from_url(file_url) if file_url else ""
    return {
        "chatId": "workflow-connectivity-test",
        "stream": False,
        "detail": bool(config.get("detail", True)),
        "variables": {
            "topic": topic,
            "taskNo": "workflow-connectivity-test",
            "productName": topic,
            "reportFileName": file_name,
            "reportFileUrl": file_url,
            "reportText": "",
            "auditStandard": config.get("default-audit-standard", ""),
            "callbackTraceId": "workflow-connectivity-test",
        },
        "messages": [
            {
                "role": "user",
                "content": build_message_content(topic, file_url, file_name),
            }
        ],
    }


def call_fastgpt(config, topic, file_url, use_proxy, print_request):
    base_url = str(config.get("base-url", "")).rstrip("/")
    api_key = str(config.get("api-key", ""))

    if not base_url:
        raise ValueError("fastgpt.base-url 未配置")
    if not api_key:
        raise ValueError("fastgpt.api-key 未配置。若 application.yml 使用 ${FASTGPT_API_KEY:}，请先设置环境变量 FASTGPT_API_KEY")

    endpoint = base_url + "/v1/chat/completions"
    print("=== Request URL ===")
    print(endpoint)
    print()
    if file_url:
        print("=== Document URL ===")
        print(file_url)
        print()

    request_body = build_request_body(topic, config, file_url)
    if print_request:
        print("=== Request Body ===")
        print(json.dumps(request_body, ensure_ascii=False, indent=2))
        print()

    body = json.dumps(request_body, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        endpoint,
        data=body,
        method="POST",
        headers={
            "Content-Type": "application/json",
            "Authorization": "Bearer " + api_key,
        },
    )

    timeout_seconds = int(config.get("read-timeout", 300000)) / 1000
    opener = urllib.request.build_opener() if use_proxy else urllib.request.build_opener(urllib.request.ProxyHandler({}))
    try:
        with opener.open(request, timeout=timeout_seconds) as response:
            response_body = response.read().decode("utf-8", errors="replace")
            return response.status, dict(response.headers), response_body
    except urllib.error.HTTPError as exc:
        response_body = exc.read().decode("utf-8", errors="replace")
        return exc.code, dict(exc.headers), response_body


def clean_markdown_wrapper(content):
    if content is None:
        return ""

    content = content.strip()
    if content.startswith("\ufeff"):
        content = content[1:]

    if content.startswith("```"):
        first_newline = content.find("\n")
        if first_newline > 0:
            content = content[first_newline + 1 :]
        if content.endswith("```"):
            content = content[:-3]
        content = content.strip()

    first_brace = content.find("{")
    last_brace = content.rfind("}")
    if first_brace >= 0 and last_brace > first_brace:
        content = content[first_brace : last_brace + 1]

    return content


def find_business_json_candidate(node):
    if isinstance(node, dict):
        if "findings" in node:
            return node
        for value in node.values():
            candidate = find_business_json_candidate(value)
            if candidate is not None:
                return candidate
    elif isinstance(node, list):
        for value in node:
            candidate = find_business_json_candidate(value)
            if candidate is not None:
                return candidate
    elif isinstance(node, str):
        cleaned = clean_markdown_wrapper(node)
        try:
            parsed = json.loads(cleaned)
        except json.JSONDecodeError:
            return None
        if isinstance(parsed, dict) and "findings" in parsed:
            return parsed
        return find_business_json_candidate(parsed)
    return None


def print_json_or_text(value):
    if isinstance(value, (dict, list)):
        print(json.dumps(value, ensure_ascii=False, indent=2))
    else:
        print(value)


def print_response(status, headers, body):
    print("=== HTTP Status ===")
    print(status)
    print()
    print("=== Response Headers ===")
    print(json.dumps(headers, ensure_ascii=False, indent=2))
    print()
    print("=== Response Body ===")
    try:
        parsed = json.loads(body)
        print(json.dumps(parsed, ensure_ascii=False, indent=2))
    except json.JSONDecodeError:
        print(body)
        print()
        print("=== Response Analysis ===")
        print("响应体不是 JSON，无法继续分析。")
        return

    print()
    print("=== Extracted choices[0].message.content ===")
    content = ""
    choices = parsed.get("choices")
    if isinstance(choices, list) and choices:
        message = choices[0].get("message") if isinstance(choices[0], dict) else None
        if isinstance(message, dict):
            content = message.get("content") or ""
    if content:
        print(truncate(content, 5000))
    else:
        print("<empty>")

    print()
    print("=== Cleaned Business Content ===")
    cleaned = clean_markdown_wrapper(content)
    if cleaned:
        print(truncate(cleaned, 5000))
    else:
        print("<empty>")

    print()
    print("=== Parsed Business JSON ===")
    business_json = None
    if cleaned:
        try:
            business_json = json.loads(cleaned)
        except json.JSONDecodeError as exc:
            print(f"message.content 清理后仍不是 JSON: {exc}")

    if business_json is None:
        business_json = find_business_json_candidate(parsed)
        if business_json is not None:
            print("从完整响应中递归找到了包含 findings 的 JSON：")

    if business_json is None:
        print("<not found>")
        return

    print_json_or_text(business_json)
    print()
    print("=== Business JSON Shape ===")
    if isinstance(business_json, dict):
        findings = business_json.get("findings")
        print(f"top-level keys: {list(business_json.keys())}")
        print(f"summary type: {type(business_json.get('summary')).__name__}")
        print(f"findings type: {type(findings).__name__}")
        if isinstance(findings, list):
            print(f"findings length: {len(findings)}")
            if findings:
                print(f"first finding type: {type(findings[0]).__name__}")
                print("first finding:")
                print_json_or_text(findings[0])
        else:
            print("findings value:")
            print_json_or_text(findings)
    else:
        print(f"business JSON type: {type(business_json).__name__}")


def main():
    parser = argparse.ArgumentParser(description="Test FastGPT workflow connectivity.")
    parser.add_argument("topic", nargs="?", help="发送给 FastGPT 工作流的简单主题")
    parser.add_argument("--file-url", help="传给 FastGPT 的文档 HTTP/HTTPS URL，必须能被 FastGPT 服务访问")
    parser.add_argument("--config", default=str(DEFAULT_CONFIG), help="application.yml 路径")
    parser.add_argument("--use-proxy", action="store_true", help="允许使用系统代理。默认禁用代理，避免局域网地址被代理转发")
    parser.add_argument("--print-request", action="store_true", help="打印实际发送给 FastGPT 的请求体")
    parser.add_argument("--raw-only", action="store_true", help="只打印 FastGPT 原始响应体，不做任何解析或格式化")
    args = parser.parse_args()

    topic = args.topic
    if not topic:
        topic = input("请输入测试主题: ").strip()
    if not topic:
        print("测试主题不能为空", file=sys.stderr)
        return 2

    config = load_fastgpt_config(Path(args.config))
    file_url = validate_file_url(args.file_url or "")
    status, headers, body = call_fastgpt(config, topic, file_url, args.use_proxy, args.print_request)
    if args.raw_only:
        print(body)
    else:
        print_response(status, headers, body)
    return 0 if 200 <= status < 300 else 1


if __name__ == "__main__":
    raise SystemExit(main())
