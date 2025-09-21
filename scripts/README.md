# Publishing Scripts

## Sonatype Snapshot Deployment

### macOS/Linux
```bash
OSSRH_USERNAME=your_user OSSRH_PASSWORD=your_token ./scripts/publish-sonatype-snapshot.sh
```
The script checks the project version for the `-SNAPSHOT` suffix, creates a temporary Maven `settings.xml` with the OSSRH credentials, and runs `mvn -B -ntp clean deploy -DskipTests`.

### Windows PowerShell
```powershell
$env:OSSRH_USERNAME='your_user'
$env:OSSRH_PASSWORD='your_token'
powershell -File scripts/publish-sonatype-snapshot.ps1
```
The PowerShell variant performs the same steps as the shell script.

## Local Snapshot Repository

To deploy artifacts into a local Maven repository for smoke testing:

### macOS/Linux
```bash
./scripts/publish-local-snapshot.sh
```

### Windows PowerShell
```powershell
powershell -File scripts/publish-local-snapshot.ps1 -OutputDirectory C:\path\to\repo
# OutputDirectory is optional; defaults to build/local-m2-repo
```
By default artifacts are written to `build/local-m2-repo`. To change the target directory, pass it (or use the `-OutputDirectory` parameter).
