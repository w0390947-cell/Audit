import argparse
import os
import subprocess
import sys
import tempfile
from pathlib import Path


DEFAULT_MYSQL_CANDIDATES = [
    Path(r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"),
    Path("/mnt/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe"),
]
SEP = "__AUDIT_PROMPT_SEP__"


def env_value(name, default):
    return os.environ.get(name) or default


def mysql_path():
    configured = os.environ.get("MYSQL_EXE")
    if configured:
        return configured
    for candidate in DEFAULT_MYSQL_CANDIDATES:
        if candidate.exists():
            return str(candidate)
    return "mysql"


def windows_path_for_mysql(path, mysql_exe):
    text = str(path)
    mysql_text = str(mysql_exe)
    if mysql_text.startswith("/mnt/") and text.startswith("/mnt/") and len(text) > 6:
        drive = text[5].upper()
        rest = text[6:].replace("/", "\\")
        return f"{drive}:\\{rest}"
    return text


def hex_to_text(value):
    value = "".join(str(value or "").split())
    if not value:
        return ""
    return bytes.fromhex(value).decode("utf-8", errors="replace")


def build_sql(workflow_code, all_workflows):
    select_expr = (
        "CONCAT("
        f"HEX(COALESCE(workflow_code, '')), '{SEP}', "
        f"HEX(COALESCE(workflow_name, '')), '{SEP}', "
        "HEX(COALESCE(prompt_template, ''))"
        ")"
    )
    if all_workflows:
        return f"SELECT {select_expr} FROM audit_workflow ORDER BY workflow_code;"
    escaped = workflow_code.replace("'", "''")
    return f"SELECT {select_expr} FROM audit_workflow WHERE workflow_code = '{escaped}';"


def run_mysql(args, sql):
    option_content = "\n".join([
        "[client]",
        f"host={args.host}",
        f"port={args.port}",
        f"user={args.user}",
        f"password={args.password}",
        "default-character-set=utf8mb4",
        "",
    ])
    option_file = None
    mysql_exe = mysql_path()
    try:
        with tempfile.NamedTemporaryFile("w", suffix=".cnf", delete=False, encoding="ascii", dir=Path.cwd()) as file:
            option_file = file.name
            file.write(option_content)
        option_file_for_mysql = windows_path_for_mysql(option_file, mysql_exe)
        cmd = [
            mysql_exe,
            f"--defaults-extra-file={option_file_for_mysql}",
            "--batch",
            "--raw",
            "--silent",
            "--skip-column-names",
            args.database,
            "-e",
            sql,
        ]
        return subprocess.run(cmd, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    finally:
        if option_file:
            try:
                os.remove(option_file)
            except OSError:
                pass


def main():
    parser = argparse.ArgumentParser(description="Print audit workflow prompt_template from MySQL.")
    parser.add_argument("--workflow-code", default="policy_document_audit")
    parser.add_argument("--all", action="store_true")
    parser.add_argument("--host", default=env_value("AUDIT_WORKFLOW_DB_HOST", "127.0.0.1"))
    parser.add_argument("--port", default=env_value("AUDIT_WORKFLOW_DB_PORT", "3306"))
    parser.add_argument("--database", default=env_value("AUDIT_WORKFLOW_DB_NAME", "audit_workflow"))
    parser.add_argument("--user", default=env_value("AUDIT_WORKFLOW_DB_USERNAME", "audit_workflow"))
    parser.add_argument("--password", default=env_value("AUDIT_WORKFLOW_DB_PASSWORD", "AuditWorkflow_2026@Mysql!"))
    args = parser.parse_args()

    print(f"Querying {args.user}@{args.host}:{args.port}/{args.database} ...")
    proc = run_mysql(args, build_sql(args.workflow_code, args.all))
    if proc.returncode != 0:
        if proc.stderr:
            print(proc.stderr.strip(), file=sys.stderr)
        return proc.returncode

    rows = [line for line in proc.stdout.splitlines() if line.strip()]
    if not rows:
        if args.all:
            print("No workflow prompt found.")
        else:
            print(f"No workflow prompt found: {args.workflow_code}")
        return 0

    printed = 0
    for row in rows:
        columns = row.split(SEP, 2)
        if len(columns) != 3:
            print("WARN: Unexpected mysql row format:")
            print(row)
            continue
        workflow_code = hex_to_text(columns[0])
        workflow_name = hex_to_text(columns[1])
        prompt = hex_to_text(columns[2])
        print("============================================================")
        print(f"workflow_code: {workflow_code}")
        print(f"workflow_name: {workflow_name}")
        print("------------------------------------------------------------")
        print(prompt if prompt else "(prompt_template is empty)")
        print()
        printed += 1

    if printed == 0:
        print("No printable prompt rows. The query returned data, but the row format was not recognized.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
