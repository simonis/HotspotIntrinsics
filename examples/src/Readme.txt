Howto build:

mkdir -p ../bin
/share/software/Java/jdk1.8.0_172/bin/javac -g -d ../bin/ -XDignore.symbol.file=true io/simonis/*.java
/share/software/Java/jdk1.8.0_172/bin/jar cvfm ../bin/MethodInstrumentationAgent.jar manifest.mf ../bin/io/simonis/MethodInstrumentationAgent*.class

g++ -fPIC -shared -I /share/output-jdk-dbg/images/jdk/include/ -I /share/output-jd-dbg/images/jdk/include/linux/ -o ../bin/traceMethodAgent.so jvmti/traceMethodAgent.cpp
