if (-not (Test-Path ".\config.env")) {
    Copy-Item ".\config.example.env" ".\config.env"
    Write-Host "Generated config.env. Please fill Aliyun AccessKey and run this script again."
    exit 1
}

python -m uvicorn server:app --host 127.0.0.1 --port 8866
