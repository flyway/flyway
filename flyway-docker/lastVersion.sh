git fetch --tags
lastVersion=$(git describe --tags --abbrev=0)
echo ${lastVersion:1}