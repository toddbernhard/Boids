### Upgrading from v1.0:

1. Make sure you have the master checked out by typing into a bash terminal

       git co master

2. Use the command

       git pull --rebase origin master

to pull down the latest updates, __overwriting__ your version (`--rebase` causes the overwrite)
3. Download the latest version of [Processing](http://processing.org/download/) and unpack it
4. Create a boids directory in your root (/) folder

       sudo mkdir /boids

5. Copy the unpacked processing folder to the directory you just created

       sudo cp -r ~/your_downloads/processing-1.5.1/ /boids/

6. You can delete the core.jar from your root, it's now in that new boids folder

       sudo rm /core.jar

7. NOTE: If you are NOT using a 64-bit Linux distribution, you must copy your respective shared libraries over to where Eclipse expects to find them

       cd /boids/processing-1.5.1/modes/java/libraries/opengl/library/
       cp [YOUR OS]/* linux64/

   where `[YOUR OS]` is `linux32`, `macosx`, `windows32`, or  `windows 64`. Note the _*_ in `[YOUR OS]/*`
8. DONE!


__[Depreciated, to be updated]__
### How to get started:

The Boids simulation was written in Java using Eclipse and this repository is set
up to make it easy for you to get up and running in Eclipse as well.

1. Open a terminal window and navigate to your Eclipse workspace.
2. Once there, clone the repository into your workspace with the command
    git clone git@github.com:toddbernhard/Boids.git
   This will copy all of the files over.
3. Open Eclipse and go to "File"->"Import...," then
   "General"->"Existing Projects into Workspace"
4. At the next screen, select the "Select root directory" and "Browse," and select the directory in your workspace named "Boids" that Git just downloaded for you.
5. Make sure the checkbox next to the Boids project is checked and click "Finish."
5. Eclipse show that there are errors in the project and that it can't locate the processing.core library (core.jar), because it expects to find it in the
   root (/) directory, so we must copy it over there.
6. Copy core.jar from the project directory to your root directory.
7. DONE! The errors should be gone, just run it as an Applet, and you're good to
   go!


I know keeping core.jar (it's Processing's core library) in the root directory is
an ugly fix, but we need an absolute path that everyone can use. If you move
core.jar, you'll change the .classpath file and we will end up in an edit war over
that path.  So, sorry, but it's quick and dirty and works. Let me know if you
know of a more elegant solution! 
