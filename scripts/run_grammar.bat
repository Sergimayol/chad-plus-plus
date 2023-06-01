java -jar lib/jflex-full-1.8.2.jar grammar/Scanner.flex -d tmp
java -jar lib/java-cup-11b.jar -destdir tmp grammar/Parser.cup

move .\tmp\*.java .\src\main\java\com\chpp\grammar\