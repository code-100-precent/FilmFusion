const fs = require('fs');
const path = require('path');
// The script is in miniprogram/, so api.js is in services/ relative to it.
const filePath = path.join(__dirname, 'services', 'api.js');
const content = fs.readFileSync(filePath, 'utf8');
const lines = content.split('\n');
const newContent = lines.slice(0, 788).join('\n');
fs.writeFileSync(filePath, newContent);
console.log('Fixed api.js');
