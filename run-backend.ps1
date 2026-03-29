$port = 8082
$mainClass = 'com.loanmanagement.app.LoanManagementAppApplication'

$connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

if ($connection) {
    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($connection.OwningProcess)"
    $commandLine = [string]$process.CommandLine

    if ($commandLine -like "*$mainClass*") {
        Write-Host "Stopping existing loanManagementApp process on port $port (PID $($connection.OwningProcess))..."
        Stop-Process -Id $connection.OwningProcess -Force
        Start-Sleep -Seconds 2
    }
    else {
        Write-Error "Port $port is already in use by another process ($($process.Name), PID $($connection.OwningProcess)). Stop it manually or change server.port before starting the backend."
        exit 1
    }
}

Write-Host "Starting loanManagementApp on port $port..."
mvn spring-boot:run