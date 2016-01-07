compile: src/edu/cooper/*.java
	rm -rf ./class
	mkdir class
	javac  -d class/ -sourcepath src: src/edu/cooper/*.java

run:
	java -cp class/: edu.cooper.FlowManager

clean:
	rm -rf ./class
