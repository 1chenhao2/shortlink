param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Continue"
$failed = $false

function Test-CommandExists([string]$Name) {
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

Write-Host "`n=== ShortLink 本地开发环境检查 ===" -ForegroundColor Cyan

foreach ($command in @("java", "mvn")) {
    if (Test-CommandExists $command) {
        Write-Host "[OK] $command 已安装" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] 未找到 $command" -ForegroundColor Red
        $failed = $true
    }
}

if (Test-CommandExists "java") {
    & java -version
}
if (Test-CommandExists "mvn") {
    & mvn -version
}

$services = @(
    @{ Name = "MySQL"; Port = 3306 },
    @{ Name = "Redis"; Port = 6379 },
    @{ Name = "Nacos"; Port = 8848 }
)

Write-Host "`n=== 本地依赖端口 ===" -ForegroundColor Cyan
foreach ($service in $services) {
    $listening = Get-NetTCPConnection -State Listen -LocalPort $service.Port -ErrorAction SilentlyContinue
    if ($listening) {
        Write-Host "[OK] $($service.Name) :$($service.Port) 正在监听" -ForegroundColor Green
    } else {
        Write-Host "[WARN] $($service.Name) :$($service.Port) 未监听" -ForegroundColor Yellow
    }
}

if ([string]::IsNullOrWhiteSpace($env:AMAP_API_KEY)) {
    Write-Host "[WARN] 未设置 AMAP_API_KEY，地区统计可能不完整" -ForegroundColor Yellow
} else {
    Write-Host "[OK] 已设置 AMAP_API_KEY（不会输出具体值）" -ForegroundColor Green
}

if (-not $SkipBuild -and (Test-CommandExists "mvn")) {
    Write-Host "`n=== Maven 构建验证 ===" -ForegroundColor Cyan
    & mvn -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[FAIL] Maven 构建失败" -ForegroundColor Red
        $failed = $true
    } else {
        Write-Host "[OK] Maven 构建成功" -ForegroundColor Green
    }
}

if ($failed) {
    Write-Host "`n检查未通过，请先修复红色项目。" -ForegroundColor Red
    exit 1
}

Write-Host "`n基础检查完成。黄色警告不会阻止脚本结束，请根据本地运行需求处理。" -ForegroundColor Green
