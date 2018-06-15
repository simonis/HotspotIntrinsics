#!groovy

// /share/software/Java/SaxonHE9-6-0-5J/saxon9he.jar
def SAXON = "saxon9he.jar"
def XSL = "hotspot.xsl"

def spaces_p = /(\w?[ ]*).*/
def comment_p = /([ ]*)\/\/.*/
def function_p = /[fF] ([ ]*).*/

def filename = args[0]
def basename = filename.replaceFirst("\\.[^.]*", "")

def text_in = new File(filename)
def out_xml, out_html
if (args.length > 1) { 
  out_xml = args[1]
}
else { 
  out_xml = basename + ".xml"
}

if (args.length > 2) { 
  out_html = args[2]
}
else { 
  out_html = basename + ".html"
}

println "reading from " + filename
println "writing xml to " + out_xml
println "writing html to " + out_html

def xml = new PrintStream(out_xml)
xml.println "<calling_hirarchy>"

def first_line = true

text_in.eachLine() { line ->
  if (line?.trim()) {
    def spaces_m = (line =~ spaces_p)
    def spaces = spaces_m[0][1].length()
    def comment = (line ==~ comment_p)
    def function = (line ==~ function_p)
    def highlight = Character.isUpperCase(line[0] as Character) ? "highlight" : ""

    line = line.substring(2)
    line = line.replace("<", "&lt;");
    line = line.replace(">", "&gt;");
    line = line.replace("[bg]", "<span style='font-weight:bold; color:green'>    ");
    line = line.replace("[bb]", "<span style='font-weight:bold; color:blue'>    ");
    line = line.replace("[]", "</span>  ");

    xml.print "<call level='${(spaces - 2)/2}' "
    if (function) { 
      xml.print "type='functionCall ${highlight}'>"
    }
    else if (comment) { 
      xml.print "     type='comment'>"
    }
    else { 
      xml.print "                   >"
    }
    if (first_line) {
      first_line = false
      xml.println "<code style=\"white-space: pre-wrap;\">${line.trim()}</code></call>"
    } else {
      xml.println "<code>${line.trim()}</code></call>"
    }
  }
}

xml.println "</calling_hirarchy>"

def cmd = "java -jar $SAXON -t -strip:none $out_xml $XSL -o:$out_html"
println "executing: $cmd"
def proc = cmd.execute()
def out = new StringBuffer(), err = new StringBuffer()
proc.consumeProcessOutput(out, err)
proc.waitForOrKill(10_000)
println "$out"
println "$err"
