import fs from "fs";
import path from "path";
import {execSync} from "child_process";
import readline from 'readline';

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

function promptUser(question) {
    return new Promise((resolve) => {
        rl.question(question, (answer) => {
            resolve(answer.toLowerCase());
        });
    });
}

async function updateVersion(type = 'patch', preRelease = 'alpha') {
    // Read the actual version
    const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
    let [major, minor, patch, pre, preNum] = packageJson.version.split(/[.-]/).map((part, index) => index < 3 ? parseInt(part) : part);

    // Actualizar la versión
    switch (type) {
        case 'major':
            major++;
            minor = 0;
            patch = 0;
            break;
        case 'minor':
            minor++;
            patch = 0;
            break;
        case 'patch':
            patch++;
            break;
    }

    // Handle pre-release
    if (preRelease !== pre) {
        pre = preRelease;
        preNum = 1;
    } else {
        preNum = parseInt(preNum || 0) + 1;
    }

    const newVersion = `${major}.${minor}.${patch}-${pre}.${preNum}`;

    // Prompt user for confirmation
    console.log(`Current version: ${packageJson.version}`);
    console.log(`New version will be: ${newVersion}`);
    const answer = await promptUser('Do you want to proceed with this update? (y/n) ');

    if (answer !== 'y') {
        console.log('Update cancelled.');
        rl.close();
        return;
    }

    // Update package.json
    packageJson.version = newVersion;
    fs.writeFileSync('package.json', JSON.stringify(packageJson, null, 2));

    // Update Cargo.toml
    const cargoPath = path.join('src-tauri', 'Cargo.toml');
    let cargoToml = fs.readFileSync(cargoPath, 'utf8');
    cargoToml = cargoToml.replace(/^version = ".*"/m, `version = "${newVersion}"`);
    fs.writeFileSync(cargoPath, cargoToml);

    console.log(`Version updated to ${newVersion}`);

    // Determine the commit type based on the version change
    let commitType;
    if (type === 'major') {
        commitType = 'feat!';  // Breaking change
    } else if (type === 'minor') {
        commitType = 'feat';   // New feature
    } else {
        commitType = 'fix';    // Bug fix or patch
    }

    // Make a commit with the new version using Conventional Commits format
    execSync(`git add package.json ${cargoPath}`);
    execSync(`git commit -m "${commitType}: bump version to ${newVersion}

BREAKING CHANGE: This commit updates the application version."`);

    // Create a tag
    execSync(`git tag -a v${newVersion} -m "Version ${newVersion}"`);
    console.log(`Tag v${newVersion} created`);

    rl.close();
}

// Execute the script
const [, , type, preRelease] = process.argv;
updateVersion(type, preRelease).catch(error => {
    console.error('An error occurred:', error);
    process.exit(1);
});