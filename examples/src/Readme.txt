Howto build:

/share/software/Java/jdk1.8.0_66/bin/javac -g -d ../bin/ -XDignore.symbol.file=true io/simonis/*.java
/share/output-jdk9-hs-comp-dbg/images/jdk/bin/jar cvfm ../bin/MethodInstrumentationAgent.jar manifest.mf ../bin/io/simonis/MethodInstrumentationAgent*.class

g++ -fPIC -shared -I /share/output-jdk9-hs-comp-dbg/images/jdk/include/ -I /share/output-jdk9-hs-comp-dbg/images/jdk/include/linux/ -o ../bin/traceMethodAgent.so jvmti/traceMethodAgent.cpp
