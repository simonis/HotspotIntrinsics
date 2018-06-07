Howto build:
============

mkdir -p ../bin

With Java 8
-----------
/share/software/Java/jdk1.8.0_172/bin/javac -g -d ../bin/ -XDignore.symbol.file=true io/simonis/{HelloWorld,MethodInstrumentationAgent,ArrayCopy*,Loop*,Random}.java
/share/software/Java/jdk1.8.0_172/bin/jar cvfm ../bin/MetInstAgent.jar manifest.mf ../bin/io/simonis/MethodInstrumentationAgent*.class

With Java 10
------------
/share/output-jdk-dbg/images/jdk/bin/javac -g -d ../bin/ --add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED io/simonis/{HelloWorld,MethodInstrumentationAgent,ArrayCopy*,Loop*,Random}.java
/share/output-jdk-dbg/images/jdk/bin/jar cvfm ../bin/MetInstAgent.jar manifest.mf ../bin/io/simonis/MethodInstrumentationAgent*.class

g++ -fPIC -shared -I /share/output-jdk-dbg/images/jdk/include/ -I /share/output-jdk-dbg/images/jdk/include/linux/ -o ../bin/trMethAgent.so jvmti/traceMethodAgent.cpp

/share/output-panama-dbg/images/jdk/bin/javac --add-modules jdk.internal.vm.ci,java.base --add-exports jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.panama=ALL-UNNAMED --add-exports jdk.internal.vm.ci/jdk.vm.ci.panama.amd64=ALL-UNNAMED --add-exports java.base/jdk.internal.misc=ALL-UNNAMED -d ../bin/ io/simonis/{RandomSnippet,Snippets}.java
