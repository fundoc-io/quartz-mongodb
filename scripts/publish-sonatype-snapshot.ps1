Param(
    [string]$WorkingDirectory
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = if ($WorkingDirectory) { (Resolve-Path $WorkingDirectory).Path } else { (Resolve-Path "$scriptDir/.." ).Path }
Set-Location $projectRoot

if (-not $env:OSSRH_USERNAME -or -not $env:OSSRH_PASSWORD) {
    Write-Error 'OSSRH_USERNAME and OSSRH_PASSWORD environment variables must be set.'
}

$version = (& mvn -B -ntp help:evaluate -Dexpression=project.version -q -DforceStdout)
Write-Host "Detected project version: $version"

if ($version -notlike '*-SNAPSHOT') {
    Write-Error "Project version $version is not a SNAPSHOT build. Aborting Sonatype snapshot publish."
}

$tmpSettings = New-TemporaryFile

@"
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>ossrh</id>
      <username>$($env:OSSRH_USERNAME)</username>
      <password>$($env:OSSRH_PASSWORD)</password>
    </server>
  </servers>
</settings>
"@ | Set-Content -Encoding UTF8 $tmpSettings.FullName

try {
    & mvn -B -ntp clean deploy -DskipTests --settings $tmpSettings.FullName
} finally {
    Remove-Item $tmpSettings.FullName -ErrorAction SilentlyContinue
}
