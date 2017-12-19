echo "Prepare to deploy add-on"

REPONAME=`basename $PWD`
PARENTDIR=`dirname $PWD`
USERNAME=`basename $PARENTDIR`

git config user.name "AlexJitianu";
git config user.email "alex_jitianu@oxygenxml.com";

echo "Check out https://$GH_TOKEN@github.com/$USERNAME/$REPONAME.git"

git clone "https://$GH_TOKEN@github.com/$USERNAME/$REPONAME.git"
cd $REPONAME

cp -f ../target/update_site.xml build;

git add build/update_site.xml;
git commit -m "New addon release - ${TRAVIS_TAG}";
git push
