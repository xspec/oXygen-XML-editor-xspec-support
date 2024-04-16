
=======
# Oxygen XSpec Helper View

This add-on contributes an **XSpec Test Results** view in Oxygen XML Editor/Developer intended to help those that create XSpec scenarios.

Install as Add-On
-----------------

1. In Oxygen, go to **Help->Install new add-ons** to open an add-on selection dialog box.
2. Enter or paste https://raw.githubusercontent.com/xspec/oXygen-XML-editor-xspec-support/master/build/update_site.xml in the **Show add-ons from** field.
3. Select the **XSpec Helper View** and **XSpec Framework** add-ons (both are required) and click Next.
4. Select the **I accept all terms of the end user license agreement** option and click **Finish**.
5. Restart the application.

Alternative Installation Method
-----

1. Download the plugin [ZIP package](https://github.com/AlexJitianu/oXygen-XML-editor-xspec-support/raw/master/build/xspec.support-1.0-SNAPSHOT-plugin.zip) and unzip it inside `{OxygenInstallDir}/plugins`.
2. Download the framework [ZIP package](https://github.com/AlexJitianu/oXygen-XML-editor-xspec-support/raw/master/build/xspec.zip) and unzip it inside `{OxygenInstallDir}/frameworks`.


How to Use It
-----------

1. Inside Oxygen XML Editor/Developer, open an XSpec file.
2. Click the **Run XSpec test scenarios** button on the toolbar.

**Result**: An **XSpec Test Results** view will be opened. 

**Tip**: At this point you can switch to the XSLT and use the "Run" actions in this view to execute the scenarios.


#### What you can do inside the "XSpec Test Results" view:

- For each test, there is a **Show** action that selects the corresponding test in the editor.
- For each scenario, there is a **Run** action that just runs that particular scenario.
- For a failed test, you can click on it to open the diff comparison between the expected and actual results.
 

How to Customize It
-------------------
On the XML XSpec report, an XSLT is applied that generates HTML. This HTML is opened inside the view. The XSLT in question 
is: `{pluginDirectory}/frameworks/xspec/src/reporter/unit-report-oxygen.xsl`.

