echo "Prepare to deploy add-on"

REPONAME=`basename $PWD`
PARENTDIR=`dirname $PWD`
USERNAME=`basename $PARENTDIR`

mkdir deploy
cd deploy

echo "Initialize repository"

git init
git config user.name "AlexJitianu";
git config user.email "alex_jitianu@oxygenxml.com";

echo "Link with https://$GH_TOKEN@github.com/$USERNAME/$REPONAME.git"

git remote add upstream "https://$GH_TOKEN@github.com/$USERNAME/$REPONAME.git"
git fetch upstream
git reset upstream/master

cp -f ../target/update_site.xml build;

ls
git status

git add build/update_site.xml;
git commit -m "New addon release - ${TRAVIS_TAG}";
git push -q upstream HEAD:master;
