#!/bin/sh

# Package for Mac
rm -rf dist
mkdir dist
cp assets/resources/images/abeille.icns dist 
cp target/scala-3.2.2/abeilledb.jar dist 
cd dist

jpackage --input . \
--name AbeilleDb \
--main-jar abeilledb.jar \
--main-class com.jeta.abeille.main.Main \
--type dmg \
--icon "./abeille.icns" \
--app-version "1.2.3" \
--copyright "Copyright 2023 Jeff Tassin" \
--mac-package-name "Abeille Database Client" \
--verbose \
--java-options '--enable-preview'  

cd ..
