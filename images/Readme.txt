Copy Call Hierarchy from Eclipse:
=================================
https://bugs.eclipse.org/bugs/show_bug.cgi?id=305471

You just have to right-click on an entry of the Call Hierarchy and select "Copy Expanded Hierarchy" from the context menu. It will copy all the hierarchy below your selection into the clipboard.

Create HTML-Version of a Call Hierarchy
=======================================
/usr/local/bin/groovy make_calling_hirarchy_new.groovy C2_Intrinsics.txt

- if you want to have several Call Hierarchies with the same width, just enter whitespace at the end of the first <code></code> element (i.e. the one which has 'style="white-space: pre-wrap;"'.
- you can change the font size individually by adapting 'style="font-size: 50%;"' on the top-level <table> element.
- you can change the indentation width by configuring 'table.calling_hirarchy td.fixed' (the default is '1ex')

Below is a default stylesheet for the Call Hierarchy:


  table.calling_hirarchy {
//    font-family: monospace, arial, helvetica, sans-serif;
//    background-color: #ffffff;
    font-size: smaller;
    border-collapse: collapse;
    margin: 0px auto;
    padding: 2px;
    width: auto;
    overflow: auto;
    border: none;
  }

  table.calling_hirarchy tr {
    border: none;
  }

  table.calling_hirarchy td {
    padding: 5px;
//    font-family: monospace;
  }

  table.calling_hirarchy td.fixed {
    width: 1ex;
  }

  td.indent_level_0 {
    border-style: solid;
    border-width: 2px;
    background-color: #a0a0a0;
  }
  td.indent_level_1 {
    border-style: solid;
    border-width: 2px;
    background-color: #a8a8a8;
  }
  td.indent_level_2 {
    border-style: solid;
    border-width: 2px;
    background-color: #b0b0b0;
  }
  td.indent_level_3 {
    border-style: solid;
    border-width: 2px;
    background-color: #b8b8b8;
  }
  td.indent_level_4 {
    border-style: solid;
    border-width: 2px;
    background-color: #c0c0c0;
  }
  td.indent_level_5 {
    border-style: solid;
    border-width: 2px;
    background-color: #c8c8c8;
  }
  td.indent_level_6 {
    border-style: solid;
    border-width: 2px;
    background-color: #d0d0d0;
  }
  td.indent_level_7 {
    border-style: solid;
    border-width: 2px;
    background-color: #d8d8d8;
  }
  td.indent_level_8 {
    border-style: solid;
    border-width: 2px;
    background-color: #e0e0e0;
  }
  td.indent_level_9 {
    border-style: solid;
    border-width: 2px;
    background-color: #e8e8e8;
  }
  td.indent_level_10 {
    border-style: solid;
    border-width: 2px;
    background-color: #f0f0f0;
  }
  td.indent_level_11 {
    border-style: solid;
    border-width: 2px;
    background-color: #f8f8f8;
  }
  td.indent_level_12 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_13 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_14 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_15 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_16 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_17 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.indent_level_18 {
    border-style: solid;
    border-width: 2px;
    background-color: #ffffff;
  }
  td.functionCall {
//    font-weight: bold;
    color: darkblue;
  }
  td.javaCall {
    font-weight: bold;
    color: darkgreen;
  }
  td.comment {
    font-style: italic;
    color: maroon;
  }
  td.highlight {
    border-style: solid;
    border-width: 2px;
    background-color: #a0ffa0;
  }
  table.calling_hirarchy td[rowspan] {
    border-style: none;
    border-width: 0;
    border-left: dotted;
    border-left-width: 2px;
  }

