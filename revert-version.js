import fs from "fs";
import path from "path";
import { execSync } from "child_process";
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
        console.error('El último commit no parece ser una actualización de versión. Abortando.');
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

        // Prompt user for confirmation
        console.log(`Current version: ${currentVersion}`);
        console.log(`This will revert to version: ${previousVersion}`);
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
        cargoToml = cargoToml.replace(/^version = ".*"/m, `version = "${previousVersion}"`);
        fs.writeFileSync(cargoPath, cargoToml);

        // Commit the revert
        execSync('git add package.json src-tauri/Cargo.toml');
        execSync(`git commit -m "revert: undo version bump, back to ${previousVersion}"`);

        // Delete the tag of the reverted version
        const revertedTag = `v${currentVersion}`;
        try {
            execSync(`git tag -d ${revertedTag}`);
            console.log(`Tag ${revertedTag} deleted locally`);
        } catch (error) {
            console.log(`No tag ${revertedTag} found to delete`);
        }

        console.log(`Revert completado. Version revertida a ${previousVersion}.`);
        console.log('Recuerda hacer push de los cambios y eliminar el tag remoto si es necesario.');
    } catch (error) {
        console.error('Error al revertir los cambios:', error);
        // Abort the revert and reset the changes
        execSync('git reset --hard HEAD');
        console.log('Se han descartado los cambios locales debido al error.');
    }

    rl.close();
}

revertVersionUpdate().catch(error => {
    console.error('An error occurred:', error);
    process.exit(1);
});