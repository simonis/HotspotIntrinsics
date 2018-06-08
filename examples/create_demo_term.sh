#!/bin/bash

XDG_CONFIG_HOME=~/.config_presentation xfce4-terminal --maximize --hide-menubar --disable-server -T "HelloWorld" -e "`pwd`/create_demo_tab.sh hello" --tab -T "javaagent/agentpath/agentlib" -e "`pwd`/create_demo_tab.sh agents" --tab -T "Loop" -e "`pwd`/create_demo_tab.sh loop" --tab -T "Loop/print" -e "`pwd`/create_demo_tab.sh loop_print" --tab -T "LoopWithGC" -e "`pwd`/create_demo_tab.sh loop_with_gc" --tab -T "Random" -e "`pwd`/create_demo_tab.sh random" --tab -T "Random/print" -e "`pwd`/create_demo_tab.sh random_print" --tab -T "Random/tiered" -e "`pwd`/create_demo_tab.sh random_tiered" --tab -T "Snippets" -e "`pwd`/create_demo_tab.sh snippets" --tab -T "ArrayCopy" -e "`pwd`/create_demo_tab.sh arraycopy" &

XDG_CONFIG_HOME=~/.config_presentation xfce4-terminal --hide-menubar --disable-server --geometry=112x13+0-0 -T "HelloWorld -agentlib:jdwp" -e "`pwd`/create_demo_tab.sh debugee" &

XDG_CONFIG_HOME=~/.config_presentation xfce4-terminal --hide-menubar --disable-server --geometry=112x14+0+0 -T "jdb" -e "`pwd`/create_demo_tab.sh jdb" &
