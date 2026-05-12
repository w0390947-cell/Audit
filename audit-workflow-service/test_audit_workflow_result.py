#!/usr/bin/env python3
"""
Create an audit workflow task, poll until it reaches a terminal state, then
print the workflow system's final result response.

Stdout is reserved for the final result JSON only.

Examples:
  python3 scripts/test_audit_workflow_result.py \
    --file-url "/profile/upload/2026/05/12/report.docx" \
    --product-name "本安-矿用本安型手机"

  python3 scripts/test_audit_workflow_result.py \
    --file-url "http://127.0.0.1:6039/profile/upload/2026/05/12/report.docx" \
    --file-name "report.docx"
"""

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from urllib.parse import quote, unquote, urlparse


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CONFIG = PROJECT_ROOT / "backend/ruoyi-admin/src/main/resources/application.yml"
TERMINAL_STATUSES = {"SUCCESS", "FAILED", "CANCELED", "CANCELLED"}


def parse_args():
    parser = argparse.ArgumentParser(
        description="Create an audit workflow task and print its final result JSON.",
    )
    parser.add_argument("--config", default=str(DEFAULT_CONFIG), help="Path to application.yml")
    parser.add_argument("--base-url", help="Workflow base URL. Defaults to audit-workflow.base-url")
    parser.add_argument("--workflow-code", help="Defaults to audit-workflow.workflow-code")
    parser.add_argument("--file-url", required=True, help="Report file URL. Relative /profile paths are supported")
    parser.add_argument("--file-name", help="Report file name. Defaults to the URL basename")
    parser.add_argument("--file-type", help="Report file type. Defaults to file-name extension")
    parser.add_argument("--product-name", default="工作流返回内容测试", help="Product name in workflow metadata")
    parser.add_argument("--delivery-unit", default="", help="Delivery unit in workflow metadata")
    parser.add_argument("--task-no", default="", help="Optional task number in workflow metadata")
    parser.add_argument("--biz-id", default="", help="Optional biz_id. Defaults to workflow-result-test-{timestamp}")
    parser.add_argument("--poll-interval", type=float, default=None, help="Polling interval seconds")
    parser.add_argument("--timeout", type=float, default=None, help="Polling timeout seconds")
    return parser.parse_args()


def load_audit_workflow_config(config_path):
    path = Path(config_path)
    if not path.exists():
        raise FileNotFoundError(f"配置文件不存在: {path}")

    config = {}
    in_section = False
    section_indent = None
    pending_list_key = None

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.split("#", 1)[0].rstrip()
        if not line.strip():
            continue

        indent = len(line) - len(line.lstrip(" "))
        stripped = line.strip()

        if stripped == "audit-workflow:":
            in_section = True
            section_indent = indent
            pending_list_key = None
            continue

        if not in_section:
            continue

        if indent <= section_indent:
            break

        if stripped.startswith("- ") and pending_list_key:
            config.setdefault(pending_list_key, []).append(resolve_placeholder(parse_scalar(stripped[2:])))
            continue

        pending_list_key = None
        if ":" not in stripped:
            continue

        key, value = stripped.split(":", 1)
        key = key.strip()
        value = value.strip()
        if value == "":
            config[key] = []
            pending_list_key = key
        else:
            config[key] = resolve_placeholder(parse_scalar(value))

    return config


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
    try:
        return int(value)
    except ValueError:
        return value


def resolve_placeholder(value):
    if not isinstance(value, str):
        return value
    if not value.startswith("${") or not value.endswith("}"):
        return value
    inner = value[2:-1]
    if ":" in inner:
        env_name, default = inner.split(":", 1)
    else:
        env_name, default = inner, ""
    return os.environ.get(env_name, default)


def normalize_file_url(file_url, public_file_base_url):
    if file_url.startswith("/profile/"):
        return str(public_file_base_url or "").rstrip("/") + file_url
    return file_url


def file_name_from_url(file_url):
    path = unquote(urlparse(file_url).path)
    name = Path(path).name
    return name or "report.docx"


def file_type_from_name(file_name):
    suffix = Path(file_name).suffix.lower().lstrip(".")
    return suffix or "docx"


def request_json(method, url, body=None):
    data = None
    headers = {"Accept": "application/json"}
    if body is not None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json"

    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            raw = response.read().decode("utf-8")
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {url}\n{detail}") from exc

    try:
        return json.loads(raw), raw
    except json.JSONDecodeError as exc:
        raise RuntimeError(f"响应不是合法 JSON: {url}\n{raw}") from exc


def assert_success(response, message):
    code = response.get("code", 500)
    if code != 200:
        raise RuntimeError(f"{message}: {response.get('message', response)}")


def extract_task_id(create_response):
    data = create_response.get("data") or {}
    for key in ("taskId", "task_id", "id", "workflowTaskId", "workflow_task_id"):
        value = data.get(key)
        if value is not None and str(value).strip():
            return str(value)
    raise RuntimeError(f"创建任务响应中未找到任务ID: {json.dumps(create_response, ensure_ascii=False)}")


def build_create_body(args, config):
    public_base = str(config.get("public-file-base-url", ""))
    file_url = normalize_file_url(args.file_url, public_base)
    file_name = args.file_name or file_name_from_url(file_url)
    file_type = args.file_type or file_type_from_name(file_name)
    now_ms = int(time.time() * 1000)
    biz_id = args.biz_id or f"workflow-result-test-{now_ms}"
    workflow_code = args.workflow_code or config.get("workflow-code") or "policy_document_audit"

    knowledge_base_codes = config.get("knowledge-base-codes") or ["default"]
    if not isinstance(knowledge_base_codes, list):
        knowledge_base_codes = [knowledge_base_codes]

    return {
        "workflow_code": workflow_code,
        "biz_id": biz_id,
        "input": {
            "file_id": biz_id,
            "file_url": file_url,
            "file_name": file_name,
            "file_type": file_type,
            "metadata": {
                "business_type": "audit_review",
                "task_no": args.task_no,
                "product_name": args.product_name,
                "delivery_unit": args.delivery_unit,
            },
            "knowledge_scope": {
                "knowledge_base_codes": knowledge_base_codes,
                "effective_only": True,
            },
            "caller_context": {
                "user_id": "workflow-result-test",
                "permission_mode": config.get("permission-mode") or "explicit_scope",
            },
        },
    }


def poll_until_terminal(base_url, task_id, interval_seconds, timeout_seconds):
    deadline = time.time() + timeout_seconds
    task_url = f"{base_url}/api/audit/tasks/{quote(str(task_id), safe='')}"

    while time.time() < deadline:
        response, _ = request_json("GET", task_url)
        assert_success(response, "查询工作流任务失败")
        data = response.get("data") or {}
        status = str(data.get("taskStatus") or data.get("task_status") or "").upper()
        if status in TERMINAL_STATUSES:
            return
        time.sleep(interval_seconds)

    raise TimeoutError(f"工作流任务轮询超时: task_id={task_id}")


def main():
    args = parse_args()
    config = load_audit_workflow_config(args.config)
    base_url = str(args.base_url or config.get("base-url") or "").rstrip("/")
    if not base_url:
        raise ValueError("未配置工作流 base-url")

    interval_ms = config.get("poll-interval-ms") or 3000
    timeout_ms = config.get("poll-timeout-ms") or 900000
    interval_seconds = args.poll_interval if args.poll_interval is not None else float(interval_ms) / 1000
    timeout_seconds = args.timeout if args.timeout is not None else float(timeout_ms) / 1000

    create_body = build_create_body(args, config)
    create_response, _ = request_json("POST", f"{base_url}/api/audit/tasks", create_body)
    assert_success(create_response, "创建工作流任务失败")
    task_id = extract_task_id(create_response)

    poll_until_terminal(base_url, task_id, interval_seconds, timeout_seconds)

    result_url = f"{base_url}/api/audit/tasks/{quote(str(task_id), safe='')}/result"
    _, raw_result = request_json("GET", result_url)
    print(raw_result)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(1)
