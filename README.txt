== VITAL Overview ==

Video Interactions for Teaching and Learning (VITAL) is a Web-based video analysis and communication system 
created by the Columbia Center for New Media Teaching and Learning and Professor Herbert Ginsburg of Teachers College, 
Columbia University.

VITAL comprises tools for video editing and annotation and for the creation of multimedia reports, 
embedded in the context of an course syllabus with topics, videos, and activities, all housed within an 
online community space.

Students who use VITAL learn to observe closely, interpret, and develop arguments using cited video content as evidence. 
The VITAL environment affords a number of benefits:

   1. The persistent accessibility of video illustrating key concepts, which students can view as often as they wish;
   2. A personal workspace enabling students to identify and isolate material, to pinpoint precise moments in the 
      videos and annotate those moments as well as save them in a private online workspace; and
   3. Analytic exercises in which students work with their video edits, comments and course readings to construct 
      multimedia essays in which they support their theories and arguments with their research in the video library.

The VITAL online community workspace enables students complete assignments from the curriculum plan 
by editing online video segments, annotating their selections, and using these collected resources 
as a dataset to compose their multimedia essays. These essays then become accessible online to their 
instructor and classmates and thus form a foundation for critique and exchange. 

== Technical Architecture ==

General Architecture:

Vital3 is built on the Spring Framework. It uses Spring MVC with Velocity as the view and basic Spring Controller classes as the controllers. Hibernate is used as the persistence layer, and Spring's HibernateDaoSupport classes are used for the Data-Access-Object (DAO) class. All persistent data access is done through the DAO, so Hibernate code is only found inside the DAO itself.

The way I've been using Spring MVC can be extended by adding new controllers, new view templates, and then updating the Vital3-Servlet.xml file with the new bean definitions and mappings. Velocity templates are parsed by Spring's built-in velocity engine.

Directory Structure Overview:

(vital3): the root of the project. Contains everything. Notable files include project.xml, project.properties, clean.sh, build.sh, and reload.sh
codegen: contains the ant folder, beanmaker java class (for easy javabean code-generation), and a custom-made (out of date) vital3 code-generator with templates.
codegen/ant: contains ant project file, used for cleaning the build, generating java classes from hibernate files, and reloading the tomcat app.
logs: contains log files for vital3, both unit-testing and runtime log to the same files.
src: contains directories for source code and configuration.
src/java: contains all java source code
src/hibernate: contains the hibernate mapping files and the hibernate properties file that maven uses for its hibernate plugin.
src/test: contains all java unit testing code.
src/conf: contains configuration files for vital3 such as Spring bean files and log4j properties.
target: contains unit-testing results, classes, and web-related files.
target/classes: not in svn. Maven puts your compiled classes here for unit-testing.
target/test-classes: not in svn. Maven puts the unit-testing classes themselves here.
target/test-reports: not in svn. The unit-test reports are put here after the unit-tests run.
target/vital3: the web application folder. All static or jsp files go in here.
target/vital3/WEB-INF/classes: not in svn. Maven puts your compiled classes in here.
target/vital3/WEB-INF/lib: not in svn. Maven puts your dependency jars in here.
target/vital3/WEB-INF/velocity: the velocity templates live here.

Unit Testing:

Unit Testing covers a fair amount of the code. Mostly controllers. Classes have been written specifically for Unit Testing, noteably the MockDAO interface and MockAssetDAO class. MockDAO will provide some limited database-simulation ability so that no actual database connection is made and hibernate is not used. Templates are not covered under unit tests.

To run a single test from the command line using Maven 1.0.2
maven -Dtestcase=ccnmtl.vital3.test.Vital3GeneralTest test:single

For easier to read output while debugging tests, add to project.properties
#Junit Options
maven.junit.format=plain
maven.junit.usefile=false


Building:

If you have just checked out or downloaded Vital 3 for the first time, please see INSTALL.txt for instructions.


Building with Apple XCode:

I've been using XCode to develop the project and Maven to build it and manage dependencies. Miraculously, they manage to work together fairly well, although it requires a few workarounds: I made a symlink to xnoybis sandboxes at my own /usr/local/share/sandboxes, and I need to have my local maven repos mirroring the content of the xnoybis one (manually). I have the xcode project checked in but I don't know if others will be able to use it. The maven project.xml file contains the list of dependencies and specifies everything about the build. I've been using the command "maven war:webapp" to build. As you can see in the directory structure overview, the target directory is in svn will all the web stuff already in it. Maven will put the compiled classes, jars, and config files in the respective places without harming the rest of the target directory. If you want to clean the compiled classes out for a clean build, run clean.sh in the root vital3 directory. IMPORTANT: after cleaning, run build.sh from xnoybis because when building through xcode, it cannot create directories!

Hibernate + Codegen:

There are three types of code-generation I've set up: 'ant codegen' will generate java classes from your hibernate mapping files. 'python codegen.py xxxx' uses customizable templates to generate java code snippets for various uses. 'java Beanmaker xxxx' will generate javabean code (getter/setter/property) nice and easily.

The ant "codegen" task can be used to create java classes for not-yet-mapped hibernate mappings, but they can't directly "update" java classes you've already generated before! Use "ant codegen" and it will generate the java source files in the src/hibernate directory.

My codegen tool is writtin in python, and you need to pass it the name of the class you are generating code for. For example, to generate code for use with the Asset class, use "python codegen.py Asset" and it will spit out all the templates parsed using the properties from the Asset class as defined from within the codegen.py file itself. This is not meant to be an all-powerful solution, but it has saved me a ton of time so far.

Beanmaker: a java app I found on the web. Very helpful for generating tedious javabean code.

Hibernate SQL:

To generate SQL ddl, don't use ant. Use the maven hibernate:schema-export command when you're in the base directory and it will print out ddl for all the tables. You must build the target before you do this, so first run maven war:webapp, then run maven hibernate:schema-export. If the target won't build but you really need the SQL, just manually put the hbm.xml files into target/classes along with working compiled java class files and then run the schema-export.

Important Notes:

When adding new persistent classes or modifying existing ones, you must make sure that there is a MockDBTable for your new class inside MockDAO.
