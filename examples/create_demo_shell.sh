
export PS1="\[\e]0;\w\a\]\n\[\e[32m\]\u@\h \[\e[33m\]\w\[\e[0m\]\n\$ "

export PATH=/share/output-jdk9-hs-comp-dbg/images/jdk/bin:$PATH

export _JAVA_OPTIONS='-Xbatch -XX:-UseCompressedOops -XX:+UseSerialGC -XX:-TieredCompilation -XX:-UseOnStackReplacement -XX:+UnlockDiagnosticVMOptions -XX:-CheckIntrinsics -XX:-LogVMOutput -XX:CICompilerCount=2'

if [ "$1" == "random_print" ]; then
  export LD_LIBRARY_PATH=/share/OpenJDK/hsdis
fi

alias javac=/share/output-jdk9-hs-comp-opt/images/jdk/bin/javac

cd /c/Users/D046063/public_html/hotspot/JET2016/git/examples/bin

set -o history
unset HISTFILE
history -c
history -r ../.history_$1
