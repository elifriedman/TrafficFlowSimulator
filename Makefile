compile: src/edu/cooper/*.java
	rm -rf ./class
	mkdir class
	javac  -d class/ -sourcepath src: src/edu/cooper/*.java

run:
	java -cp class/: edu.cooper.FlowManager $(dir)
	cp src/index.html $(dir)/output.html

clean:
	rm -rf ./class
