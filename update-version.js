// update-version.js
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

async function updateVersion(type = 'patch', isPreRelease = false) {
  // Read the actual version
  const packageJson = JSON.parse(fs.readFileSync('package.json', 'utf8'));
  let parsedVersion;
  try {
    parsedVersion = parseVersion(packageJson.version);
  } catch (error) {
    console.error(`Error parsing current version: ${error.message}`);
    console.log('Resetting to 0.1.0');
    parsedVersion = { major: 0, minor: 1, patch: 0, preRelease: null, build: null };
  }

  // Update the version
  switch (type) {
    case 'major':
      parsedVersion.major++;
      parsedVersion.minor = 0;
      parsedVersion.patch = 0;
      break;
    case 'minor':
      parsedVersion.minor++;
      parsedVersion.patch = 0;
      break;
    case 'patch':
    case 'fix':
      parsedVersion.patch++;
      break;
  }

  // Handle pre-release or build metadata
  if (isPreRelease) {
    parsedVersion.preRelease = parsedVersion.preRelease ? parsedVersion.preRelease + 1 : 1;
    parsedVersion.build = null;
  } else {
    parsedVersion.preRelease = null;
    parsedVersion.build = parsedVersion.build ? parsedVersion.build + 1 : 1;
  }

  const semverVersion = `${parsedVersion.major}.${parsedVersion.minor}.${parsedVersion.patch}${
      parsedVersion.preRelease !== null ? `-${parsedVersion.preRelease}` : ''
  }`;

  const fullVersion = `${semverVersion}${parsedVersion.build !== null ? `+${parsedVersion.build}` : ''}`;

  // Prompt user for confirmation
  console.log(`Current version: ${packageJson.version}`);
  console.log(`New version will be: ${fullVersion}`);
  console.log(`Cargo.toml version will be: ${semverVersion}`);
  const answer = await promptUser('Do you want to proceed with this update? (y/n) ');

  if (answer !== 'y') {
    console.log('Update cancelled.');
    rl.close();
    return;
  }

  // Update package.json
  packageJson.version = fullVersion;
  fs.writeFileSync('package.json', JSON.stringify(packageJson, null, 2));

  // Update Cargo.toml
  const cargoPath = path.join('src-tauri', 'Cargo.toml');
  let cargoToml = fs.readFileSync(cargoPath, 'utf8');
  cargoToml = cargoToml.replace(/^version = ".*"/m, `version = "${semverVersion}"`);
  fs.writeFileSync(cargoPath, cargoToml);

  // Check if tauri.conf.json exists and has a version field before updating
  const tauriConfPath = path.join('src-tauri', 'tauri.conf.json');
  if (fs.existsSync(tauriConfPath)) {
    const tauriConf = JSON.parse(fs.readFileSync(tauriConfPath, 'utf8'));
    if (tauriConf.package && tauriConf.package.version !== undefined) {
      tauriConf.package.version = fullVersion;
      fs.writeFileSync(tauriConfPath, JSON.stringify(tauriConf, null, 2));
      console.log('Updated version in tauri.conf.json');
    } else {
      console.log('Skipped updating tauri.conf.json as it does not contain a version field.');
    }
  } else {
    console.log('tauri.conf.json not found. Skipping update for this file.');
  }

  console.log(`Version updated to ${fullVersion} (Cargo.toml: ${semverVersion})`);

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
  if (fs.existsSync(tauriConfPath)) {
    execSync(`git add ${tauriConfPath}`);
  }
  execSync(`git commit -m "${commitType}: bump version to ${fullVersion}

BREAKING CHANGE: This commit updates the application version."`);

  // Create a tag
  execSync(`git tag -a v${fullVersion} -m "Version ${fullVersion}"`);
  console.log(`Tag v${fullVersion} created`);

  rl.close();
}

// Execute the script
const [, , type, preRelease] = process.argv;
updateVersion(type, preRelease === 'pre').catch(error => {
  console.error('An error occurred:', error);
  process.exit(1);
});