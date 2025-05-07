import fs from "fs";
import path from "path";

const isFile = (fileName: string) => {
  return fs.lstatSync(fileName).isFile();
};
const isJson = (fileName: string) => {
  return fileName.endsWith(".json");
};
const sourceFolderPath = "stub";
const destinationFolderPath = "stub-tests";

if (fs.existsSync(destinationFolderPath)) {
  fs.rmSync(destinationFolderPath, { recursive: true, force: true });
}

fs.cpSync(sourceFolderPath, destinationFolderPath, { recursive: true });

const mappingsFolderName = "mappings";
const mappingsFolderPath = path.join(destinationFolderPath, mappingsFolderName);

const defaultDelay = 50;

fs.readdirSync(mappingsFolderPath)
  .map((fileName) => {
    return path.join(mappingsFolderPath, fileName);
  })
  .filter(isFile)
  .filter(isJson)
  .forEach((fileName) => {
    const rawData = fs.readFileSync(fileName, "utf8");
    const data = JSON.parse(rawData);
    data.mappings.forEach((mapping) => {
      if (mapping.response.fixedDelayMilliseconds > defaultDelay) {
        mapping.response.fixedDelayMilliseconds = defaultDelay;
      }
    });
    fs.writeFileSync(fileName, JSON.stringify(data, null, 2), "utf8");
  });
