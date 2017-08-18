git config user.name "AlexJitianu";
git config user.email "alex_jitianu@oxygenxml.com";
git fetch;
git checkout master;
git reset;
cp -f target/update_site.xml build;
git add build/update_site.xml;
git commit -m "New addon release - ${TRAVIS_TAG}";
git push origin HEAD:master; 