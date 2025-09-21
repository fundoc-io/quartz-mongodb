Param(
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir '..')).Path
Set-Location $projectRoot

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $repoDir = Join-Path $projectRoot 'build/local-m2-repo'
} else {
    if (-not (Test-Path -LiteralPath $OutputDirectory)) {
        $null = New-Item -ItemType Directory -Path $OutputDirectory -Force
    }
    $repoDir = (Resolve-Path -LiteralPath $OutputDirectory).Path
}

$null = New-Item -ItemType Directory -Path $repoDir -Force

$version = (& mvn -B -ntp help:evaluate -Dexpression=project.version -q -DforceStdout)
Write-Host "Detected project version: $version"

if ($version -notlike '*-SNAPSHOT') {
    Write-Error "Project version $version is not a SNAPSHOT build. Aborting local snapshot publish."
}

$altRepo = "local::default::file://$repoDir"
Write-Host "Publishing to $repoDir"
& mvn -B -ntp clean deploy -DskipTests -DaltDeploymentRepository=$altRepo
Write-Host "Artifacts deployed to $repoDir. Configure your consumer project's repositories accordingly to test."
