const diff2html = require("diff2html");
const fs = require("fs");
const path = require("path");
const { execSync } = require("child_process");

const TMP_DIR = "/tmp/diff2html";

const PREFIX = `
<!DOCTYPE html>
<html lang="en-us">
  <head>
    <meta charset="utf-8" />
    <!-- Make sure to load the highlight.js CSS file before the Diff2Html CSS file -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/10.7.1/styles/github.min.css" />
    <link
      rel="stylesheet"
      type="text/css"
      href="https://cdn.jsdelivr.net/npm/diff2html/bundles/css/diff2html.min.css"
    />
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/diff2html/bundles/js/diff2html-ui.min.js"></script>
  </head>
  <body>
`;

const SUFFIX = `
  </body>
</html>
`;

function writeResult(result, fdOutput) {
  const fdWriterName = path.join(TMP_DIR, "writer")
  const fdWriter = fs.openSync(fdWriterName, "w");
  fs.writeSync(fdWriter, result.writerschema);

  const fdReaderName = path.join(TMP_DIR, "reader");
  const fdReader = fs.openSync(fdReaderName, "w");
  fs.writeSync(fdReader, result.readerschema);

  try {
    execSync(`diff -uZ ${fdWriterName} ${fdReaderName}`);
    // files are the same, do nothing
  } catch (err) {
    // files are different
    const diffString = err.stdout.toString();
    const diffJson = diff2html.parse(diffString);
    const diffHtml = diff2html.html(diffJson, { drawFileList: false, outputFormat: 'side-by-side', synchronisedScroll: true });

    fs.writeSync(fdOutput, diffHtml);
  }
}

function resultHeading(result) {
  let color = "#FFFFFF";
  if (result.expected == "NOT_COMPATIBLE") {
    color = "#FFBBBB";
  } else if (result.expected == "COMPATIBLE") {
    color = "#BBFFBB";
  }

  return `\n<h3 style="background: ${color}">${result.expected} | ${result.description} | ${result.testcase}</h3>\n`
}

if (!fs.existsSync(TMP_DIR)) {
  fs.mkdirSync(TMP_DIR);
}

const fdOutput = fs.openSync(process.env.REPORTFILE, "w");
fs.writeSync(fdOutput, PREFIX);

const results = new Map();

let stdout = execSync(`java -jar ${process.env.JARFILE} -d ${process.env.TESTS}`);
let lines = stdout.toString().split("\n");
for (let line of lines) {
  if (line.length == 0)
    continue;

    let result = JSON.parse(line);
    if (!(result.schematype in results)) {
      results[result.schematype] = new Map();
    }
    if (!(result.expected in results[result.schematype])) {
      results[result.schematype][result.expected] = new Array();
    }
    results[result.schematype][result.expected].push(result);
}

for (let schematype in results) {
  fs.writeSync(fdOutput, `\n<h2 style="background: #BBBBFF">${schematype}</h2>\n`);
  for (let compatible in results[schematype]) {
    for (let result of results[schematype][compatible]) {
      fs.writeSync(fdOutput, resultHeading(result));
      writeResult(result, fdOutput);
      if (result.messages.length > 0) {
        fs.writeSync(fdOutput, `<p><b>Messages:</b>${result.messages}</p>`);
      }
    }
  }
}

fs.writeSync(fdOutput, SUFFIX);
