param(
    [string]$WorkflowCode = "policy_document_audit",
    [switch]$All,
    [string]$HostName = $(if ($env:AUDIT_WORKFLOW_DB_HOST) { $env:AUDIT_WORKFLOW_DB_HOST } else { "127.0.0.1" }),
    [int]$Port = $(if ($env:AUDIT_WORKFLOW_DB_PORT) { [int]$env:AUDIT_WORKFLOW_DB_PORT } else { 3306 }),
    [string]$Database = $(if ($env:AUDIT_WORKFLOW_DB_NAME) { $env:AUDIT_WORKFLOW_DB_NAME } else { "audit_workflow" }),
    [string]$User = $(if ($env:AUDIT_WORKFLOW_DB_USERNAME) { $env:AUDIT_WORKFLOW_DB_USERNAME } else { "audit_workflow" }),
    [string]$Password = $(if ($env:AUDIT_WORKFLOW_DB_PASSWORD) { $env:AUDIT_WORKFLOW_DB_PASSWORD } else { "AuditWorkflow_2026@Mysql!" })
)

$ErrorActionPreference = "Stop"

$localPython = Join-Path (Get-Location) ".venv\Scripts\python.exe"
if (Test-Path $localPython) {
    $pythonPath = $localPython
} else {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if (-not $python) {
        $python = Get-Command python3 -ErrorAction SilentlyContinue
    }
    if (-not $python) {
        throw "python was not found. Activate the project virtual environment or install Python."
    }
    $pythonPath = $python.Source
}

$scriptPath = Join-Path $PSScriptRoot "print_workflow_prompts.py"
$arguments = @(
    $scriptPath,
    "--workflow-code", $WorkflowCode,
    "--host", $HostName,
    "--port", [string]$Port,
    "--database", $Database,
    "--user", $User,
    "--password", $Password
)
if ($All) {
    $arguments += "--all"
}

& $pythonPath @arguments
exit $LASTEXITCODE
