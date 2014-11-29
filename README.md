Compilers
=========

Optimising Java Bytecode using the BCEL library
As part of UCL 2nd Year course. 

Takes in .class files (found in the testfiles folder) using the BCEL library. 
Goes through and optimises the byte code for redundant Go to statements, i.e. goto's pointing to another go to. Deals with re-assinging the first goto statement to the end instruction, and finally re-writing the .class file bytecode so that it's optimised before being run. 

------
The coursework archive contains the following files and directories:

- build.xml : ant build script - it will build an executable jar, with BCEL
library included, with the command "jar"

- src : contains the skeleton source code
- build : where ant stores the compiled classes
- jars : where ant stores the executable jar
- testfiles : contains a public test file with redundant gotos. The Example.class
is actually built from Example.j which is jasmin source code.
- lib : contains bcel 5.2 library jar.
- doc : should contain your report in either PDF or Word format.

Work within this directory structure, and make sure that everything can be built
using the ant script before submitting a single zip file that contains all the
directories.
