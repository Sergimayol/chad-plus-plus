#!/bin/bash

rm src/main/java/com/chpp/grammar/Parser.java
rm src/main/java/com/chpp/grammar/ParserSym.java
rm src/main/java/com/chpp/grammar/Scanner.java

java -jar lib/jflex-full-1.8.2.jar grammar/Scanner.flex
java -jar lib/java-cup-11b.jar grammar/Parser.cup

mv Parser.java src/main/java/com/chpp/grammar/
mv ParserSym.java src/main/java/com/chpp/grammar/
mv Scanner.java src/main/java/com/chpp/grammar/
