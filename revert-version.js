// revert-version.js
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

function parseVersion(version) {
    const match = version.match(/^(\d+)\.(\d+)\.(\d+)(?:-(\d+))?(?:\+(\d+))?$/);
    if (!match) {
        throw new Error(`Invalid version format: ${version}`);
    }
    return {
        major: parseInt(match[1]),
        minor: parseInt(match[2]),
        patch: parseInt(match[3]),
        preRelease: match[4] ? parseInt(match[4]) : null,
        build: match[5] ? parseInt(match[5]) : null
    };
}

function getSemverVersion(version) {
    const parsed = parseVersion(version);
    return `${parsed.major}.${parsed.minor}.${parsed.patch}${parsed.preRelease !== null ? `-${parsed.preRelease}` : ''}`;
}

async function revertVersionUpdate() {
    // Get the current version
    const currentPackageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
    const currentVersion = currentPackageJson.version;

    // Get the last two commits
    const commits = execSync('git log --format="%H" -n 2').toString().trim().split('\n');
    const lastCommit = commits[0];
    const previousCommit = commits[1];

    // Check if the last commit is a version bump
    const commitMessage = execSync(`git log --format="%B" -n 1 ${lastCommit}`).toString().trim();
    if (!commitMessage.startsWith('feat') && !commitMessage.startsWith('fix')) {
        console.error('The last commit doesn\'t appear to be a version update. Aborting.');
        rl.close();
        process.exit(1);
    }

    try {
        // Try to get the previous version from the previous commit
        let previousVersion;
        try {
            const previousPackageJson = JSON.parse(execSync(`git show ${previousCommit}:package.json`).toString());
            previousVersion = previousPackageJson.version;
        } catch (error) {
            // If we can't get the previous version, assume it was 0.1.0
            previousVersion = "0.1.0";
        }

        // Validate the previous version
        try {
            parseVersion(previousVersion);
        } catch (error) {
            console.error(`Invalid previous version format: ${previousVersion}`);
            console.log('Resetting to 0.1.0');
            previousVersion = "0.1.0";
        }

        const previousSemverVersion = getSemverVersion(previousVersion);

        // Prompt user for confirmation
        console.log(`Current version: ${currentVersion}`);
        console.log(`This will revert to version: ${previousVersion}`);
        console.log(`Cargo.toml version will be: ${previousSemverVersion}`);
        const answer = await promptUser('Do you want to proceed with this revert? (y/n) ');

        if (answer !== 'y') {
            console.log('Revert cancelled.');
            rl.close();
            return;
        }

        // Update package.json
        currentPackageJson.version = previousVersion;
        fs.writeFileSync('package.json', JSON.stringify(currentPackageJson, null, 2));

        // Update Cargo.toml
        const cargoPath = path.join('src-tauri', 'Cargo.toml');
        let cargoToml = fs.readFileSync(cargoPath, 'utf8');
        cargoToml = cargoToml.replace(/^version = ".*"/m, `version = "${previousSemverVersion}"`);
        fs.writeFileSync(cargoPath, cargoToml);

        // Check if tauri.conf.json exists and has a version field before updating
        const tauriConfPath = path.join('src-tauri', 'tauri.conf.json');
        if (fs.existsSync(tauriConfPath)) {
            const tauriConf = JSON.parse(fs.readFileSync(tauriConfPath, 'utf8'));
            if (tauriConf.package && tauriConf.package.version !== undefined) {
                tauriConf.package.version = previousVersion;
                fs.writeFileSync(tauriConfPath, JSON.stringify(tauriConf, null, 2));
                console.log('Updated version in tauri.conf.json');
            } else {
                console.log('Skipped updating tauri.conf.json as it does not contain a version field.');
            }
        } else {
            console.log('tauri.conf.json not found. Skipping update for this file.');
        }

        // Commit the revert
        execSync('git add package.json src-tauri/Cargo.toml');
        if (fs.existsSync(tauriConfPath)) {
            execSync(`git add ${tauriConfPath}`);
        }
        execSync(`git commit -m "revert: undo version bump, back to ${previousVersion}"`);

        // Delete the tag of the reverted version
        const revertedTag = `v${currentVersion}`;
        try {
            execSync(`git tag -d ${revertedTag}`);
            console.log(`Tag ${revertedTag} deleted locally`);
        } catch (error) {
            console.log(`No tag ${revertedTag} found to delete`);
        }

        console.log(`Revert completed. Version reverted to ${previousVersion} (Cargo.toml: ${previousSemverVersion}).`);
        console.log('Remember to push the changes and delete the remote tag if necessary.');
    } catch (error) {
        console.error('Error reverting changes:', error);
        // Abort the revert and reset the changes
        execSync('git reset --hard HEAD');
        console.log('Local changes have been discarded due to the error.');
    }

    rl.close();
}

revertVersionUpdate().catch(error => {
    console.error('An error occurred:', error);
    process.exit(1);
});