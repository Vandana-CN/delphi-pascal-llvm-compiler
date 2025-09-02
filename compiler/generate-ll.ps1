$antlrJar = "antlr-4.9.3-complete.jar"
$binDir = "bin"
$testDir = "tests"

# Make sure output files go inside the tests folder
$testFiles = Get-ChildItem -Path $testDir -Filter *.pas

foreach ($file in $testFiles) {
    Write-Host "------------------------------"
    Write-Host "Processing $($file.Name)..."

    # Run the interpreter on the test case
    java -cp "$binDir;$antlrJar" antlr.Main "$($file.FullName)"

    # Copy the output.ll file as testX.ll in the tests folder
    if (Test-Path "output.ll") {
        $llFileName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name) + ".ll"
        Copy-Item -Path "output.ll" -Destination "$testDir\$llFileName" -Force
        Write-Host "✅ Saved LLVM IR as: $llFileName"
    } else {
        Write-Host "❌ Error: output.ll not found!"
    }
}

Write-Host "`n------------------------------"
Write-Host "All test cases processed. LLVM files saved in /tests"
