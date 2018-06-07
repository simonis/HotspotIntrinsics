
export PS1="\[\e]0;\w\a\]\n\[\e[32m\]\u@\h:\[\033[01;34m\]\w\[\033[00m\]\n\$ "

CWD=`pwd`

export PATH=/share/output-jdk-dbg/images/jdk/bin:$PATH

export RLWRAP_HOME=$CWD

export _JAVA_OPTIONS='-Xbatch -XX:-UseCompressedOops -XX:+UseSerialGC -XX:-TieredCompilation -XX:-UseOnStackReplacement -XX:+UnlockDiagnosticVMOptions -XX:-CheckIntrinsics -XX:-LogVMOutput -XX:CICompilerCount=1'

if [ "$1" == "random_print" ]; then
  export LD_LIBRARY_PATH=/share/OpenJDK/hsdis
else
if [ "$1" == "loop" ]; then
  export _JAVA_OPTIONS='-Xbatch -XX:-UseCompressedOops -XX:+UseSerialGC -XX:-TieredCompilation -XX:-UseOnStackReplacement -XX:+UnlockDiagnosticVMOptions -XX:-LogVMOutput -XX:CICompilerCount=1'
else
if [ "$1" == "loop_print" ]; then
  export _JAVA_OPTIONS='-Xbatch -XX:-UseCompressedOops -XX:+UseSerialGC -XX:-TieredCompilation -XX:-UseOnStackReplacement -XX:+UnlockDiagnosticVMOptions -XX:-LogVMOutput -XX:CICompilerCount=1'
else
if [ "$1" == "loop_with_gc" ]; then
  export _JAVA_OPTIONS='-Xbatch -XX:-UseCompressedOops -XX:+UseSerialGC -XX:-TieredCompilation -XX:-UseOnStackReplacement -XX:+UnlockDiagnosticVMOptions -XX:-LogVMOutput -XX:CICompilerCount=1'
fi
fi
fi
fi

alias javac=/share/software/Java/jdk1.8.0_172/bin/javac
alias java9=/share/output-jdk9-dev_ObjectLayout-dbg/images/jdk/bin/java
alias la='ls -la'

rm -rf /tmp/Demo_$1
mkdir -p /tmp/Demo_$1
cd /tmp/Demo_$1

export CLASSPATH=$CWD/bin

set -o history
unset HISTFILE
history -c
history -r $CWD/.history_$1
